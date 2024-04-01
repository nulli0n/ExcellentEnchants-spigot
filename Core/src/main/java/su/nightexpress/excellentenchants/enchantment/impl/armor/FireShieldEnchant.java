package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
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
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;
import su.nightexpress.nightcore.util.wrapper.UniSound;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class FireShieldEnchant extends AbstractEnchantmentData implements ChanceData, CombatEnchant {

    public static final String ID = "fire_shield";

    private Modifier           fireDuration;
    private ChanceSettingsImpl chanceSettings;

    public FireShieldEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to ignite attackers for " + GENERIC_DURATION + "s.");
        this.setMaxLevel(3);
        this.setRarity(Rarity.UNCOMMON);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(3, 2, 1, 100));

        this.fireDuration = Modifier.read(config, "Settings.Fire.Duration",
            Modifier.multiply(2, 1, 1, 600),
            "Sets the fire duration (in seconds).",
            "If entity's current fire ticks amount is less than this value, it will be set to this value.",
            "If entity's current fire ticks amount is greater than this value, it won't be changed.");

        this.addPlaceholder(GENERIC_DURATION, level -> NumberUtil.format(this.getFireDuration(level)));
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.ARMOR;
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
