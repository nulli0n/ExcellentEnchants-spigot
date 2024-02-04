package su.nightexpress.excellentenchants.nms.v1_20_R1;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftFishHook;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.Reflex;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;
import su.nightexpress.excellentenchants.nms.EnchantNMS;

import java.util.HashSet;
import java.util.Set;

public class V1_20_R1 implements EnchantNMS {

    @Override
    public void unfreezeRegistry() {
        Reflex.setFieldValue(Enchantment.class, "acceptingNew", true);
    }

    @Override
    public void freezeRegistry() {
        Enchantment.stopAcceptingRegistrations();
    }

    @Override
    public void registerEnchantment(@NotNull IEnchantment enchantment) {
        Enchantment.registerEnchantment((Enchantment) enchantment);
    }

    @Override
    public void sendAttackPacket(@NotNull Player player, int id) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerPlayer entity = craftPlayer.getHandle();
        ClientboundAnimatePacket packet = new ClientboundAnimatePacket(entity, id);
        craftPlayer.getHandle().connection.send(packet);
    }

    @Override
    public void retrieveHook(@NotNull FishHook hook, @NotNull ItemStack item) {
        CraftFishHook craftFishHook = (CraftFishHook) hook;
        FishingHook handle = craftFishHook.getHandle();
        handle.retrieve(CraftItemStack.asNMSCopy(item));
    }

    @Override
    @Nullable
    public ItemStack getSpawnEgg(@NotNull LivingEntity entity) {
        CraftLivingEntity craftLivingEntity = (CraftLivingEntity) entity;
        net.minecraft.world.entity.LivingEntity livingEntity = craftLivingEntity.getHandle();

        SpawnEggItem eggItem = SpawnEggItem.byId(livingEntity.getType());
        if (eggItem == null) return null;

        return CraftItemStack.asBukkitCopy(eggItem.getDefaultInstance());
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

    /*public static void popResourceFromFace(Level world, BlockPos blockposition, Direction enumdirection, ItemStack itemstack) {
        int i = enumdirection.getStepX();
        int j = enumdirection.getStepY();
        int k = enumdirection.getStepZ();
        float f = EntityType.ITEM.getWidth() / 2.0F;
        float f1 = EntityType.ITEM.getHeight() / 2.0F;
        double d0 = (double)((float)blockposition.getX() + 0.5F) + (i == 0 ? Mth.nextDouble(world.random, -0.25D, 0.25D) : (double)((float)i * (0.5F + f)));
        double d1 = (double)((float)blockposition.getY() + 0.5F) + (j == 0 ? Mth.nextDouble(world.random, -0.25D, 0.25D) : (double)((float)j * (0.5F + f1))) - (double)f1;
        double d2 = (double)((float)blockposition.getZ() + 0.5F) + (k == 0 ? Mth.nextDouble(world.random, -0.25D, 0.25D) : (double)((float)k * (0.5F + f)));
        double d3 = i == 0 ? Mth.nextDouble(world.random, -0.1D, 0.1D) : (double)i * 0.1D;
        double d4 = j == 0 ? Mth.nextDouble(world.random, 0.0D, 0.1D) : (double)j * 0.1D + 0.1D;
        double d5 = k == 0 ? Mth.nextDouble(world.random, -0.1D, 0.1D) : (double)k * 0.1D;
        popResource(world, () -> {
            return new ItemEntity(world, d0, d1, d2, itemstack, d3, d4, d5);
        }, itemstack);
    }

    private static void popResource(Level world, Supplier<ItemEntity> supplier, ItemStack itemstack) {
        if (!world.isClientSide && !itemstack.isEmpty() && world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            ItemEntity entityitem = (ItemEntity)supplier.get();
            entityitem.setDefaultPickUpDelay();
            if (world.captureDrops != null) {
                world.captureDrops.add(entityitem);
            } else {
                world.addFreshEntity(entityitem);
            }
        }

    }*/
}
