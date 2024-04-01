package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class VampireEnchant extends AbstractEnchantmentData implements ChanceData, CombatEnchant {

    public static final String ID = "vampire";

    private Modifier           healAmount;
    private boolean            healMultiplier;
    private ChanceSettingsImpl chanceSettings;

    public VampireEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to heal for " + GENERIC_AMOUNT + "❤ on hit.");
        this.setMaxLevel(4);
        this.setRarity(Rarity.RARE);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(5, 2.5, 1));

        this.healAmount = Modifier.read(config, "Settings.Heal.Amount",
            Modifier.add(0, 0.25, 1, 100),
            "Amount of health to be restored for attacker.");

        this.healMultiplier = ConfigValue.create("Settings.Heal.As_Multiplier",
            false,
            "When 'true', the option above will work as a multiplier of the inflicted damage."
        ).read(config);

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.isHealMultiplier() ? getHealAmount(level) * 100D : getHealAmount(level)));
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    public double getHealAmount(int level) {
        return this.healAmount.getValue(level);
    }

    public boolean isHealMultiplier() {
        return healMultiplier;
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.WEAPON;
    }

    @NotNull
    @Override
    public EventPriority getAttackPriority() {
        return EventPriority.MONITOR;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        double healthMax = EntityUtil.getAttribute(damager, Attribute.GENERIC_MAX_HEALTH);
        double healthHas = damager.getHealth();
        if (healthHas == healthMax) return false;

        if (!this.checkTriggerChance(level)) return false;

        double healAmount = this.getHealAmount(level);
        double healFinal = this.isHealMultiplier() ? event.getFinalDamage() * healAmount : healAmount;

        EntityRegainHealthEvent healthEvent = new EntityRegainHealthEvent(damager, healFinal, EntityRegainHealthEvent.RegainReason.CUSTOM);
        plugin.getPluginManager().callEvent(healthEvent);
        if (healthEvent.isCancelled()) return false;

        damager.setHealth(Math.min(healthMax, healthHas + healthEvent.getAmount()));

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.HEART).play(damager.getEyeLocation(), 0.25, 0.15, 5);
        }
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
