package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.type.GenericEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.SimpeListener;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;
import java.util.Set;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class ElementalProtectionEnchant extends AbstractEnchantmentData implements SimpeListener, GenericEnchant {

    public static final String ID = "elemental_protection";

    private static final Set<EntityDamageEvent.DamageCause> DAMAGE_CAUSES = Set.of(
        EntityDamageEvent.DamageCause.POISON, EntityDamageEvent.DamageCause.WITHER,
        EntityDamageEvent.DamageCause.MAGIC, EntityDamageEvent.DamageCause.FREEZE,
        EntityDamageEvent.DamageCause.LIGHTNING);

    private Modifier protectionAmount;
    private double   protectionCapacity;
    private boolean  protectionAsModifier;

    public ElementalProtectionEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription("Reduces Poison, Magic, Wither, Lightning, Freeze damage by " + GENERIC_AMOUNT + "%.");
        this.setMaxLevel(5);
        this.setRarity(Rarity.COMMON);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.protectionAmount = Modifier.read(config, "Settings.Protection.Amount",
            Modifier.add(0, 5, 1, 100),
            "Protection amount given by enchantment.");

        this.protectionCapacity = ConfigValue.create("Settings.Protection.Capacity",
            100D,
            "Maximal possible protection value from all armor pieces together.").read(config);

        this.protectionAsModifier = ConfigValue.create("Settings.Protection.As_Modifier",
            true,
            "Determines if Protection value is percent based or plain amount."
        ).read(config);

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.getProtectionAmount(level)));
        this.addPlaceholder(GENERIC_MAX, level -> NumberUtil.format(this.getProtectionCapacity()));
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
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
    public void onDamage(EntityDamageEvent event) {
        if (!DAMAGE_CAUSES.contains(event.getCause())) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!this.isAvailableToUse(entity)) return;

        double protectionAmount = 0D;
        for (ItemStack armor : EnchantUtils.getEnchantedEquipment(entity).values()) {
            int level = EnchantUtils.getLevel(armor, this.getEnchantment());
            if (level <= 0) continue;

            protectionAmount += this.getProtectionAmount(level);
            this.consumeCharges(armor, level);
        }

        if (protectionAmount <= 0D) return;
        if (protectionAmount > this.getProtectionCapacity()) {
            protectionAmount = this.getProtectionCapacity();
        }

        double damage = event.getDamage();
        double blocked = this.isProtectionAsModifier() ? damage * (1D - protectionAmount) : damage - protectionAmount;

        event.setDamage(Math.max(0, blocked));
    }
}
