package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
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
import su.nightexpress.nightcore.util.wrapper.UniParticle;
import su.nightexpress.nightcore.util.wrapper.UniSound;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class IceShieldEnchant extends AbstractEnchantmentData implements ChanceData, PotionData, CombatEnchant {

    public static final String ID = "ice_shield";

    private ChanceSettingsImpl chanceSettings;
    private PotionSettingsImpl potionSettings;

    public IceShieldEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to freeze and apply " + ENCHANTMENT_POTION_TYPE + " " + ENCHANTMENT_POTION_LEVEL + " (" + ENCHANTMENT_POTION_DURATION + "s.) on attacker.");
        this.setMaxLevel(3);
        this.setRarity(Rarity.COMMON);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(4, 4, 1, 100));

        this.potionSettings = PotionSettingsImpl.create(this, config, PotionEffectType.SLOW, false,
            Modifier.add(2, 2, 1, 300),
            Modifier.add(0, 1, 1, 5)
        );
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.ARMOR_TORSO;
    }

    @NotNull
    @Override
    public EventPriority getProtectPriority() {
        return EventPriority.HIGHEST;
    }

    @Override
    @NotNull
    public ChanceSettings getChanceSettings() {
        return this.chanceSettings;
    }

    @NotNull
    @Override
    public PotionSettings getPotionSettings() {
        return potionSettings;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.checkTriggerChance(level)) return false;
        if (!this.addEffect(damager, level)) return false;

        damager.setFreezeTicks(damager.getMaxFreezeTicks());

        if (this.hasVisualEffects()) {
            UniParticle.blockCrack(Material.ICE).play(victim.getEyeLocation(), 0.5, 0.1, 35);
            UniParticle.of(Particle.CLOUD).play(victim.getEyeLocation(), 0.25, 0.1, 25);
            UniSound.of(Sound.BLOCK_GLASS_BREAK).play(victim.getLocation());
        }

        return true;
    }
}
