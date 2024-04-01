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
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.ItemCategory;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.SimpeListener;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;
import java.util.List;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_RADIUS;

public class BlastMiningEnchant extends AbstractEnchantmentData implements ChanceData, BlockBreakEnchant, SimpeListener {

    public static final String ID = "blast_mining";

    private Modifier           explosionPower;
    private Modifier           minBlockStrength;
    private ChanceSettingsImpl chanceSettings;

    private int explodeLevel;

    public BlastMiningEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to mine blocks by explosion.");
        this.setMaxLevel(5);
        this.setRarity(Rarity.RARE);
        this.setConflicts(VeinminerEnchant.ID, TunnelEnchant.ID);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.multiply(10, 1, 1, 100));

        this.explosionPower = Modifier.read(config, "Settings.Explosion.Power",
            Modifier.add(3, 0.75, 1, 8),
            "Explosion power. The more power = the more blocks (area) to explode.");

        this.minBlockStrength = Modifier.read(config, "Settings.Min_Block_Strength",
            Modifier.add(1.5, -0.1, 1),
            "Minimal block strength value for the enchantment to have effect.",
            "Block strength value is how long it takes to break the block by a hand.",
            "For example, a Stone has 3.0 strength.");

        this.addPlaceholder(GENERIC_RADIUS, level -> NumberUtil.format(this.getExplosionPower(level)));
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
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
    public ItemCategory[] getItemCategories() {
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
        boolean exploded = block.getWorld().createExplosion(block.getLocation(), power, false, true, player);
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
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlastExplosionDamage(EntityDamageByEntityEvent event) {
        if (event.getCause() != DamageCause.ENTITY_EXPLOSION) return;
        if (!(event.getDamager() instanceof Player)) return;

        event.setCancelled(this.explodeLevel > 0);
    }

    // Do not reduce item durability for 'exploded' blocks.
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlastExplosionItemDamage(PlayerItemDamageEvent event) {
        event.setCancelled(this.explodeLevel > 0);
    }
}
