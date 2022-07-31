package su.nightexpress.excellentenchants.manager.enchants.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantSelfDestruction extends IEnchantChanceTemplate implements DeathEnchant {

    private Scaler explosionSize;

    public static final String ID = "self_destruction";
    private static final String META_EXPLOSION_SOURCE = ID + "_explosion_source";
    private static final String PLACEHOLDER_EXPLOSION_POWER = "%enchantment_explosion_power%";

    public EnchantSelfDestruction(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.explosionSize = new EnchantScaler(this, "Settings.Explosion.Size");
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_EXPLOSION_POWER, NumberUtil.format(this.getExplosionSize(level)))
        );
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
    public boolean use(@NotNull EntityDeathEvent e, @NotNull LivingEntity dead, int level) {
        if (!this.isEnchantmentAvailable(dead)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(dead)) return false;

        float size = (float) this.getExplosionSize(level);
        dead.setMetadata(META_EXPLOSION_SOURCE, new FixedMetadataValue(plugin, true));
        boolean exploded = dead.getWorld().createExplosion(dead.getLocation(), size, false, false, dead);
        dead.removeMetadata(META_EXPLOSION_SOURCE, plugin);
        return exploded;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDamage(EntityDamageByEntityEvent e) {
        if (!e.getDamager().hasMetadata(META_EXPLOSION_SOURCE)) return;
        if (!(e.getEntity() instanceof Item item)) return;

        e.setCancelled(true);
    }
}
