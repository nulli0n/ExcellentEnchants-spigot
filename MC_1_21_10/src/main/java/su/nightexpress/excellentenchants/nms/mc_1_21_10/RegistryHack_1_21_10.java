package su.nightexpress.excellentenchants.nms.mc_1_21_10;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R6.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_21_R6.CraftServer;
import org.bukkit.craftbukkit.v1_21_R6.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_21_R6.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_21_R6.util.CraftNamespacedKey;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.EnchantsKeys;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.item.ItemSet;
import su.nightexpress.excellentenchants.api.wrapper.EnchantCost;
import su.nightexpress.excellentenchants.api.EnchantDefinition;
import su.nightexpress.excellentenchants.api.EnchantDistribution;
import su.nightexpress.excellentenchants.api.wrapper.TradeType;
import su.nightexpress.excellentenchants.bridge.DistributionSettings;
import su.nightexpress.excellentenchants.bridge.EnchantCatalogEntry;
import su.nightexpress.excellentenchants.bridge.RegistryHack;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.util.Reflex;
import su.nightexpress.nightcore.util.text.night.NightMessage;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;

public class RegistryHack_1_21_10 implements RegistryHack {

    private static final MinecraftServer             SERVER;
    private static final     MappedRegistry<Enchantment> ENCHANTS;
    private static final     MappedRegistry<Item>        ITEMS;

    private static final String REGISTRY_FROZEN_TAGS_FIELD = "j"; // frozenTags
    private static final String REGISTRY_ALL_TAGS_FIELD    = "k"; // allTags
    private static final String TAG_SET_UNBOUND_METHOD     = "a"; // .unbound()
    private static final String TAG_SET_MAP_FIELD          = "a"; // val$map for PaperMC

    static {
        SERVER = ((CraftServer) Bukkit.getServer()).getServer();
        ENCHANTS = (MappedRegistry<Enchantment>) SERVER.registryAccess().lookup(Registries.ENCHANTMENT).orElseThrow();
        ITEMS = (MappedRegistry<Item>) SERVER.registryAccess().lookup(Registries.ITEM).orElseThrow();
    }

    private final NightPlugin plugin;

