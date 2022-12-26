package su.nightexpress.excellentenchants.manager.enchants.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.ArrayUtil;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.Scaler;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantElementalProtection extends IEnchantChanceTemplate {

    public static final String ID                            = "elemental_protection";
    public static final String PLACEHOLDER_PROTECTION_AMOUNT = "%enchantment_protection_amount%";
    public static final String PLACEHOLDER_PROTECTION_CAPACITY = "%enchantment_protection_capacity%";

    private static final EntityDamageEvent.DamageCause[] DAMAGE_CAUSES = new EntityDamageEvent.DamageCause[] {
        EntityDamageEvent.DamageCause.POISON, EntityDamageEvent.DamageCause.WITHER,
        EntityDamageEvent.DamageCause.MAGIC, EntityDamageEvent.DamageCause.FREEZE,
        /*EntityDamageEvent.DamageCause.SONIC_BOOM, */EntityDamageEvent.DamageCause.LIGHTNING,
    };

    private Scaler protectionAmount;
    private double protectionCapacity;
    private boolean protectionAsModifier;

    public EnchantElementalProtection(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_PROTECTION_AMOUNT, NumberUtil.format(this.getProtectionAmount(level)))
            .replace(PLACEHOLDER_PROTECTION_CAPACITY, NumberUtil.format(this.getProtectionCapacity()))
        );
    }

    @Override
    public void loadConfig() {
        super.loadConfig();

        this.protectionAmount = new EnchantScaler(this, "Settings.Protection.Amount");
        this.protectionCapacity = cfg.getDouble("Settings.Protection.Capacity");
        this.protectionAsModifier = cfg.getBoolean("Settings.Protection.As_Modifier");
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR;
    }

    public double getProtectionAmount(int level) {
        return this.protectionAmount.getValue(level);
    }

    public double getProtectionCapacity() {
        return this.protectionCapacity;
    }

    public boolean isProtectionAsModifier() {
        return this.protectionAsModifier;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!ArrayUtil.contains(DAMAGE_CAUSES, e.getCause())) return;
        if (!(e.getEntity() instanceof LivingEntity victim)) return;
        if (!this.isEnchantmentAvailable(victim)) return;

        double protectionAmount = 0D;
        for (ItemStack armor : EntityUtil.getEquippedArmor(victim).values()) {
            if (armor == null || armor.getType().isAir()) continue;

            int level = EnchantManager.getItemEnchantLevel(armor, this);
            if (this.checkTriggerChance(level)) {
                protectionAmount += this.getProtectionAmount(level);
            }
        }
        if (protectionAmount <= 0D) return;
        if (!this.takeCostItem(victim)) return;

        if (protectionAmount > this.getProtectionCapacity()) {
            protectionAmount = this.getProtectionCapacity();
        }

        if (this.isProtectionAsModifier()) {
            e.setDamage(Math.max(0, e.getDamage() * (1D - protectionAmount)));
        }
        else {
            e.setDamage(Math.max(0, e.getDamage() - protectionAmount));
        }
    }
}
