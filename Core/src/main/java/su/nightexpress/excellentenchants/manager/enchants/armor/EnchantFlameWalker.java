package su.nightexpress.excellentenchants.manager.enchants.armor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.task.AbstractTask;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.MoveEnchant;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class EnchantFlameWalker extends IEnchantChanceTemplate implements MoveEnchant, ICleanable {

    public static final String ID = "flame_walker";

    private static final BlockFace[]      FACES             = {BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
    private static final Map<Block, Long> BLOCKS_TO_DESTROY = new HashMap<>();

    private Scaler        blockDecayTime;
    private BlockTickTask blockTickTask;

    public EnchantFlameWalker(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);

        this.blockTickTask = new BlockTickTask(plugin);
        this.blockTickTask.start();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.blockDecayTime = new EnchantScaler(this, "Settings.Block_Decay");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.addMissing("Settings.Block_Decay", "5.0 + " + PLACEHOLDER_LEVEL + " * 2");
    }

    @Override
    public void clear() {
        if (this.blockTickTask != null) {
            this.blockTickTask.stop();
            this.blockTickTask = null;
        }
        BLOCKS_TO_DESTROY.keySet().forEach(block -> block.setType(Material.LAVA));
        BLOCKS_TO_DESTROY.clear();
    }

    public static void addBlock(@NotNull Block block, double seconds) {
        BLOCKS_TO_DESTROY.put(block, (long) (System.currentTimeMillis() + seconds * 1000L));
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_FEET;
    }

    public double getBlockDecayTime(int level) {
        return this.blockDecayTime.getValue(level);
    }

    @Override
    public boolean use(@NotNull PlayerMoveEvent e, @NotNull LivingEntity entity, int level) {
        if (!this.isEnchantmentAvailable(entity)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(entity)) return false;

        plugin.getEnchantNMS().handleFlameWalker(entity, entity.getLocation(), level).forEach(block -> {
            addBlock(block, Rnd.getDouble(this.getBlockDecayTime(level)) + 1);
        });
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchantFlameWalker(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.isFlying()) return;
        if (!this.isEnchantmentAvailable(player)) return;

        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) return;
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) return;

        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || boots.getType().isAir()) return;

        //int level = boots.getEnchantmentLevel(this);
        int level = EnchantManager.getEnchantmentLevel(boots, this);
        if (level < 1) return;

        Block bTo = to.getBlock().getRelative(BlockFace.DOWN);
        boolean hasLava = Stream.of(FACES).anyMatch(face -> bTo.getRelative(face).getType() == Material.LAVA);
        if (!hasLava) return;

        this.use(e, player, level);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFlameWalkerBlock(BlockBreakEvent e) {
        if (BLOCKS_TO_DESTROY.containsKey(e.getBlock())) {
            e.setDropItems(false);
            e.setExpToDrop(0);
            e.getBlock().setType(Material.LAVA);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFlameWalkerMagmaDamage(EntityDamageEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.HOT_FLOOR) return;
        if (!(e.getEntity() instanceof LivingEntity livingEntity)) return;
        if (!this.isEnchantmentAvailable(livingEntity)) return;

        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment == null) return;

        ItemStack boots = equipment.getBoots();
        if (boots == null || boots.getType().isAir()) return;

        int level = EnchantManager.getEnchantmentLevel(boots, this);
        if (level < 1) return;
        if (!this.checkTriggerChance(level)) return;

        e.setCancelled(true);
    }

    static class BlockTickTask extends AbstractTask<ExcellentEnchants> {

        public BlockTickTask(@NotNull ExcellentEnchants plugin) {
            super(plugin, 1, false);
        }

        @Override
        public void action() {
            long now = System.currentTimeMillis();

            BLOCKS_TO_DESTROY.keySet().removeIf(block -> {
                if (block.isEmpty()) return true;

                long time = BLOCKS_TO_DESTROY.get(block);
                if (now >= time) {
                    block.setType(Material.LAVA);
                    EffectUtil.playEffect(block.getLocation(), Particle.BLOCK_CRACK.name(), Material.MAGMA_BLOCK.name(), 0.5, 0.7, 0.5, 0.03, 50);
                    return true;
                }
                return false;
            });
        }
    }
}
