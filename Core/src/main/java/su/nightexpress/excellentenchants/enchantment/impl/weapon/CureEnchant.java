package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import com.google.common.collect.Sets;
import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;
import java.util.Set;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class CureEnchant extends AbstractEnchantmentData implements ChanceData, CombatEnchant {

    public static final String ID = "cure";

    private ChanceSettingsImpl chanceSettings;

    private static final Set<EntityType> CUREABLE = Sets.newHashSet(EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOMBIE_VILLAGER);

    public CureEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to cure Zombified Piglins and Zombie Villagers on hit.");
        this.setMaxLevel(5);
        this.setRarity(Rarity.RARE);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(4, 2.2, 1, 100));
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.WEAPON;
    }

    @NotNull
    @Override
    public EventPriority getAttackPriority() {
        return EventPriority.HIGHEST;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!CUREABLE.contains(victim.getType())) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!(damager instanceof Player player)) return false;

        event.setCancelled(true);

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.CLOUD).play(victim.getEyeLocation(), 0.25, 0.1, 30);
        }

        if (victim instanceof PigZombie) {
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
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
