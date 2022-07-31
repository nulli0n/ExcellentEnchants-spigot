package su.nightexpress.excellentenchants.manager.enchants.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;
import su.nightexpress.excellentenchants.manager.tasks.AbstractEnchantPassiveTask;

import java.util.function.UnaryOperator;

public class EnchantSaturation extends IEnchantChanceTemplate implements PassiveEnchant, ICleanable {

    private long saturationInterval;
    private Scaler saturationAmount;
    private Task         saturationTask;

    public static final String ID = "saturation";

    private static final String PLACEHOLDER_SATURATION_AMOUNT = "%enchantment_saturation_amount%";
    private static final String PLACEHOLDER_SATURATION_INTERVAL = "%enchantment_saturation_interval%";

    public EnchantSaturation(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);

        this.saturationTask = new Task(plugin);
        this.saturationTask.start();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.saturationInterval = cfg.getLong("Settings.Saturation.Interval", 100);
        this.saturationAmount = new EnchantScaler(this, "Settings.Saturation.Amount");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.addMissing("Settings.Saturation.Interval", 100);
    }

    @Override
    public void clear() {
        if (this.saturationTask != null) {
            this.saturationTask.stop();
            this.saturationTask = null;
        }
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_SATURATION_AMOUNT, NumberUtil.format(this.getSaturationAmount(level)))
            .replace(PLACEHOLDER_SATURATION_INTERVAL, NumberUtil.format((double) this.saturationInterval / 20D))
        );
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_HEAD;
    }

    public final double getSaturationAmount(int level) {
        return this.saturationAmount.getValue(level);
    }

    public long getSaturationInterval() {
        return saturationInterval;
    }

    @Override
    public boolean use(@NotNull LivingEntity entity, int level) {
        if (!this.isEnchantmentAvailable(entity)) return false;
        if (!(entity instanceof Player player)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (player.getFoodLevel() >= 20) return false;
        if (!this.takeCostItem(player)) return false;

        float amount = (float) this.getSaturationAmount(level);
        player.setFoodLevel((int) Math.min(20, player.getFoodLevel() + amount));
        player.setSaturation(Math.min(20, player.getSaturation() + amount));
        return true;
    }

    class Task extends AbstractEnchantPassiveTask {

        public Task(@NotNull ExcellentEnchants plugin) {
            super(plugin, saturationInterval, false);
        }

        @Override
        protected void apply(@NotNull LivingEntity entity, @NotNull ItemStack armor, @NotNull ItemMeta meta) {
            int level = meta.getEnchantLevel(EnchantSaturation.this);
            if (level < 1) return;

            use(entity, level);
        }
    }
}
