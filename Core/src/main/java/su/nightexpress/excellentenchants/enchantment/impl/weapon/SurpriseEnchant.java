package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.data.PotionData;
import su.nightexpress.excellentenchants.api.enchantment.data.PotionSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.excellentenchants.enchantment.data.PotionSettingsImpl;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Version;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class SurpriseEnchant extends AbstractEnchantmentData implements ChanceData, PotionData, CombatEnchant {

    public static final String ID = "surprise";

    private ChanceSettingsImpl chanceSettings;
    private PotionSettingsImpl potionSettings;

    public SurpriseEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to apply random potion effect to enemy on hit.");
        this.setMaxLevel(3);
        this.setRarity(Rarity.RARE);
    }

    @Override
    public boolean checkServerRequirements() {
        if (Version.isBehind(Version.V1_20_R2)) {
            this.error("Enchantment is available for 1.20.2+ only.");
            return false;
        }
        return true;
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(3, 2, 1));

        this.potionSettings = PotionSettingsImpl.create(this, config, PotionEffectType.BLINDNESS, false,
            Modifier.add(4, 1, 1, 900),
            Modifier.add(0, 1, 1, 10)
        );
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    @NotNull
    @Override
    public PotionSettings getPotionSettings() {
        return potionSettings;
    }

    @NotNull
    @Override
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
        if (!this.checkTriggerChance(level)) return false;

        PotionEffect effect = new PotionEffect(Rnd.get(BukkitThing.allFromRegistry(Registry.EFFECT)), this.getEffectDuration(level), Math.max(0, this.getEffectAmplifier(level) - 1), false, false);
        if (!victim.addPotionEffect(effect)) return false;

        if (this.hasVisualEffects()) {
            Color color = Color.fromRGB(Rnd.nextInt(256), Rnd.nextInt(256), Rnd.nextInt(256));
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 2f);
            UniParticle.of(Particle.REDSTONE, dustOptions).play(victim.getEyeLocation(), 0.25, 0.1, 25);
        }
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
