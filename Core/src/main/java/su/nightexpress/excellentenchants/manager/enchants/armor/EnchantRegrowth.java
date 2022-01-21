package su.nightexpress.excellentenchants.manager.enchants.armor;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;
import su.nightexpress.excellentenchants.manager.tasks.AbstractEnchantPassiveTask;

import java.util.function.UnaryOperator;

public class EnchantRegrowth extends IEnchantChanceTemplate implements PassiveEnchant, ICleanable {

    public static final String ID = "regrowth";

    private final String particleName;
    private final String particleData;
    private final long   healthInterval;
    private final Scaler healthAmount;
    private Task healthTask;

    private static final String PLACEHOLDER_HEALTH_AMOUNT   = "%enchantment_health_amount%";
    private static final String PLACEHOLDER_HEALTH_INTERVAL = "%enchantment_health_interval%";

    public EnchantRegrowth(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
        this.particleName = cfg.getString("Settings.Particle.Name", Particle.HEART.name());
        this.particleData = cfg.getString("Settings.Particle.Data", "");
        this.healthInterval = cfg.getLong("Settings.Health.Interval", 100);
        this.healthAmount = new EnchantScaler(this, "Settings.Health.Amount");
        this.healthTask = new Task(plugin);
        this.healthTask.start();
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.addMissing("Settings.Health.Interval", 100);
        cfg.addMissing("Settings.Particle.Name", Particle.HEART.name());
        cfg.addMissing("Settings.Particle.Data", "");
    }

    @Override
    public void clear() {
        if (this.healthTask != null) {
            this.healthTask.stop();
            this.healthTask = null;
        }
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_HEALTH_AMOUNT, NumberUtil.format(this.getHealthAmount(level)))
            .replace(PLACEHOLDER_HEALTH_INTERVAL, NumberUtil.format((double) this.healthInterval / 20D))
        );
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_TORSO;
    }

    public double getHealthAmount(int level) {
        return this.healthAmount.getValue(level);
    }

    public long getHealthInterval() {
        return this.healthInterval;
    }

    @Override
    public boolean use(@NotNull LivingEntity entity, int level) {
        if (!this.isEnchantmentAvailable(entity)) return false;
        if (!this.checkTriggerChance(level)) return false;

        double healthMax = EntityUtil.getAttribute(entity, Attribute.GENERIC_MAX_HEALTH);
        double healthHas = entity.getHealth();
        if (healthHas >= healthMax) return false;
        if (!this.takeCostItem(entity)) return false;

        double amount = Math.min(healthMax, healthHas + this.getHealthAmount(level));

        entity.setHealth(amount);
        EffectUtil.playEffect(entity.getEyeLocation(), this.particleName, this.particleData, 0.3, 0.3, 0.3, 0.1, 15);
        return true;
    }

    class Task extends AbstractEnchantPassiveTask {

        public Task(@NotNull ExcellentEnchants plugin) {
            super(plugin, healthInterval, false);
        }

        @Override
        protected void apply(@NotNull LivingEntity entity, @NotNull ItemStack armor, @NotNull ItemMeta meta) {
            int level = meta.getEnchantLevel(EnchantRegrowth.this);
            if (level < 1) return;

            use(entity, level);
        }
    }
}
