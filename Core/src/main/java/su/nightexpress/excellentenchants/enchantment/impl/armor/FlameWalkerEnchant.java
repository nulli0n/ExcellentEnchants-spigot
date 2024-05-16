package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.type.GenericEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.SimpeListener;
import su.nightexpress.nightcore.util.Pair;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class FlameWalkerEnchant extends AbstractEnchantmentData implements GenericEnchant, SimpeListener {

    public static final String ID = "flame_walker";

    private static final BlockFace[]                        FACES             = {BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
    private static final Map<Location, Pair<Long, Integer>> BLOCKS_TO_DESTROY = new ConcurrentHashMap<>();

    private Modifier blockDecayTime;

    public FlameWalkerEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription("Ability to walk on lava, ignore magma block damage.");
        this.setMaxLevel(3);
        this.setRarity(Rarity.RARE);
        this.setConflicts(Enchantment.FROST_WALKER.getKey().getKey());
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.blockDecayTime = Modifier.read(config, "Settings.Block_Decay",
            Modifier.add(8, 1, 1, 120),
            "Sets up to how long (in seconds) blocks will stay before turn back to lava.");
    }

    @Override
    public void clear() {
        BLOCKS_TO_DESTROY.keySet().forEach(location -> location.getBlock().setType(Material.LAVA));
        BLOCKS_TO_DESTROY.clear();
    }

    public static void addBlock(@NotNull Block block, double seconds) {
        BLOCKS_TO_DESTROY.put(block.getLocation(), Pair.of(System.currentTimeMillis() + (long) seconds * 1000L, Rnd.get(1000)));
    }

    public static boolean isBlock(@NotNull Block block) {
        return BLOCKS_TO_DESTROY.containsKey(block.getLocation());
    }

    public static void tickBlocks() {
        long now = System.currentTimeMillis();

        BLOCKS_TO_DESTROY.keySet().removeIf(location -> location.getBlock().isLiquid() || location.getBlock().getType() != Material.MAGMA_BLOCK);
        BLOCKS_TO_DESTROY.forEach((location, pair) -> {
            Block block = location.getBlock();
            long time = pair.getFirst();
            if (now >= time) {
                block.getWorld().getPlayers().forEach(player -> {
                    player.sendBlockDamage(location, 0F, pair.getSecond());
                });
                block.setType(Material.LAVA);
                UniParticle.blockCrack(Material.MAGMA_BLOCK).play(location, 0.5, 0.7, 0.5, 0.03, 30);
                return;
            }

            long diff = TimeUnit.MILLISECONDS.toSeconds(time - now);

            float progress = (float) (1D - Math.min(1D, diff / 5D));
            if (progress > 1F) progress = 1F;
            if (progress < 0F) progress = 0F;

            float finalProgress = progress;
            block.getWorld().getPlayers().forEach(player -> {
                player.sendBlockDamage(location, finalProgress, pair.getSecond());
            });
        });
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.ARMOR_FEET;
    }

    public double getBlockDecayTime(int level) {
        return this.blockDecayTime.getValue(level);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isFlying() || !this.isAvailableToUse(player)) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) return;

        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || boots.getType().isAir()) return;

        int level = EnchantUtils.getLevel(boots, this.getEnchantment());
        if (level <= 0) return;

        Block bTo = to.getBlock().getRelative(BlockFace.DOWN);
        boolean hasLava = Stream.of(FACES).anyMatch(face -> bTo.getRelative(face).getType() == Material.LAVA);
        if (!hasLava) return;

        Set<Block> blocks = plugin.getEnchantNMS().handleFlameWalker(player, player.getLocation(), level);
        blocks.forEach(block -> {
            addBlock(block, Rnd.getDouble(this.getBlockDecayTime(level)) + 1);
        });
        if (!blocks.isEmpty()) {
            this.consumeCharges(boots, level);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFlameWalkerBlock(BlockBreakEvent event) {
        if (isBlock(event.getBlock())) {
            event.setDropItems(false);
            event.setExpToDrop(0);
            event.getBlock().setType(Material.LAVA);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockExplode(EntityExplodeEvent event) {
        this.processExplosion(event.blockList());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockExplode2(BlockExplodeEvent event) {
        this.processExplosion(event.blockList());
    }

    private void processExplosion(@NotNull List<Block> blocks) {
        blocks.removeIf(block -> {
            if (isBlock(block)) {
                block.setType(Material.LAVA);
                return true;
            }
            return false;
        });
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMagmaDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.HOT_FLOOR) return;
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
        if (!this.isAvailableToUse(livingEntity)) return;

        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment == null) return;

        ItemStack boots = equipment.getBoots();
        if (boots == null || boots.getType().isAir()) return;

        int level = EnchantUtils.getLevel(boots, this.getEnchantment());
        if (level <= 0) return;

        event.setCancelled(true);
        this.consumeCharges(boots, level);
    }
}
