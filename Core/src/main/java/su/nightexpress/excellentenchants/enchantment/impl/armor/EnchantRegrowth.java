package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.task.AbstractEnchantmentTask;

import java.util.function.UnaryOperator;

public class EnchantRegrowth extends ExcellentEnchant implements PassiveEnchant, ICleanable {

    public static final String ID = "regrowth";

    private static final String PLACEHOLDER_HEAL_AMOUNT     = "%enchantment_heal_amount%";
    private static final String PLACEHOLDER_HEAL_MIN_HEALTH = "%enchantment_heal_min_health%";
    private static final String PLACEHOLDER_HEAL_MAX_HEALTH = "%enchantment_heal_max_health%";
    private static final String PLACEHOLDER_HEAL_INTERVAL   = "%enchantment_heal_interval%";

    private long          healInterval;
    private EnchantScaler healMinHealth;
    private EnchantScaler healMaxHealth;
    private EnchantScaler healAmount;

    private Task task;

    public EnchantRegrowth(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);

        this.task = new Task(plugin);
        this.task.start();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.healInterval = JOption.create("Settings.Heal.Interval", 100,
            "How often (in seconds) enchantment will have effect?").read(cfg);
        this.healMinHealth = EnchantScaler.read(this, "Settings.Heal.Min_Health", "0.5",
            "Minimal entity health for the enchantment to have effect.");
        this.healMaxHealth = EnchantScaler.read(this, "Settings.Heal.Max_Health", "20.0",
            "Maximal entity health when the enchantment will not heal anymore.");
        this.healAmount = EnchantScaler.read(this, "Settings.Heal.Amount", "0.25",
            "Amount of hearts to be restored.");
    }

    @Override
    public void clear() {
        if (this.task != null) {
            this.task.stop();
            this.task = null;
        }
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str)
            .replace(PLACEHOLDER_HEAL_AMOUNT, NumberUtil.format(this.getHealAmount(level)))
            .replace(PLACEHOLDER_HEAL_MIN_HEALTH, NumberUtil.format(this.getHealMaxHealth(level)))
            .replace(PLACEHOLDER_HEAL_MAX_HEALTH, NumberUtil.format(this.getHealMaxHealth(level)))
            .replace(PLACEHOLDER_HEAL_INTERVAL, NumberUtil.format((double) this.healInterval / 20D))
        ;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_TORSO;
    }

    public double getHealAmount(int level) {
        return this.healAmount.getValue(level);
    }

    public double getHealMinHealth(int level) {
        return this.healMinHealth.getValue(level);
    }

    public double getHealMaxHealth(int level) {
        return this.healMaxHealth.getValue(level);
    }

    public long getHealInterval() {
        return this.healInterval;
    }

    @Override
    public boolean onTrigger(@NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        if (!this.isAvailableToUse(entity)) return false;

        double healthMax = EntityUtil.getAttribute(entity, Attribute.GENERIC_MAX_HEALTH);
        double healthHas = entity.getHealth();
        if (healthHas < this.getHealMinHealth(level) || healthHas > this.getHealMaxHealth(level)) return false;
        if (healthHas >= healthMax) return false;

        double amount = Math.min(healthMax, healthHas + this.getHealAmount(level));
        entity.setHealth(amount);

        if (this.hasVisualEffects()) {
            EffectUtil.playEffect(entity.getEyeLocation(), Particle.HEART, "", 0.3, 0.3, 0.3, 0.1, 5);
        }
        return true;
    }

    class Task extends AbstractEnchantmentTask {

        public Task(@NotNull ExcellentEnchants plugin) {
            super(plugin, healInterval, false);
        }

        @Override
        public void action() {
            for (LivingEntity entity : this.getEntities()) {
                EnchantManager.getEquippedEnchants(entity, EnchantRegrowth.class).forEach((item, enchants) -> {
                    enchants.forEach((enchant, level) -> enchant.onTrigger(entity, item, level));
                });
            }
        }
    }
}