    public RegistryHack_1_21_10(@NotNull NightPlugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    private static ResourceLocation customResourceLocation(@NotNull String value) {
        return CraftNamespacedKey.toMinecraft(EnchantsKeys.create(value));
    }

    private static <T> TagKey<T> customTagKey(@NotNull Registry<T> registry, @NotNull String name) {
        return TagKey.create(registry.key(), customResourceLocation(name));
    }

    @NotNull
    private static ResourceKey<Enchantment> customEnchantKey(@NotNull String name) {
        ResourceLocation location = customResourceLocation(name);

        return ResourceKey.create(ENCHANTS.key(), location);
    }

    @NotNull
    private static ResourceKey<Enchantment> enchantKey(@NotNull String name) {
        ResourceLocation location = ResourceLocation.parse(name);

        return ResourceKey.create(ENCHANTS.key(), location);
    }

    private static TagKey<Item> customItemsTag(@NotNull String path) {
        return TagKey.create(ITEMS.key(), customResourceLocation(path));
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private static <T> Map<TagKey<T>, HolderSet.Named<T>> getFrozenTags(@NotNull MappedRegistry<T> registry) {
        return (Map<TagKey<T>, HolderSet.Named<T>>) Reflex.getFieldValue(registry, REGISTRY_FROZEN_TAGS_FIELD);
    }

    @NotNull
    private static <T> Object getAllTags(@NotNull MappedRegistry<T> registry) {
        return Reflex.getFieldValue(registry, REGISTRY_ALL_TAGS_FIELD);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private static <T> Map<TagKey<T>, HolderSet.Named<T>> getTagsMap(@NotNull Object tagSet) {
        // new HashMap, because original is ImmutableMap.
        return new HashMap<>((Map<TagKey<T>, HolderSet.Named<T>>) Reflex.getFieldValue(tagSet, TAG_SET_MAP_FIELD));
    }

    @Override
    public void unfreezeRegistry() {
        unfreeze(ENCHANTS);
        unfreeze(ITEMS);
    }

    @Override
    public void freezeRegistry() {
        freeze(ITEMS);
        freeze(ENCHANTS);
        //this.displayTags();
    }

    private static <T> void unfreeze(@NotNull MappedRegistry<T> registry) {
        Reflex.setFieldValue(registry, "l", false);             // MappedRegistry#frozen
        Reflex.setFieldValue(registry, "m", new IdentityHashMap<>()); // MappedRegistry#unregisteredIntrusiveHolders
    }

    private static <T> void freeze(@NotNull MappedRegistry<T> registry) {
        // Get original TagSet object of the registry before unbound.
        // We MUST keep original TagSet object and only modify an inner map object inside it.
        // Otherwise it will throw an Network Error on client join because of 'broken' tags that were bound to other TagSet object.
        Object tagSet = getAllTags(registry);

        // Get a copy of original TagSet's tags map.
        Map<TagKey<T>, HolderSet.Named<T>> tagsMap = getTagsMap(tagSet);
        // Get 'frozenTags' map with all tags of the registry.
        Map<TagKey<T>, HolderSet.Named<T>> frozenTags = getFrozenTags(registry);

        // Here we add all registered and bound vanilla tags to the 'frozenTags' map for further freeze & bind.
        // For some reason 'frozenTags' map does not contain all the tags, so some of them will be absent if not added back here
        // and result in broken gameplay features.
        tagsMap.forEach(frozenTags::putIfAbsent);

        // We MUST 'unbound' the registry tags to be able to call .freeze() method of it.
        // Otherwise it will throw an error saying tags are not bound.
        unbound(registry);

        // This method will register all tags from the 'frozenTags' map and assign a new TagSet object to the 'allTags' field of registry.
        // But we MUST replace the 'allTags' field value with the original (before unbound) TagSet object to prevent Network Error for clients.
        registry.freeze();

        // Here we need to put in 'tagsMap' map of TagSet object all new/custom registered tags.
        // Otherwise it will cause Network Error because custom tags are not present in the TagSet tags map.
        frozenTags.forEach(tagsMap::putIfAbsent);

        // Update inner tags map of the TagSet object that is 'allTags' field of the registry.
        Reflex.setFieldValue(tagSet, TAG_SET_MAP_FIELD, tagsMap);
        // Assign original TagSet object with modified tags map to the 'allTags' field of the registry.
        Reflex.setFieldValue(registry, REGISTRY_ALL_TAGS_FIELD, tagSet);
    }

    private static <T> void unbound(@NotNull MappedRegistry<T> registry) {
        Class<?> tagSetClass = Reflex.safeInnerClass(MappedRegistry.class.getName(), "a");  // TagSet for PaperMC

        Method unboundMethod = Reflex.safeMethod(tagSetClass, TAG_SET_UNBOUND_METHOD);
        Object unboundTagSet = Reflex.invokeMethod(unboundMethod, registry); // new TagSet object.

        Reflex.setFieldValue(registry, REGISTRY_ALL_TAGS_FIELD, unboundTagSet);
    }

    @Override
    public void addExclusives(@NotNull CustomEnchantment customEnchantment) {
        ResourceKey<Enchantment> enchantKey = customEnchantKey(customEnchantment.getId());
        Enchantment enchantment = ENCHANTS.getValue(enchantKey);
        if (enchantment == null) {
            this.plugin.error(customEnchantment.getId() + ": Could not set exclusive item list. Enchantment is not registered.");
            return;
        }

        TagKey<Enchantment> exclusivesKey = customTagKey(ENCHANTS, "exclusive_set/" + customEnchantment.getId());

        customEnchantment.getDefinition().getExclusiveSet().forEach(enchantId -> {
            ResourceKey<Enchantment> conflictKey = enchantKey(enchantId);
            Holder.Reference<Enchantment> reference = ENCHANTS.get(conflictKey).orElse(null);
            if (reference == null) return;

            addInTag(exclusivesKey, reference);
        });
    }

    @Override
    @Nullable
    public org.bukkit.enchantments.Enchantment registerEnchantment(@NotNull EnchantCatalogEntry entry, @NotNull DistributionSettings settings) {
        String id = entry.getId();
        EnchantDefinition definition = entry.getDefinition();

        String primaryId = definition.getPrimaryItemSet().getId();
        String supportedId = definition.getSupportedItemSet().getId();

        HolderSet.Named<Item> supportedItems = getFrozenTags(ITEMS).get(customItemsTag(supportedId));
        HolderSet.Named<Item> primaryItems = getFrozenTags(ITEMS).get(customItemsTag(primaryId));

        Component display = CraftChatMessage.fromJSON(NightMessage.asJson(definition.getDisplayName()));
        int weight = definition.getWeight();
        int maxLevel = definition.getMaxLevel();
        Enchantment.Cost minCost = nmsCost(definition.getMinCost());
        Enchantment.Cost maxCost = nmsCost(definition.getMaxCost());
        int anvilCost = definition.getAnvilCost();
        EquipmentSlotGroup[] slots = nmsSlots(definition.getSupportedItemSet().getSlots());

        Enchantment.EnchantmentDefinition nmsDefinition = Enchantment.definition(supportedItems, primaryItems, weight, maxLevel, minCost, maxCost, anvilCost, slots);
        HolderSet<Enchantment> exclusiveSet = this.createExclusiveSet(id);
        DataComponentMap.Builder builder = DataComponentMap.builder();

        Enchantment enchantment = new Enchantment(display, nmsDefinition, exclusiveSet, builder.build());
        // Create a new Holder for the custom enchantment.
        Holder.Reference<Enchantment> reference = ENCHANTS.createIntrusiveHolder(enchantment);

        // Add it into Registry.
        Registry.register(ENCHANTS, customEnchantKey(id), enchantment);

        // Now it's possible to add/remove it from vanilla tags since we have a valid, registered Reference.
        this.setupDistribution(entry, settings, reference);

        // Return the bukkit mirror.
        return CraftEnchantment.minecraftToBukkit(enchantment);
    }

    private void setupDistribution(@NotNull EnchantCatalogEntry entry, @NotNull DistributionSettings settings, @NotNull Holder.Reference<Enchantment> reference) {
        EnchantDistribution distribution = entry.getDistribution();
        boolean experimentalTrades = SERVER.getWorldData().enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE);

        // Any enchantment can be treasure.
        if (distribution.isTreasure()) {
            addInTag(EnchantmentTags.TREASURE, reference);
            addInTag(EnchantmentTags.DOUBLE_TRADE_PRICE, reference);
        }
        else addInTag(EnchantmentTags.NON_TREASURE, reference);

        // Any enchantment can be on random loot.
        if (distribution.isOnRandomLoot() && settings.isRandomLootEnabled()) {
            addInTag(EnchantmentTags.ON_RANDOM_LOOT, reference);
        }

        // Only non-treasure enchantments should be on mob equipment, traded equipment and non-rebalanced trades.
        if (!distribution.isTreasure()) {
            if (distribution.isOnMobSpawnEquipment() && settings.isMobEquipmentEnabled()) {
                addInTag(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT, reference);
            }

            if (distribution.isOnTradedEquipment() && settings.isTradeEquipmentEnabled()) {
                addInTag(EnchantmentTags.ON_TRADED_EQUIPMENT, reference);
            }
        }

        // Any enchantment can be tradable.
        if (experimentalTrades) {
            if (distribution.isTradable() && settings.isTradingEnabled()) {
                distribution.getTrades().forEach(tradeType -> {
                    addInTag(getTradeKey(tradeType), reference);
                });
            }
        }
        else {
            if (distribution.isTradable() && settings.isTradingEnabled()) {
                addInTag(EnchantmentTags.TRADEABLE, reference);
            }
            else removeFromTag(EnchantmentTags.TRADEABLE, reference);
        }

        if (entry.isCurse()) {
            addInTag(EnchantmentTags.CURSE, reference);
        }
        else {
            // Only non-curse and non-treasure enchantments should go in enchanting table.
            if (!distribution.isTreasure()) {
                if (distribution.isDiscoverable() && settings.isEnchantingEnabled()) {
                    addInTag(EnchantmentTags.IN_ENCHANTING_TABLE, reference);
                }
                else removeFromTag(EnchantmentTags.IN_ENCHANTING_TABLE, reference);
            }
        }
    }

    private void addInTag(@NotNull TagKey<Enchantment> tagKey, @NotNull Holder.Reference<Enchantment> reference) {
        modfiyTag(ENCHANTS, tagKey, reference, List::add);
    }

    private void removeFromTag(@NotNull TagKey<Enchantment> tagKey, @NotNull Holder.Reference<Enchantment> reference) {
        modfiyTag(ENCHANTS, tagKey, reference, List::remove);
    }

    private <T> void modfiyTag(@NotNull MappedRegistry<T> registry,
                               @NotNull TagKey<T> tagKey,
                               @NotNull Holder.Reference<T> reference,
                               @NotNull BiConsumer<List<Holder<T>>, Holder.Reference<T>> consumer) {

        HolderSet.Named<T> holders = registry.get(tagKey).orElse(null);
        if (holders == null) {
            this.plugin.warn(tagKey + ": Could not modify HolderSet. HolderSet is NULL.");
            return;
        }

        List<Holder<T>> contents = new ArrayList<>(holders.stream().toList());
        consumer.accept(contents, reference);

        registry.bindTag(tagKey, contents);
    }

    @Override
    public void createItemsSet(@NotNull ItemSet itemSet) {
        TagKey<Item> tag = customItemsTag(itemSet.getId());
        List<Holder<Item>> holders = new ArrayList<>();

        itemSet.getMaterials().forEach(material -> {
            ResourceLocation location = ResourceLocation.withDefaultNamespace(material);// CraftNamespacedKey.toMinecraft(material.getKey());
            Holder.Reference<Item> holder = ITEMS.get(location).orElse(null);
            if (holder == null) return;

            holders.add(holder);
        });

        // Creates new tag, puts it in the 'frozenTags' map and binds holders to it.
        ITEMS.bindTag(tag, holders);

        //return getFrozenTags(ITEMS).get(customKey);
    }

    @NotNull
    private HolderSet.Named<Enchantment> createExclusiveSet(@NotNull String id) {
        TagKey<Enchantment> customKey = customTagKey(ENCHANTS, "exclusive_set/" + id);
        List<Holder<Enchantment>> holders = new ArrayList<>();

        // Creates new tag, puts it in the 'frozenTags' map and binds holders to it.
        ENCHANTS.bindTag(customKey, holders);

        return getFrozenTags(ENCHANTS).get(customKey);
    }




    @NotNull
    private static TagKey<Enchantment> getTradeKey(@NotNull TradeType tradeType) {
        return switch (tradeType) {
            case DESERT_COMMON -> EnchantmentTags.TRADES_DESERT_COMMON;
            case DESERT_SPECIAL -> EnchantmentTags.TRADES_DESERT_SPECIAL;
            case PLAINS_COMMON -> EnchantmentTags.TRADES_PLAINS_COMMON;
            case PLAINS_SPECIAL -> EnchantmentTags.TRADES_PLAINS_SPECIAL;
            case SAVANNA_COMMON -> EnchantmentTags.TRADES_SAVANNA_COMMON;
            case SAVANNA_SPECIAL -> EnchantmentTags.TRADES_SAVANNA_SPECIAL;
            case JUNGLE_COMMON -> EnchantmentTags.TRADES_JUNGLE_COMMON;
            case JUNGLE_SPECIAL -> EnchantmentTags.TRADES_JUNGLE_SPECIAL;
            case SNOW_COMMON -> EnchantmentTags.TRADES_SNOW_COMMON;
            case SNOW_SPECIAL -> EnchantmentTags.TRADES_SNOW_SPECIAL;
            case SWAMP_COMMON -> EnchantmentTags.TRADES_SWAMP_COMMON;
            case SWAMP_SPECIAL -> EnchantmentTags.TRADES_SWAMP_SPECIAL;
            case TAIGA_COMMON -> EnchantmentTags.TRADES_TAIGA_COMMON;
            case TAIGA_SPECIAL -> EnchantmentTags.TRADES_TAIGA_SPECIAL;
        };
    }

    @NotNull
    private static Enchantment.Cost nmsCost(@NotNull EnchantCost cost) {
        return new Enchantment.Cost(cost.base(), cost.perLevel());
    }

    private static EquipmentSlotGroup[] nmsSlots(@NotNull EquipmentSlot[] slots) {
        EquipmentSlotGroup[] nmsSlots = new EquipmentSlotGroup[slots.length];

        for (int index = 0; index < nmsSlots.length; index++) {
            EquipmentSlot bukkitSlot = slots[index];
            nmsSlots[index] = CraftEquipmentSlot.getNMSGroup(bukkitSlot.getGroup());
        }

        return nmsSlots;
    }
}
