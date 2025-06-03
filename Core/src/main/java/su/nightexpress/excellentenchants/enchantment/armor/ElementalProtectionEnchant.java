package su.nightexpress.excellentenchants.enchantment.armor;

import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.*;
import su.nightexpress.excellentenchants.api.damage.DamageBonusType;
import su.nightexpress.excellentenchants.api.damage.DamageBonus;
import su.nightexpress.excellentenchants.api.enchantment.type.ProtectionEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;
import java.util.Set;

public class ElementalProtectionEnchant extends GameEnchantment implements ProtectionEnchant {

    private static final Set<DamageType> DAMAGE_CAUSES = Lists.newSet(
        DamageType.WITHER,
        DamageType.MAGIC,
        DamageType.FREEZE,
        DamageType.LIGHTNING_BOLT
    );

    private Modifier amount;
    private double   capacity;
    private boolean  multiplier;

    public ElementalProtectionEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.amount = Modifier.load(config, "Protection.Amount",
            Modifier.addictive(0).perLevel(5).capacity(25),
            "Protection amount given by enchantment."
        );

        this.capacity = ConfigValue.create("Protection.Capacity",
            80D,
            "Max. possible protection value from all armor pieces."
        ).read(config);

        this.multiplier = ConfigValue.create("Protection.Multiplier",
            true,
            "Controls if protection amount is in percent."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getProtectionAmount(level)));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_MAX, level -> NumberUtil.format(this.getCapacity()));
    }

    public double getProtectionAmount(int level) {
        return this.amount.getValue(level);
    }

    public double getCapacity() {
        return this.capacity;
    }

    public boolean isMultiplier() {
        return this.multiplier;
    }

    @Override
    @NotNull
    public EnchantPriority getProtectionPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    @NotNull
    public DamageBonus getDamageBonus() {
        return new DamageBonus(this.multiplier ? DamageBonusType.MULTIPLIER : DamageBonusType.NORMAL);
    }

    @Override
    public boolean onProtection(@NotNull EntityDamageEvent event, @NotNull DamageBonus damageBonus, @NotNull LivingEntity entity, @NotNull ItemStack itemStack, int level) {
        DamageSource source = event.getDamageSource();
        DamageType type = source.getDamageType();
        if (!DAMAGE_CAUSES.contains(type)) return false;

        double protectionAmount = this.getProtectionAmount(level);
        if (protectionAmount <= 0D) return false;

        damageBonus.addPenalty(protectionAmount, this.capacity);
        return true;
    }
}
