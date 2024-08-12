package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.PotionEffects;
import su.nightexpress.excellentenchants.api.enchantment.meta.PotionMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;

public class SurpriseEnchant extends GameEnchantment implements ChanceMeta, PotionMeta, CombatEnchant {

    public static final String ID = "surprise";

    public SurpriseEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.TAIGA_COMMON));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            ENCHANTMENT_CHANCE + "% chance to apply random potion effect to enemy on hit.",
            EnchantRarity.RARE,
            3,
            ItemCategories.WEAPON
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(3, 2, 1)));

        this.meta.setPotionEffects(PotionEffects.create(this, config, PotionEffectType.BLINDNESS, false,
            Modifier.add(4, 1, 1, 900),
            Modifier.add(0, 1, 1, 10)
        ));
    }

    @NotNull
    @Override
    public EventPriority getAttackPriority() {
        return EventPriority.HIGHEST;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.checkTriggerChance(level)) return false;

        PotionEffect effect = new PotionEffect(Rnd.get(BukkitThing.allFromRegistry(Registry.EFFECT)), this.getEffectDuration(level), Math.max(0, this.getEffectAmplifier(level) - 1), false, false);
        if (!victim.addPotionEffect(effect)) return false;

        if (this.hasVisualEffects()) {
            Color color = Color.fromRGB(Rnd.nextInt(256), Rnd.nextInt(256), Rnd.nextInt(256));
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 2f);
            UniParticle.of(Particle.DUST, dustOptions).play(victim.getEyeLocation(), 0.25, 0.1, 25);
        }
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
