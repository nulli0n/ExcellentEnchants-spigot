package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;
import java.util.Set;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;

public class CureEnchant extends GameEnchantment implements ChanceMeta, CombatEnchant {

    public static final String ID = "cure";

    private static final Set<EntityType> CUREABLE = Lists.newSet(EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOMBIE_VILLAGER);

    public CureEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.DESERT_COMMON));
    }

    // TODO On kill only

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            ENCHANTMENT_CHANCE + "% chance to cure Zombified Piglins and Zombie Villagers on hit.",
            EnchantRarity.LEGENDARY,
            5,
            ItemCategories.WEAPON
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(4, 2.2, 1, 100)));
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
