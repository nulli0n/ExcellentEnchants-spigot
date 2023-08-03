package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;

public class EnchantCurseOfMisfortune extends ExcellentEnchant implements Chanced, BlockBreakEnchant, DeathEnchant {

    public static final String ID = "curse_of_misfortune";

    private boolean dropExp;
    private ChanceImplementation chanceImplementation;

    public EnchantCurseOfMisfortune(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.LOWEST);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to have no drops from blocks or mobs.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0D);
        this.getDefaults().setConflicts(
            Enchantment.LOOT_BONUS_BLOCKS.getKey().getKey(),
            Enchantment.LOOT_BONUS_MOBS.getKey().getKey()
        );
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "20.0 * " + Placeholders.ENCHANTMENT_LEVEL);
        this.dropExp = JOption.create("Settings.Drop_Exp", false,
            "When 'true' allows to drop exp from mobs/blocks.").read(cfg);
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
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
    public boolean onBreak(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.isAvailableToUse(player)) return false;
        if (!this.checkTriggerChance(level)) return false;

        e.setDropItems(false);
        if (!this.isDropExp()) e.setExpToDrop(0);
        return true;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull Player killer, int level) {
        if (!this.isAvailableToUse(killer)) return false;
        if (!this.checkTriggerChance(level)) return false;

        event.getDrops().clear();
        if (!this.isDropExp()) event.setDroppedExp(0);
        return true;
    }

    @Override
    public boolean onDeath(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, ItemStack item, int level) {
        return false;
    }
}
