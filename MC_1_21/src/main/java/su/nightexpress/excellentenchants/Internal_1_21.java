package su.nightexpress.excellentenchants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_21_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_21_R1.block.CraftBlockType;
import org.bukkit.craftbukkit.v1_21_R1.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftFishHook;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_21_R1.util.CraftNamespacedKey;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.*;
import su.nightexpress.excellentenchants.api.enchantment.bridge.FlameWalker;
import su.nightexpress.excellentenchants.nms.EnchantNMS;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.util.Reflex;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.util.*;
import java.util.function.BiConsumer;

public class Internal_1_21 implements EnchantNMS {

    private static final MinecraftServer       SERVER;
    private static final Registry<Enchantment> ENCHANTMENT_REGISTRY;
    private static final Registry<Item>        ITEM_REGISTRY;

//    private static final String NETTY_NAME = "excellent_enchants_handler";
//    private static final String HANDLER_NAME = "EEPackets";

    private static final String HOLDER_SET_NAMED_CONTENTS_FIELD  = "c"; // 'contents' field of the HolderSet.Named
    private static final String HOLDER_SET_DIRECT_CONTENTS_FIELD = "b"; // 'contents' field of the HolderSet.Direct
    private static final String HOLDER_REFERENCE_TAGS_FIELD      = "b"; // 'tags' field of the Holder.Reference

    static {
        SERVER = ((CraftServer)Bukkit.getServer()).getServer();
        ENCHANTMENT_REGISTRY = SERVER.registryAccess().registry(Registries.ENCHANTMENT).orElse(null);
        ITEM_REGISTRY = SERVER.registryAccess().registry(Registries.ITEM).orElseThrow();
    }

    private final NightPlugin plugin;

    public Internal_1_21(@NotNull NightPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void unfreezeRegistry() {
        Reflex.setFieldValue(ENCHANTMENT_REGISTRY, "l", false);             // MappedRegistry#frozen
        Reflex.setFieldValue(ENCHANTMENT_REGISTRY, "m", new IdentityHashMap<>()); // MappedRegistry#unregisteredIntrusiveHolders
    }

    @Override
    public void freezeRegistry() {
        ENCHANTMENT_REGISTRY.freeze();
    }

    //HolderLookup.RegistryLookup<Enchantment> enchantRegistry = minecraftServer.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    //VanillaRegistries.createLookup().lookup(Registries.ENCHANTMENT).get();

    // Create Enchantment reference.
    //Holder.Reference<Enchantment> reference = Holder.Reference.createStandAlone(ENCHANTMENT_REGISTRY.holderOwner(), key);
    // Bind enchantment value to the reference (or it will be null).
    //Reflex.setFieldValue(reference, "e", enchantment);

    @NotNull
    private static ResourceKey<Enchantment> createKey(@NotNull String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.withDefaultNamespace(name));
    }

    @Override
    public void addExclusives(@NotNull CustomEnchantment customEnchantment) {
        Enchantment enchantment = ENCHANTMENT_REGISTRY.get(createKey(customEnchantment.getId()));
        if (enchantment == null) {
            this.plugin.error(customEnchantment.getId() + ": Could not set exclusive item list. Enchantment is not registered.");
            return;
        }

        HolderSet<Enchantment> exclusiveSet = enchantment.exclusiveSet();
        List<Holder<Enchantment>> contents = new ArrayList<>();

        customEnchantment.getDefinition().getConflicts().forEach(enchantId -> {
            ResourceKey<Enchantment> key = createKey(enchantId);
            Holder.Reference<Enchantment> reference = ENCHANTMENT_REGISTRY.getHolder(key).orElse(null);
            if (reference == null) return;

            contents.add(reference);
        });

        Reflex.setFieldValue(exclusiveSet, HOLDER_SET_DIRECT_CONTENTS_FIELD, contents);
    }

