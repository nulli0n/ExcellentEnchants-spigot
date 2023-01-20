package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import com.google.common.collect.Sets;
import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.EffectUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

import java.util.Set;

public class EnchantCure extends ExcellentEnchant implements Chanced, CombatEnchant {

    public static final String ID = "cure";

    private ChanceImplementation chanceImplementation;

    private static final Set<EntityType> CUREABLE = Sets.newHashSet(EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOMBIE_VILLAGER);

    public EnchantCure(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.chanceImplementation = ChanceImplementation.create(this);
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(damager)) return false;
        if (!CUREABLE.contains(victim.getType())) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!(damager instanceof Player player)) return false;

        e.setCancelled(true);

        if (this.hasVisualEffects()) {
            EffectUtil.playEffect(victim.getEyeLocation(), Particle.CLOUD, "", 0.25, 0.25, 0.25, 0.1f, 30);
        }

        if (victim instanceof PigZombie pigZombie) {
            victim.getWorld().spawn(victim.getLocation(), Piglin.class);
            victim.remove();
        }
        else if (victim instanceof ZombieVillager zombieVillager) {
            zombieVillager.setConversionTime(1);
            zombieVillager.setConversionPlayer(player);
        }
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
