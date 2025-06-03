package su.nightexpress.excellentenchants.enchantment.armor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.damage.DamageBonus;
import su.nightexpress.excellentenchants.api.damage.DamageBonusType;
import su.nightexpress.excellentenchants.api.enchantment.type.MoveEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.ProtectionEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.random.Rnd;

import java.io.File;
import java.util.Set;
import java.util.stream.Stream;

public class FlameWalkerEnchant extends GameEnchantment implements MoveEnchant, ProtectionEnchant {

    private static final BlockFace[] FACES = {BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};

    private Modifier radius;
    private Modifier decayTime;

    public FlameWalkerEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.radius = Modifier.load(config, "FlameWalker.Radius",
            Modifier.addictive(2).perLevel(1).capacity(16),
            "Square radius around the block to transform into magma block."
        );

        this.decayTime = Modifier.load(config, "FlameWalker.Block_Decay",
            Modifier.addictive(8).perLevel(1).capacity(15),
            "Sets life time for magma blocks before turning back into lava."
        );
    }

    @NotNull
    public Modifier getRadius() {
        return this.radius;
    }

    public int getBlockDecayTime(int level) {
        return (int) this.decayTime.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getMovePriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    @NotNull
    public EnchantPriority getProtectionPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    @NotNull
    public DamageBonus getDamageBonus() {
        return new DamageBonus(DamageBonusType.NORMAL);
    }

    @Override
    public boolean onMove(@NotNull PlayerMoveEvent event, @NotNull Player player, @NotNull ItemStack itemStack, int level) {
        Location to = event.getTo();

        Block nextBlock = to.getBlock().getRelative(BlockFace.DOWN);
        boolean hasLava = Stream.of(FACES).anyMatch(face -> nextBlock.getRelative(face).getType() == Material.LAVA);
        if (!hasLava) return false;

        int radius = (int) this.radius.getValue(level);
        Set<Block> blocks = this.plugin.getEnchantNMS().handleFlameWalker(player, level, radius);
        if (blocks.isEmpty()) return false;

        blocks.forEach(block -> {
            int lifeTime = (int) (Rnd.getDouble(this.getBlockDecayTime(level)) + 1);
            this.plugin.getEnchantManager().addTickedBlock(block, Material.LAVA, Material.MAGMA_BLOCK, lifeTime);
        });
        return true;
    }

    @Override
    public boolean onProtection(@NotNull EntityDamageEvent event, @NotNull DamageBonus damageBonus, @NotNull LivingEntity entity, @NotNull ItemStack itemStack, int level) {
        DamageSource source = event.getDamageSource();
        DamageType type = source.getDamageType();
        if (type != DamageType.HOT_FLOOR) return false;

        event.setCancelled(true);
        return true;
    }
}
