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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.api.enchantment.ItemsCategory;
import su.nightexpress.excellentenchants.api.enchantment.distribution.VanillaOptions;
import su.nightexpress.excellentenchants.nms.EnchantNMS;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.util.Reflex;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;

public class Internal_1_21 implements EnchantNMS {

    private static final MinecraftServer SERVER;
    private static final Registry<Enchantment> ENCHANTMENT_REGISTRY;

    private static final String HOLDER_SET_NAMED_CONTENTS_FIELD = "c"; // 'contents' field of the HolderSet.Named
    private static final String HOLDER_SET_DIRECT_CONTENTS_FIELD = "b"; // 'contents' field of the HolderSet.Direct
    private static final String HOLDER_REFERENCE_TAGS_FIELD = "b"; // 'tags' field of the Holder.Reference

    static {
        SERVER = ((CraftServer)Bukkit.getServer()).getServer();
        ENCHANTMENT_REGISTRY = SERVER.registryAccess().registry(Registries.ENCHANTMENT).orElse(null);
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

    @Override
    public boolean isEnchantable(@NotNull ItemStack bukkitItem) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(bukkitItem);
        return nmsItem.getItem().isEnchantable(nmsItem);
    }

    //HolderGetter<Item> itemRegistry = SERVER.registryAccess().lookupOrThrow(Registries.ITEM);
    //HolderLookup.RegistryLookup<Enchantment> enchantRegistry = minecraftServer.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    //VanillaRegistries.createLookup().lookup(Registries.ENCHANTMENT).get();

    @Override
    public void addExclusives(@NotNull EnchantmentData data) {
        Enchantment enchantment = ENCHANTMENT_REGISTRY.get(key(data.getId()));
        if (enchantment == null) {
            this.plugin.error(data.getId() + ": Could not set exclusive item list. Enchantment is not registered.");
            return;
        }

        HolderSet<Enchantment> exclusiveSet = enchantment.exclusiveSet();
        List<Holder<Enchantment>> contents = new ArrayList<>();

        data.getConflicts().forEach(enchantId -> {
            ResourceKey<Enchantment> key = key(enchantId);
            Holder.Reference<Enchantment> reference = ENCHANTMENT_REGISTRY.getHolder(key).orElse(null);
            if (reference == null) return;

            contents.add(reference);
        });

        Reflex.setFieldValue(exclusiveSet, HOLDER_SET_DIRECT_CONTENTS_FIELD, contents);
    }

