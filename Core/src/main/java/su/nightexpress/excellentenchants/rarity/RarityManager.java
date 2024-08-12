package su.nightexpress.excellentenchants.rarity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.SimpleManager;
import su.nightexpress.nightcore.util.random.Rnd;

import java.util.*;

public class RarityManager extends SimpleManager<EnchantsPlugin> {

    private final Map<String, Rarity> rarityMap;

    public RarityManager(@NotNull EnchantsPlugin plugin) {
        super(plugin);
        this.rarityMap = new HashMap<>();
    }

    @Override
    protected void onLoad() {
        this.loadRarities();
    }

    @Override
    protected void onShutdown() {
        this.rarityMap.clear();
    }

    private void loadRarities() {
        FileConfig config = this.plugin.getConfig();
        if (!config.contains("Rarity")) {
            EnchantRarity.getDefaults().forEach(rarity -> rarity.write(config, "Rarity." + rarity.getId()));
        }

        config.getSection("Rarity").forEach(sId -> {
            Rarity rarity = EnchantRarity.read(config, "Rarity." + sId, sId);
            this.rarityMap.put(rarity.getId(), rarity);
        });

        if (this.rarityMap.isEmpty()) {
            this.plugin.warn("No rarities are defined in the config! Adding dummy one for the plugin to work.");
            this.plugin.warn("You MUST FIX this issue for the plugin to work properly.");
            Rarity rarity = EnchantRarity.DUMMY;
            this.rarityMap.put(rarity.getId(), rarity);
        }

        this.plugin.info("Loaded " + this.rarityMap.size() + " rarities.");
    }

    @NotNull
    public Rarity getRarityByWeight() {
        Map<Rarity, Double> map = new HashMap<>();

        for (Rarity rarity : this.getRarities()) {
            map.put(rarity, (double) rarity.getWeight());
        }

        return Rnd.getByWeight(map);
    }

    @NotNull
    public Set<Rarity> getRarities() {
        return new HashSet<>(this.rarityMap.values());
    }

    @NotNull
    public List<String> getRarityNames() {
        return new ArrayList<>(this.rarityMap.keySet());
    }

    @Nullable
    public Rarity getRarity(@NotNull String id) {
        return this.rarityMap.get(id.toLowerCase());
    }
}
