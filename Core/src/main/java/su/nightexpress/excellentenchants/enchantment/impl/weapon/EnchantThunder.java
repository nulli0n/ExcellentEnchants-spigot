package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

public class EnchantThunder extends ExcellentEnchant implements Chanced, CombatEnchant {

    public static final String ID = "thunder";
    
    private static final String META_NO_ITEM_DAMAGE = "noItemDamage";

    private boolean inThunderstormOnly;
    private ChanceImplementation chanceImplementation;

    public EnchantThunder(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.chanceImplementation = ChanceImplementation.create(this);
        this.inThunderstormOnly = JOption.create("Settings.During_Thunderstorm_Only", false,
            "When 'true' the enchantment will be triggered only if there is an active thunderstorm in the world.").read(cfg);
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    public boolean isInThunderstormOnly() {
        return inThunderstormOnly;
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(damager)) return false;
        if (this.isInThunderstormOnly() && !victim.getWorld().isThundering()) return false;
        if (victim.getLocation().getBlock().getLightFromSky() != 15) return false;
        if (!this.checkTriggerChance(level)) return false;

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (victim.isDead()) return;
            victim.setNoDamageTicks(0);
            victim.getWorld().strikeLightning(victim.getLocation()).setMetadata(META_NO_ITEM_DAMAGE, new FixedMetadataValue(plugin, true));
        });

        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDamage(EntityDamageByEntityEvent e) {
        if (!e.getDamager().hasMetadata(META_NO_ITEM_DAMAGE)) return;
        if (!(e.getEntity() instanceof Item item)) return;

        e.setCancelled(true);
        item.setFireTicks(0);
    }
}
