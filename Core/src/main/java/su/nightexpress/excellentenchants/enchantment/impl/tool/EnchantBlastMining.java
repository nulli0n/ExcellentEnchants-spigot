package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.hook.impl.NoCheatPlusHook;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.enchantment.EnchantRegister;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;

import java.util.List;
import java.util.function.UnaryOperator;

public class EnchantBlastMining extends ExcellentEnchant implements Chanced, BlockBreakEnchant {

    public static final String ID = "blast_mining";
    public static final String PLACEHOLDER_EXPLOSION_POWER = "%enchantment_explosion_power%";

    private static final String META_EXPLOSION_SOURCE = ID + "_explosion_source";
    private static final String META_EXPLOSION_MINED = ID + "_explosion_mined";

    private EnchantScaler explosionPower;
    private EnchantScaler minBlockStrength;

    private ChanceImplementation chanceImplementation;

    public EnchantBlastMining(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.chanceImplementation = ChanceImplementation.create(this);
        this.explosionPower = EnchantScaler.read(this, "Settings.Explosion.Power", "3.0 + (" + Placeholders.ENCHANTMENT_LEVEL + " - 1.0 * 0.25)",
            "Explosion power. The more power = the more blocks (area) to explode.");
        this.minBlockStrength = EnchantScaler.read(this, "Settings.Min_Block_Strength", "1.5 - " + Placeholders.ENCHANTMENT_LEVEL + " / 10",
            "Minimal block strength value for the enchantment to have effect.",
            "Block strength value is how long it takes to break the block by a hand.",
            "For example, a Stone has 3.0 strength.");
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

    private boolean isBlockHardEnough(@NotNull Block block, int level) {
        float strength = block.getType().getHardness();//plugin.getNMS().getBlockStrength(block);
        return (strength >= this.getMinBlockStrength(level));
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str)
            .replace(PLACEHOLDER_EXPLOSION_POWER, NumberUtil.format(this.getExplosionPower(level)));
    }

    @Override
    @NotNull
    public FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.PICKAXE};
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.isAvailableToUse(player)) return false;

        if (EnchantRegister.VEINMINER != null && EnchantManager.hasEnchantment(item, EnchantRegister.VEINMINER)) return false;
        if (EnchantRegister.TUNNEL != null && EnchantManager.hasEnchantment(item, EnchantRegister.TUNNEL)) return false;

        Block block = e.getBlock();
        if (block.hasMetadata(META_EXPLOSION_MINED)) return false;

        if (!this.isBlockHardEnough(block, level)) return false;
        if (!this.checkTriggerChance(level)) return false;

        float power = (float) this.getExplosionPower(level);

        player.setMetadata(META_EXPLOSION_SOURCE, new FixedMetadataValue(plugin, level));
        NoCheatPlusHook.exemptBlocks(player);
        boolean exploded = block.getWorld().createExplosion(block.getLocation(), power, false, true, player);
        NoCheatPlusHook.unexemptBlocks(player);
        player.removeMetadata(META_EXPLOSION_SOURCE, plugin);
        return exploded;
    }

    // Process explosion event to mine blocks.
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlastExplosionEvent(EntityExplodeEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (!player.hasMetadata(META_EXPLOSION_SOURCE)) return;

        int level = player.getMetadata(META_EXPLOSION_SOURCE).get(0).asInt();
        List<Block> blockList = e.blockList();

        // Remove the 'source' block which player mined and caused the explosion to prevent duplicated drops.
        // Remove all the 'soft' blocks that should not be exploded.
        blockList.removeIf(block -> block.getLocation().equals(e.getLocation()) || !this.isBlockHardEnough(block, level));

        // Break all 'exploded' blocks by a player, adding metadata to them to prevent trigger enchantment in a loop.
        blockList.forEach(block -> {
            block.setMetadata(META_EXPLOSION_MINED, new FixedMetadataValue(plugin, true));
            //plugin.getNMS().breakBlock(player, block);
            player.breakBlock(block);
            block.removeMetadata(META_EXPLOSION_MINED, plugin);
        });

        // Clear list of 'exploded' blocks so the event won't affect them, as they are already mined by a player.
        blockList.clear();
    }

    // Do not damage around entities by enchantment explosion.
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlastExplosionDamage(EntityDamageByEntityEvent e) {
        if (e.getCause() != DamageCause.ENTITY_EXPLOSION) return;
        if (!(e.getDamager() instanceof Player player)) return;

        e.setCancelled(player.hasMetadata(META_EXPLOSION_SOURCE));
    }

    // Do not reduce item durability for 'exploded' blocks.
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlastExplosionItemDamage(PlayerItemDamageEvent e) {
        if (!e.getPlayer().hasMetadata(META_EXPLOSION_SOURCE)) return;

        e.setCancelled(true);
    }
}
