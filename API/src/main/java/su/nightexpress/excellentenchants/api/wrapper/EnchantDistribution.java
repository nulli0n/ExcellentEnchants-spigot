package su.nightexpress.excellentenchants.api.wrapper;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.Enums;
import su.nightexpress.nightcore.util.Lists;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EnchantDistribution implements Writeable {

    private final boolean        treasure;
    private final boolean        discoverable;
    private final boolean        tradable;
    private final boolean        onMobSpawnEquipment;
    private final boolean        onRandomLoot;
    private final boolean        onTradedEquipment;
    private final Set<TradeType> tradeTypes;

    public EnchantDistribution(@NotNull Set<TradeType> tradeTypes,
                               boolean treasure,
                               boolean discoverable,
                               boolean tradable,
                               boolean onMobSpawnEquipment,
                               boolean onRandomLoot,
                               boolean onTradedEquipment) {
        this.tradeTypes = tradeTypes;
        this.treasure = treasure;
        this.discoverable = discoverable;
        this.tradable = tradable;
        this.onMobSpawnEquipment = onMobSpawnEquipment;
        this.onRandomLoot = onRandomLoot;
        this.onTradedEquipment = onTradedEquipment;
    }

    @NotNull
    public static EnchantDistribution defaults() {
        return new EnchantDistribution(
            new HashSet<>(Arrays.asList(TradeType.values())),
            false,
            true,
            true,
            true,
            true,
            true
        );
    }

    @NotNull
    public static EnchantDistribution regular(@NotNull TradeType... tradeTypes) {
        return new EnchantDistribution(
            Lists.newSet(tradeTypes),
            false,
            true,
            true,
            true,
            true,
            true
        );
    }

    @NotNull
    public static EnchantDistribution treasure(@NotNull TradeType... tradeTypes) {
        return new EnchantDistribution(
            Lists.newSet(tradeTypes),
            true,
            false,
            true,
            false,
            true,
            false
        );
    }

    public static EnchantDistribution read(@NotNull FileConfig config, @NotNull String path) {
        boolean treasure = ConfigValue.create(path + ".Treasure",
            false,
            "Sets whether this enchantment is a treasure enchantment.",
            "Treasure enchantments are those that can't be obtained using an enchantment table, and won't be generated on randomly enchanted equipment sold by villagers or worn by mobs.",
            "Treasure enchantments can only be received via looting, trading, or fishing.",
            "If a treasure enchantment is tradable, it will have double the price (before capping to 64 emeralds) compared to a non-treasure enchantment of the same level.",
            "[*] Reboot required when changed."
        ).read(config);

        boolean tradable = ConfigValue.create(path + ".Tradeable",
            true,
            "Controls if this enchantment can be sold by villagers.",
            "https://minecraft.wiki/w/Trading#Librarian",
            "[*] Reboot required when changed."
        ).read(config);

        var tradeTypes = ConfigValue.forSet(path + ".TradeTypes",
            name -> Enums.get(name, TradeType.class),
            (cfg, path2, set) -> cfg.set(path2, set.stream().map(Enum::name).toList()),
            () -> Lists.newSet(TradeType.values()),
            "Sets in which village biomes this enchantment can be found in villager trades.",
            "Allowed values: [" + Enums.inline(TradeType.class) + "]",
            "https://minecraft.wiki/w/Villager_Trade_Rebalance#Trading",
            "[*] Reboot required when changed.",
            "[*] Has no effect if 'Tradeable' is set on 'false' and Villager Trade Rebalance is disabled."
        ).read(config);

        boolean onMobSpawnEquipment = ConfigValue.create(path + ".On_Mob_Spawn_Equipment",
            true,
            "Controls if this enchantment can be found on spawned mobs' equipment.",
            "https://minecraft.wiki/w/Armor#Armor_equipped_on_mobs",
            "[*] Reboot required when changed.",
            "[*] Has no effect if 'Treasure' is set on 'true'."
        ).read(config);

        boolean onTradedEquipment = ConfigValue.create(path + ".On_Traded_Equipment",
            true,
            "Controls if this enchantment can be found on equipment sold by villagers.",
            "https://minecraft.wiki/w/Trading#Trade_offers",
            "[*] Reboot required when changed.",
            "[*] Has no effect if 'Treasure' is set on 'true' or Villager Trade Rebalance is ENABLED."
        ).read(config);

        boolean onRandomLoot = ConfigValue.create(path + ".On_Random_Loot",
            true,
            "Controls if this enchantment can be found on naturally generated equipment from loot tables.",
            "https://minecraft.wiki/w/Loot_table",
            "[*] Reboot required when changed."
        ).read(config);

        boolean discoverable = ConfigValue.create(path + ".Discoverable",
            true,
            "Controls if this enchantment can be found in an enchanting table or use to enchant items generated by loot tables.",
            "https://minecraft.wiki/w/Enchanting#Enchanting_table",
            "[*] Reboot required when changed.",
            "[*] Has no effect if 'Treasure' is set on 'true'."
        ).read(config);

        return new EnchantDistribution(tradeTypes, treasure, discoverable, tradable, onMobSpawnEquipment, onRandomLoot, onTradedEquipment);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Treasure", this.treasure);
        config.set(path + ".Tradeable", this.tradable);
        config.set(path + ".TradeTypes", Lists.modify(this.tradeTypes, Enum::name));
        config.set(path + ".On_Mob_Spawn_Equipment", this.onMobSpawnEquipment);
        config.set(path + ".On_Traded_Equipment", this.onTradedEquipment);
        config.set(path + ".On_Random_Loot", this.onRandomLoot);
        config.set(path + ".Discoverable", this.discoverable);
    }

    public boolean isTreasure() {
        return this.treasure;
    }

    public boolean isTradable() {
        return this.tradable;
    }

    @NotNull
    public Set<TradeType> getTrades() {
        return this.tradeTypes;
    }

    public boolean isDiscoverable() {
        return this.discoverable;
    }

    public boolean isOnMobSpawnEquipment() {
        return this.onMobSpawnEquipment;
    }

    public boolean isOnRandomLoot() {
        return this.onRandomLoot;
    }

    public boolean isOnTradedEquipment() {
        return this.onTradedEquipment;
    }
}
