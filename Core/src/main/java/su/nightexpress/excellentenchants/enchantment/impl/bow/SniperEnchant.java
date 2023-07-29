package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class SniperEnchant extends ExcellentEnchant implements BowEnchant, Chanced {

    public static final String ID = "sniper";

    private static final String PLACEHOLDER_PROJECTILE_SPEED = "%enchantment_projectile_speed%";

    private ChanceImplementation chanceImplementation;
    private EnchantScaler speedModifier;

    public SniperEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.LOWEST);

        this.getDefaults().setDescription("Increases projectile speed by " + PLACEHOLDER_PROJECTILE_SPEED + "%");
        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(0.3);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

        this.chanceImplementation = ChanceImplementation.create(this, "100.0");
        this.speedModifier = EnchantScaler.read(this, "Settings.Speed_Modifier",
            "1.0 + " + Placeholders.ENCHANTMENT_LEVEL + " / 5.0", "Sets projectile's speed modifier.");

        this.addPlaceholder(PLACEHOLDER_PROJECTILE_SPEED, level -> NumberUtil.format(this.getSpeedModifier(level) * 100D));
    }

    public double getSpeedModifier(int level) {
        return this.speedModifier.getValue(level);
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
    }

    @NotNull
    @Override
    public Chanced getChanceImplementation() {
        return this.chanceImplementation;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent e, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!this.isAvailableToUse(shooter)) return false;
        if (!this.checkTriggerChance(level)) return false;

        double modifier = this.getSpeedModifier(level);

        Entity entity = e.getProjectile();
        Vector vector = entity.getVelocity();
        entity.setVelocity(vector.multiply(modifier));

        return true;
    }

    @Override
    public boolean onHit(@NotNull ProjectileHitEvent e, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        return false;
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent e, @NotNull Projectile projectile, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
