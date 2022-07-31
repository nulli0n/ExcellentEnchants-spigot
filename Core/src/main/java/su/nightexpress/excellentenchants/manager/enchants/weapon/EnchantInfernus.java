package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantInfernus extends IEnchantChanceTemplate {

    private Scaler fireTicks;

    public static final String ID = "infernus";
    public static final String PLACEHOLDER_FIRE_DURATION = "%enchantment_fire_duration%";

    public EnchantInfernus(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.fireTicks = new EnchantScaler(this, "Settings.Fire_Ticks");
    }

    public int getFireTicks(int level) {
        return (int) this.fireTicks.getValue(level);
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_FIRE_DURATION, NumberUtil.format((double) this.getFireTicks(level) / 20D))
        );
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TRIDENT;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInfernusTridentLaunch(ProjectileLaunchEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof LivingEntity shooter)) return;
        if (!this.isEnchantmentAvailable(shooter)) return;

        ItemStack item = trident.getItem();

        int level = item.getEnchantmentLevel(this);
        if (level <= 0) return;

        if (!this.checkTriggerChance(level)) return;
        if (!this.takeCostItem(shooter)) return;
        trident.setFireTicks(Integer.MAX_VALUE);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInfernusDamageApply(EntityDamageByEntityEvent e) {
        Entity entity = e.getDamager();
        if (!(entity instanceof Trident trident)) return;

        ItemStack item = trident.getItem();

        int level = item.getEnchantmentLevel(this);
        if (level <= 0 || trident.getFireTicks() <= 0) return;

        int ticks = this.getFireTicks(level);
        e.getEntity().setFireTicks(ticks);
    }
}
