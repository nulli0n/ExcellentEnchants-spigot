package su.nightexpress.excellentenchants.nms;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class V1_16_R3 implements EnchantNMS {

    @Override
    @NotNull
    public Set<Block> handleFlameWalker(@NotNull LivingEntity entity1, @NotNull Location location, int level) {
        EntityLiving entity = ((CraftLivingEntity) entity1).getHandle();
        BlockPosition pos = new BlockPosition(location.getX(), location.getY(), location.getZ());
        World world = ((CraftWorld) entity1.getWorld()).getHandle();

        IBlockData bStone = Blocks.MAGMA_BLOCK.getBlockData();
        float rad = Math.min(16, 2 + level);

        BlockPosition.MutableBlockPosition posMut = new BlockPosition.MutableBlockPosition();
        Set<Block> blocks = new HashSet<>();
        for (BlockPosition bNear : BlockPosition.a(pos.a(-rad, -1.0, -rad), pos.a(rad, -1.0, rad))) {

            if (!bNear.a(entity.getPositionVector(), rad)) continue;
            posMut.d(bNear.getX(), bNear.getY() + 1, bNear.getZ());

            IBlockData bLavaUp = world.getType(posMut);
            IBlockData bLava = world.getType(bNear);

            if (!bLavaUp.isAir()) continue;
            if (bLava.getMaterial() != Material.LAVA) continue;
            if (bLava.get(BlockFluids.LEVEL) != 0) continue;
            if (!bStone.canPlace(world, bNear)) continue;
            if (!world.a(bStone, bNear, VoxelShapeCollision.a())) continue;
            if (!CraftEventFactory.handleBlockFormEvent(world, bNear, bStone, entity)) continue;

            world.getBlockTickList().a(bNear, Blocks.MAGMA_BLOCK, MathHelper.nextInt(entity.getRandom(), 60, 120));

            Location loc2 = new Location(world.getWorld(), bNear.getX(), bNear.getY(), bNear.getZ());
            blocks.add(loc2.getBlock());
        }
        return blocks;
    }
}
