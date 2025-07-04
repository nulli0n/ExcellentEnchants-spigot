package su.nightexpress.excellentenchants.nms.mc_1_21_7;

import su.nightexpress.excellentenchants.nms.EnchantNMS;

public class Internal_1_21_7 implements EnchantNMS {

//    @NotNull
//    @Override
//    public Material getItemBlockVariant(@NotNull Material material) {
//        ItemStack itemStack = new ItemStack(material);
//        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
//        if (nmsStack.getItem() instanceof BlockItem blockItem) {
//            return CraftBlockType.minecraftToBukkit(blockItem.getBlock());
//        }
//        return material;
//    }

//    @Override
//    @NotNull
//    public Set<Block> handleFlameWalker(@NotNull LivingEntity bukkitEntity, int level, int radius) {
//        Entity entity = ((CraftLivingEntity) bukkitEntity).getHandle();
//        BlockPos pos = entity.blockPosition();
//        Level world = entity.level();
//
//        BlockState magmaState = Blocks.MAGMA_BLOCK.defaultBlockState();
//        BlockPos.MutableBlockPos posAbove = new BlockPos.MutableBlockPos();
//
//        Set<Block> blocks = new HashSet<>();
//        for (BlockPos posNear : BlockPos.betweenClosed(pos.offset(-radius, -1, -radius), pos.offset(radius, -1, radius))) {
//            if (!posNear.closerThan(entity.blockPosition(), radius)) continue;
//
//            posAbove.set(posNear.getX(), posNear.getY() + 1, posNear.getZ());
//
//            BlockState aboveLava = world.getBlockState(posAbove);
//            BlockState lavaState = world.getBlockState(posNear);
//
//            if (!aboveLava.isAir()) continue;
//            if (!lavaState.getBlock().equals(Blocks.LAVA)) continue;
//            if (lavaState.getValue(LiquidBlock.LEVEL) != 0) continue;
//            if (!magmaState.canSurvive(world, posNear)) continue;
//            if (!world.isUnobstructed(magmaState, posNear, CollisionContext.empty())) continue;
//            if (!CraftEventFactory.handleBlockFormEvent(world, posNear, magmaState, 3, entity)) continue;
//
//            blocks.add(CraftBlock.at(world, posNear));
//        }
//
//        return blocks;
//    }
}
