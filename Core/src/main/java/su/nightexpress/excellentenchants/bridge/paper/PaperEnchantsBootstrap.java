package su.nightexpress.excellentenchants.bridge.paper;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import io.papermc.paper.tag.TagEntry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import su.nightexpress.excellentenchants.EnchantsKeys;
import su.nightexpress.excellentenchants.api.EnchantDefinition;
import su.nightexpress.excellentenchants.api.EnchantDistribution;
import su.nightexpress.excellentenchants.api.item.ItemSet;
import su.nightexpress.excellentenchants.api.item.ItemSetRegistry;
import su.nightexpress.excellentenchants.api.wrapper.TradeType;
import su.nightexpress.excellentenchants.enchantment.DistributionConfig;
import su.nightexpress.excellentenchants.enchantment.EnchantCatalog;
import su.nightexpress.nightcore.bridge.common.NightKey;
import su.nightexpress.nightcore.util.Lists;

public class PaperEnchantsBootstrap implements PluginBootstrap {

    private static final boolean HAS_SPECIAL_TRADE_TAGS = hasSpecialTradeTags();

    @NotNull
    private TagKey<ItemType> customItemTag(@NotNull String name) {
        return TagKey.create(RegistryKey.ITEM, Key.key(EnchantsKeys.NAMESPACE, name));
    }

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        Path dataDirectory = context.getDataDirectory();

        DistributionConfig distributionConfig = DistributionConfig.load(dataDirectory);
        if (distributionConfig.isUseMinecraftNamespace()) {
            EnchantsKeys.setVanillaNamespace();
        }

        var lifeCycle = context.getLifecycleManager();

