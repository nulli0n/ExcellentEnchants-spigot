package su.nightexpress.excellentenchants.tier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.utils.Colors2;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;

import java.util.*;
import java.util.stream.Collectors;

public class TierManager extends AbstractManager<ExcellentEnchants> {

    public static final String FILE_NAME = "tiers.yml";

    private final Map<String, Tier> tierMap;

    public TierManager(@NotNull ExcellentEnchants plugin) {
        super(plugin);
        this.tierMap = new HashMap<>();
    }

    @Override
    protected void onLoad() {
        JYML config = this.getConfig();

        if (config.getSection("").isEmpty()) {
            Config.getDefaultTiers().forEach(tier -> tier.write(config, tier.getId()));
        }

        for (String sId : config.getSection("")) {
            String path = sId + ".";

            int priority = config.getInt(path + "Priority");
            String name = config.getString(path + "Name", sId);
            String color = config.getString(path + "Color", Colors2.WHITE);

            Map<ObtainType, Double> chance = new HashMap<>();
            for (ObtainType obtainType : ObtainType.values()) {
                config.addMissing(path + "Obtain_Chance." + obtainType.name(), 50D);

                double chanceType = config.getDouble(path + "Obtain_Chance." + obtainType.name());
                chance.put(obtainType, chanceType);
            }

            Tier tier = new Tier(sId, priority, name, color, chance);
            this.tierMap.put(tier.getId(), tier);
        }
        config.saveChanges();

        this.plugin.info("Tiers Loaded: " + this.tierMap.size());
    }

    @Override
    protected void onShutdown() {
        this.tierMap.clear();
    }

    @NotNull
    public JYML getConfig() {
        return JYML.loadOrExtract(plugin, FILE_NAME);
    }

    @NotNull
    public Map<String, Tier> getTierMap() {
        return tierMap;
    }

    @NotNull
    public Collection<Tier> getTiers() {
        return this.getTierMap().values();
    }

    @Nullable
    public Tier getTierById(@NotNull String id) {
        return this.getTierMap().get(id.toLowerCase());
    }

    @NotNull
    public List<String> getTierIds() {
        return new ArrayList<>(this.getTierMap().keySet());
    }

    @Nullable
    public Tier getTierByChance(@NotNull ObtainType obtainType) {
        Map<Tier, Double> map = this.getTiers().stream()
            .filter(tier -> tier.getChance(obtainType) > 0D)
            .collect(Collectors.toMap(k -> k, v -> v.getChance(obtainType), (o, n) -> n, HashMap::new));
        if (map.isEmpty()) return null;

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
