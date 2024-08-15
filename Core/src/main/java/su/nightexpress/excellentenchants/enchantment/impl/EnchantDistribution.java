package su.nightexpress.excellentenchants.enchantment.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.Distribution;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.StringUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EnchantDistribution implements Distribution {

    private Set<TradeType> tradeTypes;
    private boolean        treasure;
    private boolean        discoverable;
    private boolean        tradable;
    private boolean        onMobSpawnEquipment;
    private boolean        onRandomLoot;
    private boolean        onTradedEquipment;

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
            new HashSet<>(Arrays.asList(tradeTypes)),
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
            new HashSet<>(Arrays.asList(tradeTypes)),
            true,
            false,
            true,
            false,
            true,
            false
        );
    }

    public void load(@NotNull FileConfig config) {
        this.treasure = ConfigValue.create("Distribution.Treasure",
            this.treasure,
            "Sets whether this enchantment is a treasure enchantment.",
            "Treasure enchantments are those that can't be obtained using an enchantment table, and won't be generated on randomly enchanted equipment sold by villagers or worn by mobs.",
            "Treasure enchantments can only be received via looting, trading, or fishing.",
            "If a treasure enchantment is tradable, it will have double the price (before capping to 64 emeralds) compared to a non-treasure enchantment of the same level.",
            "[*] Reboot required when changed."
        ).read(config);

        this.tradable = ConfigValue.create("Distribution.Tradeable",
            this.tradable,
            "Tradable enchantments are those that can be generated on Enchanted Books sold by librarians.",
            "[*] Reboot required when changed."
        ).read(config);

        this.tradeTypes = ConfigValue.forSet("Distribution.TradeTypes",
            name -> StringUtil.getEnum(name, TradeType.class).orElse(null),
            (cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()),
            () -> new HashSet<>(this.tradeTypes),
            "Sets in which village biomes this enchantment can be found in villager trades.",
            "Allowed values: [" + StringUtil.inlineEnum(TradeType.class, ", ") + "]",
            "https://minecraft.wiki/w/Villager_Trade_Rebalance#Trading",
            "[*] Reboot required when changed.",
            "[**] Has no effect if 'Tradeable' is set on 'false' and Villager Trade Rebalance is disabled."
        ).read(config);

        this.onMobSpawnEquipment = ConfigValue.create("Distribution.On_Mob_Spawn_Equipment",
            this.onMobSpawnEquipment,
            "Sets whether or not this enchantment can be found on spawned mobs' equipment.",
            "[*] Reboot required when changed.",
            "[**] Has no effect if 'Treasure' is set on 'true'."
        ).read(config);

        this.onTradedEquipment = ConfigValue.create("Distribution.On_Traded_Equipment",
            this.onTradedEquipment,
            "Sets whether or not this enchantment can be found on equipment sold by villagers.",
            "[*] Reboot required when changed.",
            "[**] Has no effect if 'Treasure' is set on 'true'."
        ).read(config);

        this.onRandomLoot = ConfigValue.create("Distribution.On_Random_Loot",
            this.onRandomLoot,
            "Sets whether or not this enchantment can be found on naturally generated equipment from loot tables.",
            "[*] Reboot required when changed."
        ).read(config);

        this.discoverable = ConfigValue.create("Distribution.Discoverable",
            this.discoverable,
            "Sets whether or not this enchantment can be generated in enchanting table.",
            "[*] Reboot required when changed.",
            "[**] Has no effect if 'Treasure' is set on 'true'."
        ).read(config);
    }

    @Override
    public boolean isTreasure() {
        return this.treasure;
    }

    @Override
    public boolean isTradable() {
        return this.tradable;
    }

    @Override
    @NotNull
    public Set<TradeType> getTrades() {
        return this.tradeTypes;
    }

    @Override
    public boolean isDiscoverable() {
        return discoverable;
    }

    @Override
    public boolean isOnMobSpawnEquipment() {
        return onMobSpawnEquipment;
    }

    @Override
    public boolean isOnRandomLoot() {
        return onRandomLoot;
    }

    @Override
    public boolean isOnTradedEquipment() {
        return onTradedEquipment;
    }
}
