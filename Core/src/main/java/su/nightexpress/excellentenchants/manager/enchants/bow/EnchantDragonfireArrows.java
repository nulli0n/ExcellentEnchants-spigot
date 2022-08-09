package su.nightexpress.excellentenchants.manager.enchants.bow;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantBowTemplate;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantDragonfireArrows extends IEnchantBowTemplate {

    public static final String ID = "dragonfire_arrows";

    public static final String PLACEHOLDER_FIRE_RADIUS = "%enchantment_fire_radius%";
    public static final String PLACEHOLDER_FIRE_DURATION = "%enchantment_fire_duration%";

    private EnchantScaler fireDuration;
    private EnchantScaler fireRadius;

    public EnchantDragonfireArrows(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.fireDuration = new EnchantScaler(this, "Settings.Fire.Duration");
        this.fireRadius = new EnchantScaler(this, "Settings.Fire.Radius");
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_FIRE_DURATION, NumberUtil.format(this.getFireDuration(level) / 20D))
            .replace(PLACEHOLDER_FIRE_RADIUS, NumberUtil.format(this.getFireRadius(level)))
        );
    }

    public int getFireDuration(int level) {
        return (int) this.fireDuration.getValue(level);
    }

    public double getFireRadius(int level) {
        return this.fireRadius.getValue(level);
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!super.use(e, damager, victim, weapon, level)) return false;

        this.createCloud(damager, victim.getLocation(), level);
        return true;
    }

    @Override
    public boolean use(@NotNull ProjectileHitEvent e, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        if (!super.use(e, projectile, bow, level)) return false;

        this.createCloud(projectile.getShooter(), projectile.getLocation() , level);
        return true;
    }

    private void createCloud(@Nullable ProjectileSource shooter, @NotNull Location location, int level) {
        World world = location.getWorld();
        if (world == null) return;

        AreaEffectCloud cloud = world.spawn(location, AreaEffectCloud.class);
        cloud.clearCustomEffects();
        cloud.setSource(shooter);
        cloud.setParticle(Particle.DRAGON_BREATH);
        cloud.setRadius((float) this.getFireRadius(level));
        cloud.setDuration(this.getFireDuration(level));
        cloud.setRadiusPerTick((7.0F - cloud.getRadius()) / (float) cloud.getDuration());
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 1, 1), true);
    }
}
