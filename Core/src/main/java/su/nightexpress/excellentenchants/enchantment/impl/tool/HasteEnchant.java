package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Potioned;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.PeriodImplementation;
import su.nightexpress.excellentenchants.enchantment.impl.meta.PotionImplementation;

public class HasteEnchant extends ExcellentEnchant implements Potioned, PassiveEnchant {

    public static final String ID = "haste";

    private PotionImplementation potionImplementation;
    private PeriodImplementation periodImplementation;

    public HasteEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription("Grants permanent " + Placeholders.ENCHANTMENT_POTION_TYPE + " " + Placeholders.ENCHANTMENT_POTION_LEVEL + " effect.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.3);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.potionImplementation = PotionImplementation.create(this, PotionEffectType.FAST_DIGGING, true);
        this.periodImplementation = PeriodImplementation.create(this, "100");
    }

    @NotNull
    @Override
    public PotionImplementation getPotionImplementation() {
        return potionImplementation;
    }

    @NotNull
    @Override
    public PeriodImplementation getPeriodImplementation() {
        return periodImplementation;
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean onTrigger(@NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        return this.addEffect(entity, level);
    }
}
