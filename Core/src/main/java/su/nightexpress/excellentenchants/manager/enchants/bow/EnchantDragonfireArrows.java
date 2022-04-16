package su.nightexpress.excellentenchants.manager.enchants.bow;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantBowTemplate;
import su.nightexpress.excellentenchants.manager.EnchantRegister;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantDragonfireArrows extends IEnchantBowTemplate {

    public static final String ID = "dragonfire_arrows";

    public static final String PLACEHOLDER_FIRE_RADIUS = "%enchantment_fire_radius%";
    public static final String PLACEHOLDER_FIRE_DURATION = "%enchantment_fire_duration%";

    private final EnchantScaler fireDuration;
    private final EnchantScaler fireRadius;

    public EnchantDragonfireArrows(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);

        this.fireDuration = new EnchantScaler(this, "Settings.Fire.Duration");
        this.fireRadius = new EnchantScaler(this, "Settings.Fire.Radius");
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(EnchantRegister.CONFUSING_ARROWS);
        this.addConflict(EnchantRegister.POISONED_ARROWS);
        this.addConflict(EnchantRegister.EXPLOSIVE_ARROWS);
        this.addConflict(EnchantRegister.WITHERED_ARROWS);
        this.addConflict(EnchantRegister.ELECTRIFIED_ARROWS);
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
    public boolean use(@NotNull ProjectileHitEvent e, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        if (!super.use(e, projectile, bow, level)) return false;

        World world = projectile.getWorld();
        AreaEffectCloud cloud = world.spawn(projectile.getLocation(), AreaEffectCloud.class);
        cloud.clearCustomEffects();
        cloud.setSource(projectile.getShooter());
        cloud.setParticle(Particle.DRAGON_BREATH);
        cloud.setRadius((float) this.getFireRadius(level));
        cloud.setDuration(this.getFireDuration(level));
        cloud.setRadiusPerTick((7.0F - cloud.getRadius()) / (float) cloud.getDuration());
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 1, 1), true);

        return true;
    }
}
