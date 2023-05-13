package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantSelfDestruction extends ExcellentEnchant implements Chanced, DeathEnchant {

    public static final String ID = "self_destruction";

    private static final String META_EXPLOSION_SOURCE       = ID + "_explosion_source";
    private static final String PLACEHOLDER_EXPLOSION_POWER = "%enchantment_explosion_power%";

    private EnchantScaler explosionSize;
    private ChanceImplementation chanceImplementation;

    public EnchantSelfDestruction(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("%enchantment_trigger_chance%% chance to create an explosion on death.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.3);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "20.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 10");
        this.explosionSize = EnchantScaler.read(this, "Settings.Explosion.Size",
            "1.0" + Placeholders.ENCHANTMENT_LEVEL,
            "A size of the explosion. The more size - the bigger the damage.");

        this.addPlaceholder(PLACEHOLDER_EXPLOSION_POWER, level -> NumberUtil.format(this.getExplosionSize(level)));
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_TORSO;
    }

    public final double getExplosionSize(int level) {
        return this.explosionSize.getValue(level);
    }

    @Override
    public boolean onDeath(@NotNull EntityDeathEvent e, @NotNull LivingEntity entity, int level) {
        if (!this.isAvailableToUse(entity)) return false;
        if (!this.checkTriggerChance(level)) return false;

        float size = (float) this.getExplosionSize(level);
        entity.setMetadata(META_EXPLOSION_SOURCE, new FixedMetadataValue(plugin, true));
        boolean exploded = entity.getWorld().createExplosion(entity.getLocation(), size, false, false, entity);
        entity.removeMetadata(META_EXPLOSION_SOURCE, plugin);
        return exploded;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent e, @NotNull LivingEntity entity, @NotNull Player killer, int level) {
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDamage(EntityDamageByEntityEvent e) {
        if (!e.getDamager().hasMetadata(META_EXPLOSION_SOURCE)) return;
        if (!(e.getEntity() instanceof Item item)) return;

        e.setCancelled(true);
    }
}
