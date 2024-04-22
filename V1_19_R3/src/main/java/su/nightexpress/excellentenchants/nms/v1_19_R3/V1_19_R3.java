package su.nightexpress.excellentenchants.nms.v1_19_R3;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftFishHook;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.nms.EnchantNMS;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Reflex;
import su.nightexpress.nightcore.util.random.Rnd;

import java.lang.reflect.Method;
import java.util.*;

public class V1_19_R3 implements EnchantNMS {

    @Override
    public void unfreezeRegistry() {
        Reflex.setFieldValue(BuiltInRegistries.ENCHANTMENT, "l", false);
        Reflex.setFieldValue(BuiltInRegistries.ENCHANTMENT, "m", new IdentityHashMap<>());
        Reflex.setFieldValue(Enchantment.class, "acceptingNew", true);
    }

    @Override
    public void freezeRegistry() {
        Enchantment.stopAcceptingRegistrations();
        BuiltInRegistries.ENCHANTMENT.freeze();
    }

    @Override
    public void registerEnchantment(@NotNull EnchantmentData data) {
        CustomEnchantment customEnchantment = new CustomEnchantment(data);
        BuiltInRegistries.ENCHANTMENT.createIntrusiveHolder(customEnchantment);
        Registry.register(BuiltInRegistries.ENCHANTMENT, data.getId(), customEnchantment);

        CraftEnchantment craftEnchantment = new CraftEnchantment(customEnchantment);
        Enchantment.registerEnchantment(craftEnchantment);
        data.setEnchantment(craftEnchantment);
    }

    private static final Method GET_ENCHANTS_LIST = Reflex.getMethod(EnchantmentMenu.class, "a", net.minecraft.world.item.ItemStack.class, Integer.TYPE, Integer.TYPE);

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public Map<Integer, Map<Enchantment, Integer>> getEnchantLists(@NotNull Inventory inventory, @NotNull ItemStack bukkitItem) {
        Map<Integer, Map<Enchantment, Integer>> map = new HashMap<>();

        // Returns SimpleContainer class assigned to 'enchantSlots' field.
        Container container = ((CraftInventory) inventory).getInventory();

        // Get parent (real EnchantmentMenu) object from SimpleContainer obtained above.
        EnchantmentMenu enchantmentMenu = (EnchantmentMenu) Reflex.getFieldValue(container, "this$0");

        net.minecraft.world.item.ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);

        for (int button = 0; button < 3; button++) {
            int cost = enchantmentMenu.costs[button];
            List<EnchantmentInstance> list = (List<EnchantmentInstance>) Reflex.invokeMethod(GET_ENCHANTS_LIST, enchantmentMenu, itemStack, button, cost);

            Map<Enchantment, Integer> enchantments = new HashMap<>();

            if (list != null && !list.isEmpty()) {
                EnchantmentInstance random = Rnd.get(list);
                enchantmentMenu.enchantClue[button] = BuiltInRegistries.ENCHANTMENT.getId(random.enchantment);
                enchantmentMenu.levelClue[button] = random.level;

                for (EnchantmentInstance instance : list) {
                    ResourceLocation location = BuiltInRegistries.ENCHANTMENT.getKey(instance.enchantment);
                    if (location == null) continue;

                    enchantments.put(BukkitThing.getEnchantment(CraftNamespacedKey.fromMinecraft(location).getKey()), instance.level);
                }
            }

            map.put(button, enchantments);
        }

        return map;
    }

    @Override
    public void sendAttackPacket(@NotNull Player player, int id) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        Entity entity = craftPlayer.getHandle();
        ClientboundAnimatePacket packet = new ClientboundAnimatePacket(entity, id);
        craftPlayer.getHandle().connection.send(packet);
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

        itemStack.hurtAndBreak(result, handle.getPlayerOwner(), player -> {
            player.broadcastBreakEvent(hand);
        });
    }

    @NotNull
    @Override
    public Material getItemBlockVariant(@NotNull Material material) {
        ItemStack itemStack = new ItemStack(material);
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        if (nmsStack.getItem() instanceof BlockItem blockItem) {
            return CraftMagicNumbers.getMaterial(blockItem.getBlock());
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
    public Item popResource(@NotNull Block block, @NotNull ItemStack item) {
        Level world = ((CraftWorld)block.getWorld()).getHandle();
        BlockPos pos = ((CraftBlock)block).getPosition();
        net.minecraft.world.item.ItemStack itemstack = CraftItemStack.asNMSCopy(item);

        float yMod = EntityType.ITEM.getHeight() / 2.0F;
        double x = (pos.getX() + 0.5F) + Mth.nextDouble(world.random, -0.25D, 0.25D);
        double y = (pos.getY() + 0.5F) + Mth.nextDouble(world.random, -0.25D, 0.25D) - yMod;
        double z = (pos.getZ() + 0.5F) + Mth.nextDouble(world.random, -0.25D, 0.25D);

        ItemEntity itemEntity = new ItemEntity(world, x, y, z, itemstack);
        itemEntity.setDefaultPickUpDelay();
        return (Item) itemEntity.getBukkitEntity();
    }
}