    @Override
    @NotNull
    public org.bukkit.enchantments.Enchantment registerEnchantment(@NotNull CustomEnchantment customEnchantment) {
        Definition customDefinition = customEnchantment.getDefinition();

        Component display = CraftChatMessage.fromJSON(NightMessage.asJson(customEnchantment.getFormattedName()));
        HolderSet.Named<Item> supportedItems = createItemSet("enchant_supported", customEnchantment, customDefinition.getSupportedItems());
        HolderSet.Named<Item> primaryItems = createItemSet("enchant_primary", customEnchantment, customDefinition.getPrimaryItems());
        int weight = customDefinition.getRarity().getWeight();
        int maxLevel = customDefinition.getMaxLevel();
        Enchantment.Cost minCost = nmsCost(customDefinition.getMinCost());
        Enchantment.Cost maxCost = nmsCost(customDefinition.getMaxCost());
        int anvilCost = customDefinition.getAnvilCost();
        EquipmentSlotGroup[] slots = nmsSlots(customDefinition);

        Enchantment.EnchantmentDefinition definition = Enchantment.definition(supportedItems, primaryItems, weight, maxLevel, minCost, maxCost, anvilCost, slots);
        HolderSet<Enchantment> exclusiveSet = HolderSet.direct();
        DataComponentMap.Builder builder = DataComponentMap.builder();

        Enchantment enchantment = new Enchantment(display, definition, exclusiveSet, builder.build());

        // Create a new Holder for the custom enchantment.
        Holder.Reference<Enchantment> reference = ENCHANTMENT_REGISTRY.createIntrusiveHolder(enchantment);

        // Add it into Registry.
        Registry.register(ENCHANTMENT_REGISTRY, customEnchantment.getId(), enchantment);

        // Now it's possible to add/remove it from vanilla tags since we have a valid, registered Reference.
        this.setupDistribution(customEnchantment, reference);

        // Return the bukkit mirror.
        return CraftEnchantment.minecraftToBukkit(enchantment);
    }

//    public void displayTags() {
//        displayTag(EnchantmentTags.CURSE);
//        displayTag(EnchantmentTags.TREASURE);
//        displayTag(EnchantmentTags.NON_TREASURE);
//        displayTag(EnchantmentTags.IN_ENCHANTING_TABLE);
//        displayTag(EnchantmentTags.DOUBLE_TRADE_PRICE);
//        displayTag(EnchantmentTags.ON_TRADED_EQUIPMENT);
//        displayTag(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT);
//        displayTag(EnchantmentTags.ON_RANDOM_LOOT);
//        displayTag(EnchantmentTags.ARMOR_EXCLUSIVE);
//        displayTag(EnchantmentTags.TRADEABLE);
//    }
//
//    public void displayTag(TagKey<Enchantment> tagKey) {
//        ENCHANTMENT_REGISTRY.getTag(tagKey).ifPresent(holders -> {
//            System.out.println(tagKey + ": " + holders.stream().map(Holder::value).toList());
//        });
//        System.out.println(" ");
//    }

    private void setupDistribution(@NotNull CustomEnchantment enchantment, @NotNull Holder.Reference<Enchantment> reference) {
        boolean experimentalTrades = SERVER.getWorldData().enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE);
        Distribution distribution = enchantment.getDistribution();

        // Any enchantment can be treasure.
        if (distribution.isTreasure()) {
            addInTag(EnchantmentTags.TREASURE, reference);
            addInTag(EnchantmentTags.DOUBLE_TRADE_PRICE, reference);
        }
        else addInTag(EnchantmentTags.NON_TREASURE, reference);

        // Any enchantment can be on random loot.
        if (distribution.isOnRandomLoot()) {
            addInTag(EnchantmentTags.ON_RANDOM_LOOT, reference);
        }

        // Only non-treasure enchantments should be on mob equipment, traded equipment and non-rebalanced trades.
        if (!distribution.isTreasure()) {
            if (distribution.isOnMobSpawnEquipment()) {
                addInTag(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT, reference);
            }

            if (distribution.isOnTradedEquipment()) {
                addInTag(EnchantmentTags.ON_TRADED_EQUIPMENT, reference);
            }

            if (!experimentalTrades) {
                if (distribution.isTradable()) {
                    addInTag(EnchantmentTags.TRADEABLE, reference);
                }
                else removeFromTag(EnchantmentTags.TRADEABLE, reference);
            }
        }

        // Any enchantment can be on rebalanced trades.
        if (experimentalTrades && distribution.isTradable()) {
            distribution.getTrades().forEach(tradeType -> {
                addInTag(getTradeKey(tradeType), reference);
            });
        }

