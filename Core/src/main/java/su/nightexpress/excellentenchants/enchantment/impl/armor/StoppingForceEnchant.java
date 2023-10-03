package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

public class StoppingForceEnchant extends ExcellentEnchant implements Chanced, CombatEnchant {

    public static final String ID = "stopping_force";

    public static final String PLACEHOLDER_KNOCKBACK_RESISTANCE = "%knockback_resistance%";

    private ChanceImplementation chanceImplementation;
    private EnchantScaler knockbackModifier;

    public StoppingForceEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to resist knockback in combat by " + PLACEHOLDER_KNOCKBACK_RESISTANCE + "%.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.5);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this, "100.0");
        this.knockbackModifier = EnchantScaler.read(this, "Settings.Knockback_Modifier",
            "0.7 - " + Placeholders.ENCHANTMENT_LEVEL + " / 5.0",
            "Sets the knockback multiplier when taking damage.", "Lower value = less knockback.");

        this.addPlaceholder(PLACEHOLDER_KNOCKBACK_RESISTANCE, level -> NumberUtil.format(this.getKnockbackModifier(level) * 100));
    }

    public double getKnockbackModifier(int level) {
        return this.knockbackModifier.getValue(level);
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_LEGS;
    }

    @NotNull
    @Override
    public EventPriority getProtectPriority() {
        return EventPriority.HIGHEST;
    }

    @NotNull
    @Override
    public Chanced getChanceImplementation() {
        return this.chanceImplementation;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.checkTriggerChance(level)) return false;

        this.plugin.runTask(task -> {
            victim.setVelocity(victim.getVelocity().multiply(this.getKnockbackModifier(level)));
        });
        return true;
    }
}
