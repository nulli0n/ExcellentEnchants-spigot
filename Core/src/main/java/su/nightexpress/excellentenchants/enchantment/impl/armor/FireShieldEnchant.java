package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;
import su.nightexpress.nightcore.util.wrapper.UniSound;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_DURATION;

public class FireShieldEnchant extends GameEnchantment implements ChanceMeta, CombatEnchant {

    public static final String ID = "fire_shield";

    private Modifier fireDuration;

    public FireShieldEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.DESERT_COMMON));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            ENCHANTMENT_CHANCE + "% chance to ignite attackers for " + GENERIC_DURATION + "s.",
            EnchantRarity.RARE,
            3,
            ItemCategories.ARMOR
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(3, 2, 1, 100)));

        this.fireDuration = Modifier.read(config, "Settings.Fire.Duration",
            Modifier.multiply(2, 1, 1, 600),
            "Sets the fire duration (in seconds).",
            "If entity's current fire ticks amount is less than this value, it will be set to this value.",
            "If entity's current fire ticks amount is greater than this value, it won't be changed.");

        this.addPlaceholder(GENERIC_DURATION, level -> NumberUtil.format(this.getFireDuration(level)));
    }

    @NotNull
    @Override
    public EventPriority getProtectPriority() {
        return EventPriority.HIGHEST;
    }

    public double getFireDuration(int level) {
        return this.fireDuration.getValue(level);
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event,
                             @NotNull LivingEntity damager, @NotNull LivingEntity victim,
                             @NotNull ItemStack weapon, int level) {
        if (!this.checkTriggerChance(level)) return false;

        int ticksToSet = (int) (this.getFireDuration(level) * 20);
        int ticksHas = damager.getFireTicks();
        if (ticksHas >= ticksToSet) return false;

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.FLAME).play(victim.getEyeLocation(), 0.5, 0.1, 35);
            UniSound.of(Sound.ITEM_FIRECHARGE_USE).play(victim.getLocation());
        }

        damager.setFireTicks(ticksToSet);
        return true;
    }
}
