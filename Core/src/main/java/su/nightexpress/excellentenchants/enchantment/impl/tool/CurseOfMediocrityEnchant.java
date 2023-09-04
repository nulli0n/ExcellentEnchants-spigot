package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class CurseOfMediocrityEnchant extends ExcellentEnchant implements Chanced, BlockDropEnchant, DeathEnchant {

    public static final String ID = "curse_of_mediocrity";

    private ChanceImplementation chanceImplementation;

    public CurseOfMediocrityEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.LOWEST);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to disenchant item drops.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0D);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this, "25.0 * " + Placeholders.ENCHANTMENT_LEVEL);
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }

    @Override
    @NotNull
    public FitItemType[] getFitItemTypes() {
        return new FitItemType[] {FitItemType.WEAPON, FitItemType.TOOL};
    }

    @NotNull
    @Override
    public Chanced getChanceImplementation() {
        return this.chanceImplementation;
    }

    @Override
    public boolean isCursed() {
        return true;
    }

    @Override
    public boolean onDrop(@NotNull BlockDropItemEvent event,
                          @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.isAvailableToUse(player)) return false;
        if (!this.checkTriggerChance(level)) return false;

        event.getItems().forEach(drop -> {
            ItemStack stack = drop.getItemStack();
            ItemUtil.mapMeta(stack, meta -> {
                meta.getEnchants().keySet().forEach(meta::removeEnchant);
            });
            drop.setItemStack(stack);
        });

        return true;
    }

    @Override
    public boolean onDeath(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, ItemStack item, int level) {
        return false;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull Player killer, int level) {
        if (!this.isAvailableToUse(killer)) return false;
        if (!this.checkTriggerChance(level)) return false;

        event.getDrops().forEach(stack -> {
            ItemUtil.mapMeta(stack, meta -> {
                meta.getEnchants().keySet().forEach(meta::removeEnchant);
            });
        });

        return true;
    }
}
