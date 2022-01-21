package su.nightexpress.excellentenchants.manager.enchants.tool;

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
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.hook.HookNCP;
import su.nightexpress.excellentenchants.manager.EnchantRegister;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

import java.util.List;
import java.util.function.UnaryOperator;

public class EnchantBlastMining extends IEnchantChanceTemplate implements BlockBreakEnchant {

    private final Scaler explosionPower;
    private final Scaler minBlockStrength;

    public static final String ID = "blast_mining";
    public static final String PLACEHOLDER_EXPLOSION_POWER = "%enchantment_explosion_power%";

    private static final String META_EXPLOSION_SOURCE = ID + "_explosion_source";
    private static final String META_EXPLOSION_MINED = ID + "_explosion_mined";

    public EnchantBlastMining(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);

        this.explosionPower = new EnchantScaler(this, "Settings.Explosion.Power");
        this.minBlockStrength = new EnchantScaler(this, "Settings.Min_Block_Strength");
    }

    public double getExplosionPower(int level) {
        return this.explosionPower.getValue(level);
    }

    public float getMinBlockStrength(int level) {
        return (float) minBlockStrength.getValue(level);
    }

    private boolean isBlockHardEnough(@NotNull Block block, int level) {
        float strength = plugin.getNMS().getBlockStrength(block);
        return (strength >= this.getMinBlockStrength(level));
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.addMissing("Settings.Min_Block_Strength", "1.5 - " + PLACEHOLDER_LEVEL + " / 10.0");
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(EnchantRegister.TUNNEL);
        this.addConflict(EnchantRegister.VEINMINER);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_EXPLOSION_POWER, NumberUtil.format(this.getExplosionPower(level)))
        );
    }

    /*@Override
    public boolean isFitItemType(@NotNull ItemStack item) {
        return ItemUtil.isPickaxe(item);
    }*/

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
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.isEnchantmentAvailable(player)) return false;

        if (EnchantRegister.VEINMINER != null && item.containsEnchantment(EnchantRegister.VEINMINER)) return false;
        if (EnchantRegister.TUNNEL != null && item.containsEnchantment(EnchantRegister.TUNNEL)) return false;

        Block block = e.getBlock();
        if (block.hasMetadata(META_EXPLOSION_MINED)) return false;

        if (!this.isBlockHardEnough(block, level)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(player)) return false;

        float power = (float) this.getExplosionPower(level);

        player.setMetadata(META_EXPLOSION_SOURCE, new FixedMetadataValue(plugin, level));
        HookNCP.exemptBlocks(player);
        boolean exploded = block.getWorld().createExplosion(block.getLocation(), power, false, true, player);
        HookNCP.unexemptBlocks(player);
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
            plugin.getNMS().breakBlock(player, block);
            block.removeMetadata(META_EXPLOSION_MINED, plugin);
        });

        // Clear list of 'exploded' blocks so the event won't affect them, as they are already mined by a player.
        blockList.clear();
    }

    // Do not damage around entities by en enchantment explosion.
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
