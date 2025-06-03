package su.nightexpress.excellentenchants.nms.v1_21_4;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_21_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_21_R3.block.CraftBlockType;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_21_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.nms.EnchantNMS;

import java.util.HashSet;
import java.util.Set;

public class Internal_1_21_4 implements EnchantNMS {

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
    public Set<Block> handleFlameWalker(@NotNull LivingEntity bukkitEntity, int level, int radius) {
        Entity entity = ((CraftLivingEntity) bukkitEntity).getHandle();
        BlockPos pos = entity.blockPosition();
        Level world = entity.level();

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

        return blocks;
    }
}