package su.nightexpress.excellentenchants.manager.enchants.bow;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantBowTemplate;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantExplosiveArrows extends IEnchantBowTemplate {

    private boolean explosionFireSpread;
    private boolean explosionDamageItems;
    private boolean explosionDamageBlocks;
    private Scaler  explosionSize;

    public static final String ID                          = "explosive_arrows";
    public static final String PLACEHOLDER_EXPLOSION_POWER = "%enchantment_explosion_power%";

    private static final String META_EXPLOSION_SOURCE = ID + "_source";

    public EnchantExplosiveArrows(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.explosionFireSpread = cfg.getBoolean("Settings.Explosion.Fire_Spread");
        this.explosionDamageItems = cfg.getBoolean("Settings.Explosion.Damage_Items");
        this.explosionDamageBlocks = cfg.getBoolean("Settings.Explosion.Damage_Blocks");
        this.explosionSize = new EnchantScaler(this, "Settings.Explosion.Size");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        this.cfg.addMissing("Settings.Explosion.Fire_Spread", true);
        this.cfg.addMissing("Settings.Explosion.Damage_Items", true);
        this.cfg.addMissing("Settings.Explosion.Damage_Blocks", false);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_EXPLOSION_POWER, NumberUtil.format(this.getExplosionSize(level)))
        );
    }

    public final double getExplosionSize(int level) {
        return this.explosionSize.getValue(level);
    }

    public final boolean isExplosionFireSpread() {
        return this.explosionFireSpread;
    }

    public final boolean isExplosionDamageBlocks() {
        return this.explosionDamageBlocks;
    }

    @Override
    public boolean use(@NotNull ProjectileHitEvent e, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        if (!super.use(e, projectile, bow, level)) return false;

        Entity shooter = null;
        if (projectile.getShooter() instanceof Entity entity) {
            shooter = entity;
            shooter.setMetadata(META_EXPLOSION_SOURCE, new FixedMetadataValue(this.plugin, true));
        }

        World world = projectile.getWorld();
        float explSize = (float) this.getExplosionSize(level);
        boolean explFire = this.isExplosionFireSpread();
        boolean explBlocks = this.isExplosionDamageBlocks();
        boolean exploded = world.createExplosion(projectile.getLocation(), explSize, explFire, explBlocks, shooter);
        if (shooter != null) shooter.removeMetadata(META_EXPLOSION_SOURCE, this.plugin);
        return exploded;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDamage(EntityDamageByEntityEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return;
        if (this.explosionDamageItems) return;
        if (!e.getDamager().hasMetadata(META_EXPLOSION_SOURCE)) return;
        if (!(e.getEntity() instanceof Item item)) return;

        e.setCancelled(true);
    }
}
