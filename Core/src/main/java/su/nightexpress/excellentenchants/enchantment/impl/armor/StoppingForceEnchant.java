package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_AMOUNT;

public class StoppingForceEnchant extends GameEnchantment implements ChanceMeta, CombatEnchant {

    public static final String ID = "stopping_force";

    private Modifier knockbackModifier;

    public StoppingForceEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.DESERT_COMMON));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            ENCHANTMENT_CHANCE + "% chance to get only " + GENERIC_AMOUNT + "% of knockback in combat.",
            EnchantRarity.RARE,
            3,
            ItemCategories.LEGGINGS
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(100, 0, 0, 100)));

        this.knockbackModifier = Modifier.read(config, "Settings.Knockback_Modifier",
            Modifier.add(0.9, -0.2, 1),
            "Sets the knockback multiplier when taking damage.", "Lower value = less knockback.");

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.getKnockbackModifier(level) * 100));
    }

    public double getKnockbackModifier(int level) {
        return this.knockbackModifier.getValue(level);
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

        double mod = Math.max(0, this.getKnockbackModifier(level));

        this.plugin.runTask(task -> {
            victim.setVelocity(victim.getVelocity().multiply(mod));
        });
        return true;
    }
}
