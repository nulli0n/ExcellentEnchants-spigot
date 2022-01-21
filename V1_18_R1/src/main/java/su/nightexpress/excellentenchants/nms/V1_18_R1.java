package su.nightexpress.excellentenchants.nms;

import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemBow;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockFluids;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_18_R1.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class V1_18_R1 implements EnchantNMS {

    @Override
    @NotNull
    public Set<Block> handleFlameWalker(@NotNull LivingEntity entity1, @NotNull Location location, int level) {
        Entity entity = ((CraftLivingEntity) entity1).getHandle();
        BlockPosition pos = new BlockPosition(location.getX(), location.getY(), location.getZ());
        World world = ((CraftWorld) entity1.getWorld()).getHandle();

        IBlockData bStone = Blocks.iX.n();
        float rad = Math.min(16, 2 + level);

        org.bukkit.World w1 = entity1.getWorld();
        BlockPosition.MutableBlockPosition posMut = new BlockPosition.MutableBlockPosition();

        Set<Block> blocks = new HashSet<>();
        for (BlockPosition bNear : BlockPosition.a(pos.a(-rad, -1.0, -rad), pos.a(rad, -1.0, rad))) {
            if (!bNear.a(entity.cV(), rad)) continue;
            posMut.d(bNear.u(), bNear.v() + 1, bNear.w());

            IBlockData bLavaUp = world.a_(posMut);
            IBlockData bLava = world.a_(bNear);

            if (!bLavaUp.g()) continue;
            // меня заебало нахуй искать и подбирать ебучую лаву в NMS
            Block normal = w1.getBlockAt(bNear.u(), bNear.v(), bNear.w());
            if (normal.getType() != org.bukkit.Material.LAVA) continue;
            if (bLava.c(BlockFluids.a) != 0) continue;
            if (!bStone.a((IWorldReader) world, bNear)) continue;
            if (!world.a(bStone, bNear, VoxelShapeCollision.a())) continue;
            if (!CraftEventFactory.handleBlockFormEvent(world, bNear, bStone, entity)) continue;
            world.N().a(bNear, Blocks.iX);

            Location loc2 = new Location(world.getWorld(), bNear.u(), bNear.v(), bNear.w());
            blocks.add(loc2.getBlock());
        }
        return blocks;
    }
}
