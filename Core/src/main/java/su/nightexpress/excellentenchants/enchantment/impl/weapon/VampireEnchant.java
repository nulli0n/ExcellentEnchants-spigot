package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_AMOUNT;

public class VampireEnchant extends GameEnchantment implements ChanceMeta, CombatEnchant {

    public static final String ID = "vampire";

    private Modifier healAmount;
    private boolean  healMultiplier;

    public VampireEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.PLAINS_SPECIAL));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            ENCHANTMENT_CHANCE + "% chance to heal for " + GENERIC_AMOUNT + "❤ on hit.",
            EnchantRarity.LEGENDARY,
            4,
            ItemCategories.WEAPON
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(5, 2.5, 1)));

        this.healAmount = Modifier.read(config, "Settings.Heal.Amount",
            Modifier.add(0, 0.25, 1, 100),
            "Amount of health to be restored for attacker.");

        this.healMultiplier = ConfigValue.create("Settings.Heal.As_Multiplier",
            false,
            "When 'true', the option above will work as a multiplier of the inflicted damage."
        ).read(config);

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.isHealMultiplier() ? getHealAmount(level) * 100D : getHealAmount(level)));
    }

    public double getHealAmount(int level) {
        return this.healAmount.getValue(level);
    }

    public boolean isHealMultiplier() {
        return healMultiplier;
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
