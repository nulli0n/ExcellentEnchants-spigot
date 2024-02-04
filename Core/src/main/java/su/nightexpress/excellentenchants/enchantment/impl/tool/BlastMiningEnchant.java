package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.EventListener;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.api.enchantment.ItemCategory;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.excellentenchants.hook.impl.NoCheatPlusHook;

import java.util.List;

public class BlastMiningEnchant extends ExcellentEnchant implements Chanced, BlockBreakEnchant, EventListener {

    public static final String ID = "blast_mining";
    public static final String PLACEHOLDER_EXPLOSION_POWER = "%enchantment_explosion_power%";

    private EnchantScaler explosionPower;
    private EnchantScaler minBlockStrength;
    private ChanceImplementation chanceImplementation;

    private int explodeLevel;

    public BlastMiningEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to mine blocks by explosion.");
        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(1.0);
        this.getDefaults().setConflicts(VeinminerEnchant.ID, TunnelEnchant.ID);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

        this.chanceImplementation = ChanceImplementation.create(this,
            "20.0 * " + Placeholders.ENCHANTMENT_LEVEL);

        this.explosionPower = EnchantScaler.read(this, "Settings.Explosion.Power",
            "3.0 + (" + Placeholders.ENCHANTMENT_LEVEL + " - 1.0 * 0.25)",
            "Explosion power. The more power = the more blocks (area) to explode.");

        this.minBlockStrength = EnchantScaler.read(this, "Settings.Min_Block_Strength",
            "1.5 - " + Placeholders.ENCHANTMENT_LEVEL + " / 10",
            "Minimal block strength value for the enchantment to have effect.",
            "Block strength value is how long it takes to break the block by a hand.",
            "For example, a Stone has 3.0 strength.");

        this.addPlaceholder(PLACEHOLDER_EXPLOSION_POWER, level -> NumberUtil.format(this.getExplosionPower(level)));
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    public double getExplosionPower(int level) {
        return this.explosionPower.getValue(level);
    }

    public float getMinBlockStrength(int level) {
        return (float) minBlockStrength.getValue(level);
    }

    private boolean isHardEnough(@NotNull Block block, int level) {
        float strength = block.getType().getHardness();
        return (strength >= this.getMinBlockStrength(level));
    }

    @Override
    @NotNull
    public ItemCategory[] getFitItemTypes() {
        return new ItemCategory[]{ItemCategory.PICKAXE};
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        if (!(entity instanceof Player player)) return false;
        if (EnchantUtils.isBusy()) return false;

        Block block = event.getBlock();
        if (!this.isHardEnough(block, level)) return false;
        if (!this.checkTriggerChance(level)) return false;

        float power = (float) this.getExplosionPower(level);

        this.explodeLevel = level;
        NoCheatPlusHook.exemptBlocks(player);
        boolean exploded = block.getWorld().createExplosion(block.getLocation(), power, false, true, player);
        NoCheatPlusHook.unexemptBlocks(player);
        this.explodeLevel = -1;

        return exploded;
    }

    // Process explosion event to mine blocks.
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlastExplosionEvent(EntityExplodeEvent event) {
        if (this.explodeLevel <= 0) return;
        if (!(event.getEntity() instanceof Player player)) return;

        List<Block> blockList = event.blockList();
        blockList.forEach(block -> {
            if (block.getLocation().equals(event.getLocation()) || !this.isHardEnough(block, this.explodeLevel)) return;

            EnchantUtils.safeBusyBreak(player, block);
        });
        blockList.clear();
    }

    // Do not damage around entities by enchantment explosion.
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlastExplosionDamage(EntityDamageByEntityEvent e) {
        if (e.getCause() != DamageCause.ENTITY_EXPLOSION) return;
        if (!(e.getDamager() instanceof Player player)) return;

        e.setCancelled(this.explodeLevel > 0);
    }

    // Do not reduce item durability for 'exploded' blocks.
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlastExplosionItemDamage(PlayerItemDamageEvent e) {
        e.setCancelled(this.explodeLevel > 0);
    }
}
