package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.DecayBlock;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.bridge.FlameWalker;
import su.nightexpress.excellentenchants.api.enchantment.type.GenericEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.SimpeListener;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class FlameWalkerEnchant extends GameEnchantment implements GenericEnchant, FlameWalker, SimpeListener {

    private static final BlockFace[] FACES = {BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};

    private final Map<Location, DecayBlock> magmaBlocks = new ConcurrentHashMap<>();

    private Modifier radius;
    private Modifier blockDecayTime;

    public FlameWalkerEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.treasure(TradeType.DESERT_SPECIAL));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            Lists.newList("Ability to walk on lava, ignore magma block damage."),
            EnchantRarity.LEGENDARY,
            3,
            ItemCategories.BOOTS,
            null,
            Lists.newSet(BukkitThing.toString(Enchantment.FROST_WALKER))
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.radius = Modifier.read(config, "Settings.Radius",
            Modifier.add(3.0, 1.0, 1, 16),
            "Sets max radius."
        );

        this.blockDecayTime = Modifier.read(config, "Settings.Block_Decay",
            Modifier.add(8, 1, 1, 120),
            "Sets up to how long (in seconds) blocks will stay before turn back into lava."
        );
    }

    @NotNull
    @Override
    public Modifier getRadius() {
        return radius;
    }

    @Override
    public double getBlockDecayTime(int level) {
        return this.blockDecayTime.getValue(level);
    }

//    @Override
//    public void clear() {
//        this.removeBlocks();
//    }

    @Override
    public void removeBlocks() {
        magmaBlocks.keySet().forEach(location -> location.getBlock().setType(Material.LAVA));
        magmaBlocks.clear();
    }

    public boolean isBlock(@NotNull Block block) {
        return magmaBlocks.containsKey(block.getLocation());
    }

    @Override
    public void tickBlocks() {
        magmaBlocks.keySet().removeIf(location -> location.getBlock().isLiquid() || location.getBlock().getType() != Material.MAGMA_BLOCK);
        magmaBlocks.forEach((location, decayBlock) -> {
            Block block = location.getBlock();
            if (decayBlock.isExpired()) {
                block.getWorld().getPlayers().forEach(player -> {
                    player.sendBlockDamage(location, 0F, decayBlock.getSourceId());
                });
                block.setType(Material.LAVA);
                UniParticle.blockCrack(Material.MAGMA_BLOCK).play(location, 0.5, 0.7, 0.5, 0.03, 30);
                return;
            }

            float progress = decayBlock.getProgress();
            block.getWorld().getPlayers().forEach(player -> {
                player.sendBlockDamage(location, progress, decayBlock.getSourceId());
            });
        });
    }

    @Override
    public void addBlock(@NotNull Block block, int level) {
        this.magmaBlocks.put(block.getLocation(), new DecayBlock(block.getLocation(), Rnd.getDouble(this.getBlockDecayTime(level)) + 1));
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

        int level = EnchantUtils.getLevel(boots, this.getBukkitEnchantment());
        if (level <= 0) return;
        if (this.isOutOfCharges(boots)) return;

        Block bTo = to.getBlock().getRelative(BlockFace.DOWN);
        boolean hasLava = Stream.of(FACES).anyMatch(face -> bTo.getRelative(face).getType() == Material.LAVA);
        if (!hasLava) return;

        if (plugin.getEnchantNMS().handleFlameWalker(this, player, level)) {
            this.consumeCharges(boots, level);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMagmaDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.HOT_FLOOR) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!this.isAvailableToUse(entity)) return;

        ItemStack boots = EnchantUtils.getEquipped(entity, EquipmentSlot.FEET);
        if (boots == null || boots.getType().isAir()) return;

        int level = EnchantUtils.getLevel(boots, this.getBukkitEnchantment());
        if (level <= 0) return;
        if (this.isOutOfCharges(boots)) return;

        event.setCancelled(true);
        this.consumeCharges(boots, level);
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
}