    public void registerEnchantment(@NotNull EnchantmentData data) {
        ResourceKey<Enchantment> key = key(data.getId());

        Component component = CraftChatMessage.fromJSON(NightMessage.asJson(data.getName()));
        HolderSet.Named<Item> supportedItems = createItemSet("enchant_supported", data, data.getSupportedItems());
        HolderSet.Named<Item> primaryItems = createItemSet("enchant_primary", data, data.getPrimaryItems());
        int weight = data.getRarity().getWeight();
        int maxLevel = data.getMaxLevel();
        Enchantment.Cost minCost = new Enchantment.Cost(data.getMinCost().base(), data.getMinCost().perLevel());
        Enchantment.Cost maxCost = new Enchantment.Cost(data.getMaxCost().base(), data.getMaxCost().perLevel());
        int anvilCost = data.getAnvilCost();
        net.minecraft.world.entity.EquipmentSlotGroup[] slots = nmsSlots(data);

        Enchantment.EnchantmentDefinition definition = Enchantment.definition(supportedItems, primaryItems, weight, maxLevel, minCost, maxCost, anvilCost, slots);
        HolderSet<Enchantment> exclusiveSet = HolderSet.direct();

        Enchantment enchantment = new Enchantment(component, definition, exclusiveSet, DataComponentMap.builder().build());

        Holder.Reference<Enchantment> reference = ENCHANTMENT_REGISTRY.createIntrusiveHolder(enchantment);
        Registry.register(ENCHANTMENT_REGISTRY, data.getId(), enchantment);

        boolean experimentalTrades = SERVER.getWorldData().enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE);
        // TODO More tags Random Loot, Mob Equipment, etc.
        if (data.isCurse()) {
            addInTag(EnchantmentTags.CURSE, reference);
        }
        else {
            if (data.isTreasure()) {
                addInTag(EnchantmentTags.TREASURE, reference);
            }
            else addInTag(EnchantmentTags.NON_TREASURE, reference);

            if (data.getDistributionOptions() instanceof VanillaOptions vanillaOptions) {
                if (vanillaOptions.isTradeable()) {
                    if (experimentalTrades) {
                        addInTag(EnchantmentTags.TRADES_DESERT_COMMON, reference);
                        addInTag(EnchantmentTags.TRADES_JUNGLE_COMMON, reference);
                        addInTag(EnchantmentTags.TRADES_PLAINS_COMMON, reference);
                        addInTag(EnchantmentTags.TRADES_SAVANNA_COMMON, reference);
                        addInTag(EnchantmentTags.TRADES_SNOW_COMMON, reference);
                        addInTag(EnchantmentTags.TRADES_SWAMP_COMMON, reference);
                        addInTag(EnchantmentTags.TRADES_TAIGA_COMMON, reference);
                        addInTag(EnchantmentTags.TRADES_DESERT_SPECIAL, reference);
                        addInTag(EnchantmentTags.TRADES_JUNGLE_SPECIAL, reference);
                        addInTag(EnchantmentTags.TRADES_PLAINS_SPECIAL, reference);
                        addInTag(EnchantmentTags.TRADES_SAVANNA_SPECIAL, reference);
                        addInTag(EnchantmentTags.TRADES_SNOW_SPECIAL, reference);
                        addInTag(EnchantmentTags.TRADES_SWAMP_SPECIAL, reference);
                        addInTag(EnchantmentTags.TRADES_TAIGA_SPECIAL, reference);
                    }
                    else {
                        addInTag(EnchantmentTags.TRADEABLE, reference);
                    }
                }
                else removeFromTag(EnchantmentTags.TRADEABLE, reference);

                if (vanillaOptions.isDiscoverable()) {
                    addInTag(EnchantmentTags.IN_ENCHANTING_TABLE, reference);
                }
                else removeFromTag(EnchantmentTags.IN_ENCHANTING_TABLE, reference);
            }
        }