        // Create custom tags with custom items for enchantment's 'primary' and 'supported' items sets.
        // Use 'postFlatten' instead of 'preFlatten' to access vanilla item tags to create default item sets more effectively.
        lifeCycle.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ITEM).newHandler(event -> {
            PostFlattenTagRegistrar<ItemType> registrar = event.registrar();
            PaperItemTagLookup tagLookup = new PaperItemTagLookup(registrar);
            ItemSetRegistry itemSetRegistry = new ItemSetRegistry(dataDirectory, tagLookup);

            // Load default item types.
            itemSetRegistry.load();

            // Register custom tags for our ItemSet objects.
            itemSetRegistry.values().forEach(itemSet -> {
                TagKey<ItemType> tagKey = this.customItemTag(itemSet.getId());
                // Transform item names into TagEntry values by creating TypedKey for each item.
                var tagEntries = itemSet.getMaterials().stream().map(itemName -> TypedKey.create(RegistryKey.ITEM, Key
                    .key(itemName))).toList();

                registrar.addToTag(tagKey, tagEntries);
            });

            // Load defaults and read from the config files Definition and Distribution settings for enchants.
            EnchantCatalog.loadAll(dataDirectory, itemSetRegistry, (entry, exception) -> context.getLogger().error(
                "Could not load '{}' enchantment: '{}'", entry.getId(), exception.getMessage()));
        }));

        // Register a new handler for the freeze lifecycle event on the enchantment registry
        lifeCycle.registerEventHandler(RegistryEvents.ENCHANTMENT.compose().newHandler(event -> {
            var registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

            EnchantCatalog.enabled().forEach((data) -> {
                EnchantDefinition definition = data.getDefinition();
                ItemSet primarySet = definition.getPrimaryItemSet();
                ItemSet supportedSet = definition.getSupportedItemSet();

                TagKey<ItemType> primaryItemsTag = this.customItemTag(primarySet.getId());
                TagKey<ItemType> supportedItemsTag = this.customItemTag(supportedSet.getId());

                Tag<@NonNull ItemType> primaryItems = event.getOrCreateTag(primaryItemsTag);
                Tag<@NonNull ItemType> supportedItems = event.getOrCreateTag(supportedItemsTag);

                RegistryKeySet<@NonNull Enchantment> exclusiveSet = RegistrySet.keySet(RegistryKey.ENCHANTMENT,
                    definition.getExclusiveSet()
                        .stream()
                        .map(rawKey -> {
                            Key key = NightKey.key(rawKey).toBukkit();
                            if (registry.get(key) == null) {
                                context.getLogger().warn(
                                    "Unknown enchantment '{}' in exclusive list of '{}'. Ensure excluded enchantments are loaded before they are listed as exclusions for others.",
                                    key, data.getId());
                                return null;
                            }
                            return EnchantmentKeys.create(key);
                        })
                        .filter(Objects::nonNull)
                        .toList()
                );

                EquipmentSlotGroup[] activeSlots = Stream.of(supportedSet.getSlots()).map(EquipmentSlot::getGroup)
                    .toArray(EquipmentSlotGroup[]::new);
                Key key = data.getKey();
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
                        .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(definition.getMinCost().base(),
                            definition.getMinCost().perLevel()))
                        .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(definition.getMaxCost().base(),
                            definition.getMaxCost().perLevel()))
                        .activeSlots(activeSlots)
                );
            });
        }));

        lifeCycle.registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ENCHANTMENT).newHandler(event -> {
            var registrar = event.registrar();

            EnchantCatalog.enabled().forEach(data -> {
                EnchantDistribution distribution = data.getDistribution();
                TypedKey<Enchantment> key = EnchantmentKeys.create(data.getKey());

                TagEntry<Enchantment> entry = TagEntry.valueEntry(key);
                Set<TagEntry<Enchantment>> list = Lists.newSet(entry);

                // Any enchantment can be treasure.
                if (distribution.isTreasure()) {
                    registrar.addToTag(EnchantmentTagKeys.TREASURE, list);
                    registrar.addToTag(EnchantmentTagKeys.DOUBLE_TRADE_PRICE, list);
                }
                // This tag is included in other tags, which makes it impossible to exclude enchants of this tag from other tags.
                //else registrar.addToTag(EnchantmentTagKeys.NON_TREASURE, list);

                // Any enchantment can be on random loot.
                if (distribution.isOnRandomLoot() && distributionConfig.isRandomLootEnabled()) {
                    registrar.addToTag(EnchantmentTagKeys.ON_RANDOM_LOOT, list);
                }

                // Only non-treasure enchantments should be on mob equipment, traded equipment and non-rebalanced trades.
                if (!distribution.isTreasure()) {
                    if (distribution.isOnMobSpawnEquipment() && distributionConfig.isMobEquipmentEnabled()) {
                        registrar.addToTag(EnchantmentTagKeys.ON_MOB_SPAWN_EQUIPMENT, list);
                    }

                    // This only works if Villager Trade Rebalance is disabled.
                    // Because this experiment uses trades with predefined Enchantment Providers with Single Enchantment options.
                    // Example: VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_BOOTS, 8, 1, 3, 15, 0.05F, TradeRebalanceEnchantmentProviders.TRADES_DESERT_ARMORER_BOOTS_4), VillagerType.DESERT)
                    // Reference Classes:
                    // - VillagerTrades
                    // - TradeRebalanceEnchantmentProviders
                    // - VanillaEnchantmentProviders
                    if (distribution.isOnTradedEquipment() && distributionConfig.isTradeEquipmentEnabled()) {
                        registrar.addToTag(EnchantmentTagKeys.ON_TRADED_EQUIPMENT, list);
                    }
                }

                // Any enchantment can be tradable (on enchanted books).
                if (distribution.isTradable() && distributionConfig.isTradingEnabled()) {
                    distribution.getTrades().stream().map(PaperEnchantsBootstrap::getTradeKey).distinct().forEach(
                        tradeKey -> {
                            registrar.addToTag(tradeKey, list);
                        });
                    registrar.addToTag(EnchantmentTagKeys.TRADEABLE, list);
                }

                if (data.isCurse()) {
                    registrar.addToTag(EnchantmentTagKeys.CURSE, list);
                }
                else {
                    // Only non-curse and non-treasure enchantments should go in enchanting table.
                    if (!distribution.isTreasure()) {
                        if (distribution.isDiscoverable() && distributionConfig.isEnchantingEnabled()) {
                            registrar.addToTag(EnchantmentTagKeys.IN_ENCHANTING_TABLE, list);
                        }
                    }
                }
            });
        }));
    }

    @NotNull
    private static TagKey<Enchantment> getTradeKey(@NotNull TradeType tradeType) {
        return EnchantmentTagKeys.create(Key.key(Key.MINECRAFT_NAMESPACE, "trades/" + getTradePath(tradeType)));
    }

    @NotNull
    private static String getTradePath(@NotNull TradeType tradeType) {
        return switch (tradeType) {
            case DESERT_COMMON -> "desert_common";
            case DESERT_SPECIAL -> HAS_SPECIAL_TRADE_TAGS ? "desert_special" : "desert_common";
            case PLAINS_COMMON -> "plains_common";
            case PLAINS_SPECIAL -> HAS_SPECIAL_TRADE_TAGS ? "plains_special" : "plains_common";
            case SAVANNA_COMMON -> "savanna_common";
            case SAVANNA_SPECIAL -> HAS_SPECIAL_TRADE_TAGS ? "savanna_special" : "savanna_common";
            case JUNGLE_COMMON -> "jungle_common";
            case JUNGLE_SPECIAL -> HAS_SPECIAL_TRADE_TAGS ? "jungle_special" : "jungle_common";
            case SNOW_COMMON -> "snow_common";
            case SNOW_SPECIAL -> HAS_SPECIAL_TRADE_TAGS ? "snow_special" : "snow_common";
            case SWAMP_COMMON -> "swamp_common";
            case SWAMP_SPECIAL -> HAS_SPECIAL_TRADE_TAGS ? "swamp_special" : "swamp_common";
            case TAIGA_COMMON -> "taiga_common";
            case TAIGA_SPECIAL -> HAS_SPECIAL_TRADE_TAGS ? "taiga_special" : "taiga_common";
        };
    }

    private static boolean hasSpecialTradeTags() {
        try {
            EnchantmentTagKeys.class.getField("TRADES_DESERT_SPECIAL");
            return true;
        }
        catch (NoSuchFieldException exception) {
            return false;
        }
    }
}
