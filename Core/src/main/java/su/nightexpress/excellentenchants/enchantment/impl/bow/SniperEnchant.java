package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class SniperEnchant extends AbstractEnchantmentData implements BowEnchant, ChanceData {

    public static final String ID = "sniper";

    private ChanceSettingsImpl chanceSettings;
    private Modifier           speedModifier;

    public SniperEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);

        this.setDescription("Increases projectile speed by " + GENERIC_AMOUNT + "%");
        this.setMaxLevel(5);
        this.setRarity(Rarity.UNCOMMON);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config);

        this.speedModifier = Modifier.read(config, "Settings.Speed_Modifier",
            Modifier.add(1, 0.2, 1, 3D),
            "Sets projectile's speed modifier.");

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.getSpeedModifier(level) * 100D));
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return this.chanceSettings;
    }

    public double getSpeedModifier(int level) {
        return this.speedModifier.getValue(level);
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.BOW;
    }

    @NotNull
    @Override
    public EventPriority getShootPriority() {
        return EventPriority.LOWEST;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!this.checkTriggerChance(level)) return false;

        double modifier = this.getSpeedModifier(level);

        Entity entity = event.getProjectile();
        Vector vector = entity.getVelocity();
        entity.setVelocity(vector.multiply(modifier));

        return true;
    }

    @Override
    public boolean onHit(@NotNull ProjectileHitEvent event, LivingEntity user, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        return false;
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull Projectile projectile, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
