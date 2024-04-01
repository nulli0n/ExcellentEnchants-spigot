package su.nightexpress.excellentenchants.enchantment.impl.armor;

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

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class ColdSteelEnchant extends AbstractEnchantmentData implements ChanceData, PotionData, CombatEnchant {

    public static final String ID = "cold_steel";

    private ChanceSettingsImpl chanceSettings;
    private PotionSettingsImpl potionSettings;

    public ColdSteelEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to apply " + ENCHANTMENT_POTION_TYPE + " " + ENCHANTMENT_POTION_LEVEL + " (" + ENCHANTMENT_POTION_DURATION + "s.) on attacker.");
        this.setRarity(Rarity.COMMON);
        this.setMaxLevel(3);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(8, 2, 1));

        this.potionSettings = PotionSettingsImpl.create(this, config, PotionEffectType.SLOW_DIGGING, false,
            Modifier.add(4, 1, 1, 300),
            Modifier.add(0, 1,1, 5)
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

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.ARMOR_TORSO;
    }

    @NotNull
    @Override
    public EventPriority getProtectPriority() {
        return EventPriority.HIGHEST;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.checkTriggerChance(level)) return false;

        return this.addEffect(damager, level);
    }
}