        if (enchantment.isCurse()) {
            addInTag(EnchantmentTags.CURSE, reference);
        }
        else {
            // Only non-curse and non-treasure enchantments should go in enchanting table.
            if (!distribution.isTreasure()) {
                if (distribution.isDiscoverable()) {
                    addInTag(EnchantmentTags.IN_ENCHANTING_TABLE, reference);
                }
                else removeFromTag(EnchantmentTags.IN_ENCHANTING_TABLE, reference);
            }
        }
    }

    private void addInTag(@NotNull TagKey<Enchantment> tagKey, @NotNull Holder.Reference<Enchantment> reference) {
        modfiyTag(ENCHANTMENT_REGISTRY, tagKey, reference, List::add);
    }

    private void removeFromTag(@NotNull TagKey<Enchantment> tagKey, @NotNull Holder.Reference<Enchantment> reference) {
        modfiyTag(ENCHANTMENT_REGISTRY, tagKey, reference, List::remove);
    }

    @SuppressWarnings("unchecked")
    private <T> void modfiyTag(@NotNull Registry<T> registry,
                               @NotNull TagKey<T> tagKey,
                               @NotNull Holder.Reference<T> reference,
                               @NotNull BiConsumer<List<Holder<T>>, Holder.Reference<T>> consumer) {

        HolderSet.Named<T> holders = registry.getTag(tagKey).orElse(null);
        if (holders == null) {
            this.plugin.warn(tagKey + ": Could not modify HolderSet. HolderSet is NULL.");
            return;
        }

        // We must use reflection to get a Holder list from the HolderSet and make it mutable.
        List<Holder<T>> contents = new ArrayList<>((List<Holder<T>>) Reflex.getFieldValue(holders, HOLDER_SET_NAMED_CONTENTS_FIELD));

        // Do something with it.
        consumer.accept(contents, reference);

        // Assign it back to the HolderSet.
        Reflex.setFieldValue(holders, HOLDER_SET_NAMED_CONTENTS_FIELD, contents);
    }

    @SuppressWarnings("unchecked")
    private static HolderSet.Named<Item> createItemSet(@NotNull String prefix, @NotNull CustomEnchantment data, @NotNull ItemsCategory category) {
        TagKey<Item> customKey = TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace(prefix + "/" + data.getId()));
        HolderSet.Named<Item> customItems = ITEM_REGISTRY.getOrCreateTag(customKey);
        List<Holder<Item>> holders = new ArrayList<>();

        category.getMaterials().forEach(material -> {
            ResourceLocation location = CraftNamespacedKey.toMinecraft(material.getKey());
            Holder.Reference<Item> holder = ITEM_REGISTRY.getHolder(location).orElse(null);
            if (holder == null) return;

            // We must reassign the 'tags' field value because of the HolderSet#contains(Holder<T> holder) behavior.
            // It checks if Holder.Reference.is(this.key) -> Holder.Reference.tags.contains(key). Where 'key' is our custom key created above.
            // So, even if our HolderSet content is filled with items, we have to include their tag to the actual items in registry.
            Set<TagKey<Item>> holderTags = new HashSet<>((Set<TagKey<Item>>) Reflex.getFieldValue(holder, HOLDER_REFERENCE_TAGS_FIELD));
            holderTags.add(customKey);
            Reflex.setFieldValue(holder, HOLDER_REFERENCE_TAGS_FIELD, holderTags);

            holders.add(holder);
        });

        Reflex.setFieldValue(customItems, HOLDER_SET_NAMED_CONTENTS_FIELD, holders);

        return customItems;
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
    private static Enchantment.Cost nmsCost(@NotNull Cost cost) {
        return new Enchantment.Cost(cost.base(), cost.perLevel());
    }

    @SuppressWarnings("UnstableApiUsage")
    private static EquipmentSlotGroup[] nmsSlots(@NotNull Definition definition) {
        EquipmentSlot[] slots = definition.getSupportedItems().getSlots();
        EquipmentSlotGroup[] nmsSlots = new EquipmentSlotGroup[slots.length];

        for (int index = 0; index < nmsSlots.length; index++) {
            EquipmentSlot bukkitSlot = slots[index];
            nmsSlots[index] = CraftEquipmentSlot.getNMSGroup(bukkitSlot.getGroup());
        }

        return nmsSlots;
    }

    @Override
    public void sendAttackPacket(@NotNull Player player, int id) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerPlayer entity = craftPlayer.getHandle();
        ClientboundAnimatePacket packet = new ClientboundAnimatePacket(entity, id);
        craftPlayer.getHandle().connection.send(packet);

        player.spigot().sendMessage();
    }

    @Override
    public void retrieveHook(@NotNull FishHook hook, @NotNull ItemStack item, @NotNull EquipmentSlot slot) {
        CraftFishHook craftFishHook = (CraftFishHook) hook;
        FishingHook handle = craftFishHook.getHandle();

        net.minecraft.world.entity.player.Player owner = handle.getPlayerOwner();
        if (owner == null) return;

        int result = handle.retrieve(CraftItemStack.asNMSCopy(item));

        net.minecraft.world.entity.EquipmentSlot hand = slot == EquipmentSlot.HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND;

        net.minecraft.world.item.ItemStack itemStack = owner.getItemBySlot(hand);
        if (itemStack == null) return;

        itemStack.hurtAndBreak(result, handle.getPlayerOwner(), hand);
    }

    @NotNull
    @Override
    public Material getItemBlockVariant(@NotNull Material material) {
        ItemStack itemStack = new ItemStack(material);
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        if (nmsStack.getItem() instanceof BlockItem blockItem) {
            return CraftBlockType.minecraftToBukkit(blockItem.getBlock());
        }
        return material;
    }

    @Override
    public boolean handleFlameWalker(@NotNull FlameWalker flameWalker, @NotNull LivingEntity bukkitEntity, int level) {
        Entity entity = ((CraftLivingEntity) bukkitEntity).getHandle();
        BlockPos pos = entity.blockPosition();
        Level world = entity.level();

        int radius = (int) flameWalker.getRadius().getValue(level);
        BlockState magmaState = Blocks.MAGMA_BLOCK.defaultBlockState();
        BlockPos.MutableBlockPos posAbove = new BlockPos.MutableBlockPos();

        Set<Block> blocks = new HashSet<>();
        for (BlockPos posNear : BlockPos.betweenClosed(pos.offset(-radius, -1, -radius), pos.offset(radius, -1, radius))) {
            if (!posNear.closerThan(entity.blockPosition(), radius)) continue;

            posAbove.set(posNear.getX(), posNear.getY() + 1, posNear.getZ());

            BlockState aboveLava = world.getBlockState(posAbove);
            BlockState lavaState = world.getBlockState(posNear);

            if (!aboveLava.isAir()) continue;
            if (!lavaState.getBlock().equals(Blocks.LAVA)) continue;
            if (lavaState.getValue(LiquidBlock.LEVEL) != 0) continue;
            if (!magmaState.canSurvive(world, posNear)) continue;
            if (!world.isUnobstructed(magmaState, posNear, CollisionContext.empty())) continue;
            if (!CraftEventFactory.handleBlockFormEvent(world, posNear, magmaState, entity)) continue;

            blocks.add(CraftBlock.at(world, posNear));
        }

        blocks.forEach(block -> FlameWalker.addBlock(block, Rnd.getDouble(flameWalker.getBlockDecayTime(level)) + 1));

        return !blocks.isEmpty();
    }

    @NotNull
    public org.bukkit.entity.Item popResource(@NotNull Block block, @NotNull ItemStack item) {
        Level world = ((CraftWorld)block.getWorld()).getHandle();
        BlockPos pos = ((CraftBlock)block).getPosition();
        net.minecraft.world.item.ItemStack itemstack = CraftItemStack.asNMSCopy(item);

        float yMod = EntityType.ITEM.getHeight() / 2.0F;
        double x = (pos.getX() + 0.5F) + Mth.nextDouble(world.random, -0.25D, 0.25D);
        double y = (pos.getY() + 0.5F) + Mth.nextDouble(world.random, -0.25D, 0.25D) - yMod;
        double z = (pos.getZ() + 0.5F) + Mth.nextDouble(world.random, -0.25D, 0.25D);

        ItemEntity itemEntity = new ItemEntity(world, x, y, z, itemstack);
        itemEntity.setDefaultPickUpDelay();
        return (org.bukkit.entity.Item) itemEntity.getBukkitEntity();
    }

//    public void addPacketListener(@NotNull Player player) {
//        ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
//        Connection connection = (Connection) Reflex.getFieldValue(serverPlayer.connection, "connection");
//
//        connection.channel.pipeline().addBefore(NETTY_NAME, HANDLER_NAME, new ChannelDuplexHandler() {
//            @Override
//            public void write(ChannelHandlerContext context, Object packet, ChannelPromise promise) throws Exception {
//                if (packet instanceof ClientboundContainerSetSlotPacket slotPacket) {
//
//                }
//                super.write(context, packet, promise);
//            }
//
//            @Override
//            public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
//                super.channelRead(context, packet);
//            }
//        });
//    }
//
//    public void removePacketListener(@NotNull Player player) {
//        ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
//        Connection connection = (Connection) Reflex.getFieldValue(serverPlayer.connection, "connection");
//
//        ChannelPipeline pipeline = connection.channel.pipeline();
//        if (pipeline.get(HANDLER_NAME) != null) {
//            pipeline.remove(HANDLER_NAME);
//        }
//    }
}