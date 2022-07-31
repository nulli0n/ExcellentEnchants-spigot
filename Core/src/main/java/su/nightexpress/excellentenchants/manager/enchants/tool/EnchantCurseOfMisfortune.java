package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

public class EnchantCurseOfMisfortune extends IEnchantChanceTemplate implements BlockBreakEnchant, DeathEnchant {

    private boolean dropExp;

    public static final String ID = "curse_of_misfortune";

    public EnchantCurseOfMisfortune(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.LOWEST);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.dropExp = cfg.getBoolean("Settings.Drop_Exp");
    }

    public boolean isDropExp() {
        return dropExp;
    }

    @Override
    @NotNull
    public FitItemType[] getFitItemTypes() {
        return new FitItemType[] {FitItemType.WEAPON, FitItemType.TOOL};
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }

    @Override
    public boolean isCursed() {
        return true;
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.isEnchantmentAvailable(player)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(player)) return false;

        e.setDropItems(false);
        if (!this.isDropExp()) e.setExpToDrop(0);
        return true;
    }

    @Override
    public boolean use(@NotNull EntityDeathEvent e, @NotNull LivingEntity dead, int level) {
        Player player = dead.getKiller();
        if (player == null) return false;
        if (!this.isEnchantmentAvailable(player)) return false;

        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(player)) return false;

        e.getDrops().clear();
        if (!this.isDropExp()) e.setDroppedExp(0);
        return true;
    }
}
