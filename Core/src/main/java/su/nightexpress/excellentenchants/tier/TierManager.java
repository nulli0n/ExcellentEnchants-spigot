package su.nightexpress.excellentenchants.tier;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TierManager extends AbstractManager<ExcellentEnchants> {

    public static final String FILE_NAME = "tiers.yml";

    private JYML                    config;
    private final Map<String, Tier> tiers;

    public TierManager(@NotNull ExcellentEnchants plugin) {
        super(plugin);
        this.tiers = new ConcurrentHashMap<>();

    }

    @Override
    protected void onLoad() {
        /*if (ExcellentEnchants.isLoaded) {
            this.getTiers().forEach(tier -> tier.getEnchants().clear());
            return;
        }*/

        this.config = JYML.loadOrExtract(plugin, FILE_NAME);

        for (String sId : config.getSection("")) {
            String path = sId + ".";

            int priority = config.getInt(path + "Priority");
            String name = config.getString(path + "Name", sId);

            ChatColor color;
            try {
                color = ChatColor.of(config.getString(path + "Color", ChatColor.WHITE.getName()));
            }
            catch (IllegalArgumentException e) {
                color = ChatColor.WHITE;
            }

            Map<ObtainType, Double> chance = new HashMap<>();
            for (ObtainType obtainType : ObtainType.values()) {
                config.addMissing(path + "Obtain_Chance." + obtainType.name(), 50D);

                double chanceType = config.getDouble(path + "Obtain_Chance." + obtainType.name());
                chance.put(obtainType, chanceType);
            }

            Tier tier = new Tier(sId, priority, name, color, chance);
            this.tiers.put(tier.getId(), tier);
        }

        this.plugin.info("Tiers Loaded: " + this.tiers.size());
    }

    @Override
    protected void onShutdown() {
        this.tiers.clear();
    }

    @NotNull
    public JYML getConfig() {
        return config;
    }

    @Nullable
    public Tier getTierById(@NotNull String id) {
        return this.tiers.get(id.toLowerCase());
    }

    @NotNull
    public Collection<Tier> getTiers() {
        return this.tiers.values();
    }

    @NotNull
    public List<String> getTierIds() {
        return new ArrayList<>(this.tiers.keySet());
    }

    @Nullable
    public Tier getTierByChance(@NotNull ObtainType obtainType) {
        Map<Tier, Double> map = new HashMap<>();
        this.getTiers().forEach(tier -> map.put(tier, tier.getChance(obtainType)));
        map.values().removeIf(chance -> chance <= 0D);
        return Rnd.getByWeight(map);
    }

    @NotNull
    public Tier getMostCommon() {
        return this.getTiers().stream().min(Comparator.comparingInt(Tier::getPriority)).orElseThrow();
    }

    @NotNull
    public Tier getByRarityModifier(double point) {
        int minPriority = this.getTiers().stream().mapToInt(Tier::getPriority).min().orElse(0);
        int maxPriority = this.getTiers().stream().mapToInt(Tier::getPriority).max().orElse(0);

        int threshold = (int) Math.ceil(minPriority + (maxPriority - minPriority) * point);

        return this.getTiers().stream().filter(tier -> tier.getPriority() <= threshold)
            .max(Comparator.comparingInt(tier -> tier.getPriority() - threshold)).orElse(this.getMostCommon());
    }
}
