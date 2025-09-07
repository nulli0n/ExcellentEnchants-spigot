package su.nightexpress.excellentenchants.enchantment.armor;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.DefendEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

public class StoppingForceEnchant extends GameEnchantment implements DefendEnchant {

    private Modifier knockbackReduction;

    public StoppingForceEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.oneHundred());
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.knockbackReduction = Modifier.load(config, "Knockback.Reduction",
            Modifier.addictive(0.3).perLevel(0.2).capacity(1D),
            "Sets the knockback multiplier when taking damage.", "Lower value = less knockback.");

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getKnockbackReduction(level) * 100));
    }

    public double getKnockbackReduction(int level) {
        return this.knockbackReduction.getValue(level);
    }

    @NotNull
    @Override
    public EnchantPriority getProtectPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        double reduction = 1D - Math.max(0, this.getKnockbackReduction(level));

        this.plugin.runTask(victim, () -> victim.setVelocity(victim.getVelocity().multiply(reduction)));
        return true;
    }
}
