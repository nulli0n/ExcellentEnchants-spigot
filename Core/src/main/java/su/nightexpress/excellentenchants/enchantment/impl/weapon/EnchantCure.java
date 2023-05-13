package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import com.google.common.collect.Sets;
import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.particle.SimpleParticle;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

import java.util.Set;

public class EnchantCure extends ExcellentEnchant implements Chanced, CombatEnchant {

    public static final String ID = "cure";

    private ChanceImplementation chanceImplementation;

    private static final Set<EntityType> CUREABLE = Sets.newHashSet(EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOMBIE_VILLAGER);

    public EnchantCure(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to cure Zombified Piglins and Zombie Villagers on hit.");
        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(0.5);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "20.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 8");
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
            SimpleParticle.of(Particle.CLOUD).play(victim.getEyeLocation(), 0.25, 0.1, 30);
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
