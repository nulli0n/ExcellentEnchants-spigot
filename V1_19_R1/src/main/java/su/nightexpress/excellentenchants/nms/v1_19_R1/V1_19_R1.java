package su.nightexpress.excellentenchants.nms.v1_19_R1;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R1.event.CraftEventFactory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.nms.EnchantNMS;

import java.util.HashSet;
import java.util.Set;

public class V1_19_R1 implements EnchantNMS {

    @Override
    public void addEnchantmentEffect(@NotNull LivingEntity entity, @NotNull Enchantment enchant, @NotNull PotionEffect effect) {
        net.minecraft.world.entity.LivingEntity entity1 = ((CraftLivingEntity)entity).getHandle();
        entity1.addEffect(new CustomEffectInstance(MobEffect.byId(effect.getType().getId()), effect.getAmplifier(), enchant), EntityPotionEffectEvent.Cause.PLUGIN);
    }

    @Override
    @Nullable
    public Enchantment getEnchantmentByEffect(@NotNull LivingEntity entity, @NotNull PotionEffect type) {
        net.minecraft.world.entity.LivingEntity entity1 = ((CraftLivingEntity)entity).getHandle();
        MobEffectInstance handle = entity1.getEffect(MobEffect.byId(type.getType().getId()));
        if (handle instanceof CustomEffectInstance instance) {
            return instance.getEnchantment();
        }
        return null;
    }

    @Override
    @NotNull
    public Set<Block> handleFlameWalker(@NotNull LivingEntity bukkitEntity, @NotNull Location location, int level) {
        Entity entity = ((CraftLivingEntity) bukkitEntity).getHandle();
        BlockPos pos = new BlockPos(location.getX(), location.getY(), location.getZ());
        ServerLevel world = ((CraftWorld) bukkitEntity.getWorld()).getHandle();

        float radius = Math.min(16F, 2F + level);
        BlockState bStone = Blocks.MAGMA_BLOCK.defaultBlockState();
        BlockPos.MutableBlockPos posAbove = new BlockPos.MutableBlockPos();

        Set<Block> blocks = new HashSet<>();
        for (BlockPos posNear : BlockPos.betweenClosed(pos.offset(-radius, -1.0, -radius), pos.offset(radius, -1.0, radius))) {
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
}
