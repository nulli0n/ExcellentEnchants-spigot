package su.nightexpress.excellentenchants;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import io.papermc.paper.tag.TagEntry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantDefaults;
import su.nightexpress.excellentenchants.api.EnchantKeys;
import su.nightexpress.excellentenchants.api.EnchantRegistry;
import su.nightexpress.excellentenchants.api.bridge.PostFlatten;
import su.nightexpress.excellentenchants.api.config.ConfigBridge;
import su.nightexpress.excellentenchants.api.config.DistributionConfig;
import su.nightexpress.excellentenchants.api.item.ItemSet;
import su.nightexpress.excellentenchants.api.item.ItemSetRegistry;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDefinition;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDistribution;
import su.nightexpress.excellentenchants.api.wrapper.TradeType;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

public class EnchantsBootstrap implements PluginBootstrap {

    @NotNull
    private static TagKey<ItemType> customItemTag(@NotNull String name) {
        return TagKey.create(RegistryKey.ITEM, Key.key(ConfigBridge.NAMESPACE, name));
    }

    @NotNull
    private static Key customKey(@NotNull String id) {
        return EnchantKeys.custom(id);
    }

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        File dataDir = context.getDataDirectory().toFile();

        ConfigBridge.load(dataDir, true); // Load distribution config, assign isPaper field.
        EnchantDefaults.load(dataDir); // Load defaults and read from the config files Definition and Distribution settings for enchants.

        var lifeCycle = context.getLifecycleManager();

