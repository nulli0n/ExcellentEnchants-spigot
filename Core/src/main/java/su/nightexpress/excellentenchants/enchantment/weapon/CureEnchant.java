package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.AttackEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;
import java.util.Set;

public class CureEnchant extends GameEnchantment implements AttackEnchant {

    private static final Set<EntityType> CUREABLE = Lists.newSet(EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOMBIE_VILLAGER);

    public CureEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(10, 10));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @Override
    @NotNull
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.MONITOR;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!CUREABLE.contains(victim.getType())) return false;
        if (!(damager instanceof Player player)) return false;
        if (event.getFinalDamage() < victim.getHealth()) return false;

        event.setCancelled(true);

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.CLOUD).play(victim.getEyeLocation(), 0.25, 0.1, 30);
        }

        if (victim instanceof PigZombie) {
            this.plugin.runTask(victim.getLocation(), () -> victim.getWorld().spawn(victim.getLocation(), Piglin.class));
            this.plugin.runTask(victim, victim::remove);
        }
        else if (victim instanceof ZombieVillager zombieVillager) {
            this.plugin.runTask(zombieVillager, () -> {
                zombieVillager.setConversionTime(1);
                zombieVillager.setConversionPlayer(player);
            });
        }
        return true;
    }
}
