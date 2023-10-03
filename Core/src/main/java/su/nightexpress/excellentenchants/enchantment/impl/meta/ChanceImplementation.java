package su.nightexpress.excellentenchants.enchantment.impl.meta;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;

public final class ChanceImplementation implements Chanced {

    //private final ExcellentEnchant enchant;
    private final EnchantScaler triggerChance;

    private ChanceImplementation(@NotNull ExcellentEnchant enchant, @NotNull EnchantScaler triggerChance) {
        //this.enchant = enchant;
        this.triggerChance = triggerChance;
    }

    @NotNull
    public static ChanceImplementation create(@NotNull ExcellentEnchant enchant) {
        return create(enchant, "100");
    }

    @NotNull
    public static ChanceImplementation create(@NotNull ExcellentEnchant enchant, @NotNull String def) {
        return new ChanceImplementation(enchant, EnchantScaler.read(enchant, "Settings.Trigger_Chance", def,
            "A chance that this enchantment will be triggered."));
    }

    @Override
    @NotNull
    public Chanced getChanceImplementation() {
        return this;
    }

    @Override
    public double getTriggerChance(int level) {
        return this.triggerChance.getValue(level);
    }

    @Override
    public boolean checkTriggerChance(int level) {
        return Rnd.chance(this.getTriggerChance(level));
    }
}