        org.bukkit.enchantments.Enchantment bukkitEnchant = CraftEnchantment.minecraftToBukkit(enchantment);
        data.setEnchantment(bukkitEnchant);
    }

    private void addInTag(@NotNull TagKey<Enchantment> tagKey, @NotNull Holder.Reference<Enchantment> reference) {
        modfiyTag(tagKey, reference, List::add);
    }

    private void removeFromTag(@NotNull TagKey<Enchantment> tagKey, @NotNull Holder.Reference<Enchantment> reference) {
        modfiyTag(tagKey, reference, List::remove);
    }

    private void modfiyTag(@NotNull TagKey<Enchantment> tagKey,
                                  @NotNull Holder.Reference<Enchantment> reference,
                                  @NotNull BiConsumer<List<Holder<Enchantment>>, Holder.Reference<Enchantment>> consumer) {
        // Get HolderSet of the TagKey
        HolderSet.Named<Enchantment> holders = ENCHANTMENT_REGISTRY.getTag(tagKey).orElse(null);
        if (holders == null) {
            this.plugin.warn(tagKey + ": Could not modify HolderSet. HolderSet is NULL.");
            return;
        }

        // Create Enchantment reference.
            //Holder.Reference<Enchantment> reference = Holder.Reference.createStandAlone(ENCHANTMENT_REGISTRY.holderOwner(), key);
        // Bind enchantment value to the reference (or it will be null).
            //Reflex.setFieldValue(reference, "e", enchantment);

        modfiyHolderSetContents(holders, reference, consumer);
    }

    @SuppressWarnings("unchecked")
    private static <T> void modfiyHolderSetContents(@NotNull HolderSet.Named<T> holders,
                                                    @NotNull Holder.Reference<T> reference,
                                                    @NotNull BiConsumer<List<Holder<T>>, Holder.Reference<T>> consumer) {

        // We must use reflection to get a mutable Holder list from the HolderSet.
        List<Holder<T>> contents = new ArrayList<>((List<Holder<T>>) Reflex.getFieldValue(holders, HOLDER_SET_NAMED_CONTENTS_FIELD));
        // Do something with it.
        consumer.accept(contents, reference);
        // Assign it back to the HolderSet.
        Reflex.setFieldValue(holders, HOLDER_SET_NAMED_CONTENTS_FIELD, contents);
    }

    @SuppressWarnings("unchecked")
    private static HolderSet.Named<Item> createItemSet(@NotNull String prefix, @NotNull EnchantmentData data, @NotNull ItemsCategory category) {
        Registry<Item> items = SERVER.registryAccess().registry(Registries.ITEM).orElseThrow();
        TagKey<Item> customKey = TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace(prefix + "/" + data.getId()));
        HolderSet.Named<Item> customItems = items.getOrCreateTag(customKey);
        List<Holder<Item>> holders = new ArrayList<>();

        category.getMaterials().forEach(material -> {
            ResourceLocation location = CraftNamespacedKey.toMinecraft(material.getKey());
            Holder.Reference<Item> holder = items.getHolder(location).orElse(null);
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

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.withDefaultNamespace(name));
    }

    @SuppressWarnings("UnstableApiUsage")
    public static net.minecraft.world.entity.EquipmentSlotGroup[] nmsSlots(@NotNull EnchantmentData data) {
        org.bukkit.inventory.EquipmentSlot[] slots = data.getSupportedItems().getSlots();
        net.minecraft.world.entity.EquipmentSlotGroup[] nmsSlots = new net.minecraft.world.entity.EquipmentSlotGroup[slots.length];

        for (int index = 0; index < nmsSlots.length; index++) {
            org.bukkit.inventory.EquipmentSlot bukkitSlot = slots[index];
            nmsSlots[index] = CraftEquipmentSlot.getNMSGroup(bukkitSlot.getGroup());
        }

        return nmsSlots;
    }

    private static final Method GET_ENCHANTS_LIST = Reflex.getMethod(EnchantmentMenu.class, "a", net.minecraft.world.item.ItemStack.class, Integer.TYPE, Integer.TYPE);

    @NotNull
    @Override
    public Map<Integer, Map<org.bukkit.enchantments.Enchantment, Integer>> getEnchantLists(@NotNull Inventory inventory, @NotNull ItemStack bukkitItem) {
        return Collections.emptyMap();
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
    @NotNull
    public Set<Block> handleFlameWalker(@NotNull LivingEntity bukkitEntity, @NotNull Location location, int level) {
        Entity entity = ((CraftLivingEntity) bukkitEntity).getHandle();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ServerLevel world = ((CraftWorld) bukkitEntity.getWorld()).getHandle();

        int radius = Math.min(16, 2 + level);
        BlockState bStone = Blocks.MAGMA_BLOCK.defaultBlockState();
        BlockPos.MutableBlockPos posAbove = new BlockPos.MutableBlockPos();

        Set<Block> blocks = new HashSet<>();
        for (BlockPos posNear : BlockPos.betweenClosed(pos.offset(-radius, -1, -radius), pos.offset(radius, -1, radius))) {
            if (!posNear.closerThan(entity.blockPosition(), radius)) continue;

            posAbove.set(posNear.getX(), posNear.getY() + 1, posNear.getZ());

            BlockState bLavaAbove = world.getBlockState(posAbove);
            BlockState bLava = world.getBlockState(posNear);

            if (!bLavaAbove.isAir()) continue;
            if (!bLava.getBlock().equals(Blocks.LAVA)) continue;
            if (bLava.getValue(LiquidBlock.LEVEL) != 0) continue;
            if (!bStone.canSurvive(world, posNear)) continue;
            if (!world.isUnobstructed(bStone, posNear, CollisionContext.empty())) continue;
            if (!CraftEventFactory.handleBlockFormEvent(world, posNear, bStone, entity)) continue;
            //world.scheduleTick(posNear, Blocks.STONE, Rnd.get(60, 120));

            Location bukkitLoc = new Location(world.getWorld(), posNear.getX(), posNear.getY(), posNear.getZ());
            blocks.add(bukkitLoc.getBlock());
        }
        return blocks;
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
}