        // Create custom tags with custom items for enchantment's 'primary' and 'supported' items sets.
        // Use 'postFlatten' instead of 'preFlatten' to access vanilla item tags to create default item sets more effectively.
        lifeCycle.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ITEM).newHandler(event -> {
            PostFlattenTagRegistrar<ItemType> registrar = event.registrar();
            PostFlatten.registrar = registrar;

            ItemSetRegistry.load(dataDir); // Load default item types, uses ConfigBridge.isPaper() to determine which items source to use.
            ItemSetRegistry.getByIdMap().forEach((keyName, category) -> {
                // Create a new tag for our custom item set.
                TagKey<ItemType> tagKey = customItemTag(keyName);
                // Transform item names into TagEntry values by creating TypedKey for each item.
                var tagEntries = category.getMaterials().stream().map(itemName -> TypedKey.create(RegistryKey.ITEM, Key.key(itemName)))/*.map(TagEntry::valueEntry)*/.toList();

                registrar.addToTag(tagKey, tagEntries);
            });

            PostFlatten.registrar = null;
        }));

        // Register a new handler for the freeze lifecycle event on the enchantment registry
        lifeCycle.registerEventHandler(RegistryEvents.ENCHANTMENT.freeze().newHandler(event -> {
            EnchantRegistry.getDataMap().forEach((enchantId, data) -> {
                // Skip disabled enchantments.
                if (DistributionConfig.isDisabled(enchantId)) {
                    context.getLogger().info("Enchantment {} is disabled. Skip.", enchantId);
                    return;
                }

                EnchantDefinition definition = data.getDefinition();
                String primaryId = definition.getPrimaryItemsId();
                String supportedId = definition.getSupportedItemsId();

                ItemSet primarySet = ItemSetRegistry.getById(primaryId);
                ItemSet supportedSet = ItemSetRegistry.getById(supportedId);

                if (primarySet == null || supportedSet == null) {
                    context.getLogger().error("Could not register enchantment '{}' due to invalid primary/supported items sets: {}/{}", enchantId, primaryId, supportedId);
                    return;
                }

                // Working too.
//                RegistryKeySet<ItemType> primarySet = RegistrySet.keySet(RegistryKey.ITEM, ItemTypeKeys.DIAMOND_CHESTPLATE);
//                RegistryKeySet<ItemType> supportedSet = RegistrySet.keySet(RegistryKey.ITEM, ItemTypeKeys.DIAMOND_HELMET);

                TagKey<ItemType> primaryItemsTag = customItemTag(primaryId);
                TagKey<ItemType> supportedItemsTag = customItemTag(supportedId);
                var primaryItems = event.getOrCreateTag(primaryItemsTag);
                var supportedItems = event.getOrCreateTag(supportedItemsTag);

                List<TypedKey<Enchantment>> exclusiveEntries = definition.getExclusiveSet().stream()
                    .map(BukkitThing::parseKey)
                    .map(EnchantKeys::adaptCustom) // Remap with the 'correct' namespace according to the config setting.
                    .filter(key -> !DistributionConfig.isDisabled(key.value()))
                    .map(EnchantmentKeys::create)
                    .toList();
                var exclusiveSet = RegistrySet.keySet(RegistryKey.ENCHANTMENT, exclusiveEntries);

                EquipmentSlotGroup[] activeSlots = Stream.of(supportedSet.getSlots()).map(EquipmentSlot::getGroup).toArray(EquipmentSlotGroup[]::new);
                Key key = customKey(enchantId);
                Component component = MiniMessage.miniMessage().deserialize(definition.getDisplayName());
                String nameOnly = MiniMessage.miniMessage().stripTags(definition.getDisplayName());

                event.registry().register(
                    EnchantmentKeys.create(key),
                    builder -> builder
                        .description(Component.translatable(key.asString(), nameOnly, component.style()))
                        .primaryItems(primaryItems)
                        .supportedItems(supportedItems)
                        .exclusiveWith(exclusiveSet)
                        .anvilCost(definition.getAnvilCost())
                        .maxLevel(definition.getMaxLevel())
                        .weight(definition.getWeight())
                        .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(definition.getMinCost().base(), definition.getMinCost().perLevel()))
                        .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(definition.getMaxCost().base(), definition.getMaxCost().perLevel()))
                        .activeSlots(activeSlots)
                );
            });
        }));

        lifeCycle.registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ENCHANTMENT).newHandler(event -> {
            var registrar = event.registrar();

            EnchantRegistry.getDataMap().forEach((enchantId, data) -> {
                // Skip disabled enchantments.
                if (DistributionConfig.isDisabled(enchantId)) return;

                EnchantDistribution distribution = data.getDistribution();
                TypedKey<Enchantment> key = EnchantmentKeys.create(customKey(enchantId));

                TagEntry<Enchantment> entry = TagEntry.valueEntry(key);
                var list = Lists.newSet(entry);

                // Any enchantment can be treasure.
                if (distribution.isTreasure()) {
                    registrar.addToTag(EnchantmentTagKeys.TREASURE, list);
                    registrar.addToTag(EnchantmentTagKeys.DOUBLE_TRADE_PRICE, list);
                }
                // This tag is included in other tags, which makes it impossible to exclude enchants of this tag from other tags.
                //else registrar.addToTag(EnchantmentTagKeys.NON_TREASURE, list);

                // Any enchantment can be on random loot.
                if (distribution.isOnRandomLoot() && DistributionConfig.DISTRIBUTION_RANDOM_LOOT.get()) {
                    registrar.addToTag(EnchantmentTagKeys.ON_RANDOM_LOOT, list);
                }

                // Only non-treasure enchantments should be on mob equipment, traded equipment and non-rebalanced trades.
                if (!distribution.isTreasure()) {
                    if (distribution.isOnMobSpawnEquipment() && DistributionConfig.DISTRIBUTION_MOB_EQUIPMENT.get()) {
                        registrar.addToTag(EnchantmentTagKeys.ON_MOB_SPAWN_EQUIPMENT, list);
                    }

                    // This only works if Villager Trade Rebalance is disabled.
                    // Because this experiment uses trades with predefined Enchantment Providers with Single Enchantment options.
                    // Example: VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_BOOTS, 8, 1, 3, 15, 0.05F, TradeRebalanceEnchantmentProviders.TRADES_DESERT_ARMORER_BOOTS_4), VillagerType.DESERT)
                    // Reference Classes:
                    // - VillagerTrades
                    // - TradeRebalanceEnchantmentProviders
                    // - VanillaEnchantmentProviders
                    if (distribution.isOnTradedEquipment() && DistributionConfig.DISTRIBUTION_TRADE_EQUIPMENT.get()) {
                        registrar.addToTag(EnchantmentTagKeys.ON_TRADED_EQUIPMENT, list);
                    }
                }

                // Any enchantment can be tradable (on enchanted books).
                if (distribution.isTradable() && DistributionConfig.DISTRIBUTION_TRADING.get()) {
                    distribution.getTrades().forEach(tradeType -> {
                        registrar.addToTag(getTradeKey(tradeType), list);
                    });
                    registrar.addToTag(EnchantmentTagKeys.TRADEABLE, list);
                }

                if (data.isCurse()) {
                    registrar.addToTag(EnchantmentTagKeys.CURSE, list);
                }
                else {
                    // Only non-curse and non-treasure enchantments should go in enchanting table.
                    if (!distribution.isTreasure()) {
                        if (distribution.isDiscoverable() && DistributionConfig.DISTRIBUTION_ENCHANTING.get()) {
                            registrar.addToTag(EnchantmentTagKeys.IN_ENCHANTING_TABLE, list);
                        }
                    }
                }
            });
        }));
    }

    @NotNull
    private static TagKey<Enchantment> getTradeKey(@NotNull TradeType tradeType) {
        return switch (tradeType) {
            case DESERT_COMMON -> EnchantmentTagKeys.TRADES_DESERT_COMMON;
            case DESERT_SPECIAL -> EnchantmentTagKeys.TRADES_DESERT_SPECIAL;
            case PLAINS_COMMON -> EnchantmentTagKeys.TRADES_PLAINS_COMMON;
            case PLAINS_SPECIAL -> EnchantmentTagKeys.TRADES_PLAINS_SPECIAL;
            case SAVANNA_COMMON -> EnchantmentTagKeys.TRADES_SAVANNA_COMMON;
            case SAVANNA_SPECIAL -> EnchantmentTagKeys.TRADES_SAVANNA_SPECIAL;
            case JUNGLE_COMMON -> EnchantmentTagKeys.TRADES_JUNGLE_COMMON;
            case JUNGLE_SPECIAL -> EnchantmentTagKeys.TRADES_JUNGLE_SPECIAL;
            case SNOW_COMMON -> EnchantmentTagKeys.TRADES_SNOW_COMMON;
            case SNOW_SPECIAL -> EnchantmentTagKeys.TRADES_SNOW_SPECIAL;
            case SWAMP_COMMON -> EnchantmentTagKeys.TRADES_SWAMP_COMMON;
            case SWAMP_SPECIAL -> EnchantmentTagKeys.TRADES_SWAMP_SPECIAL;
            case TAIGA_COMMON -> EnchantmentTagKeys.TRADES_TAIGA_COMMON;
            case TAIGA_SPECIAL -> EnchantmentTagKeys.TRADES_TAIGA_SPECIAL;
        };
    }
}
