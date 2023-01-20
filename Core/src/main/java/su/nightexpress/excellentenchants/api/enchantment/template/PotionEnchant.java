package su.nightexpress.excellentenchants.api.enchantment.template;

import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Potioned;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.PotionImplementation;

public abstract class PotionEnchant extends ExcellentEnchant implements Potioned {

    private final PotionEffectType effectType;
    private final boolean          isPermanent;

    private PotionImplementation potionImplementation;

    public PotionEnchant(@NotNull ExcellentEnchants plugin, @NotNull String id, @NotNull EnchantPriority priority,
                         @NotNull PotionEffectType effectType, boolean isPermanent) {
        super(plugin, id, priority);
        this.effectType = effectType;
        this.isPermanent = isPermanent;
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.potionImplementation = PotionImplementation.create(this, this.effectType, this.isPermanent);
    }

    @Override
    @NotNull
    public Potioned getPotionImplementation() {
        return this.potionImplementation;
    }
}
