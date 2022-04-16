package su.nightexpress.excellentenchants.manager.enchants.bow;

import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantBowPotionTemplate;
import su.nightexpress.excellentenchants.manager.EnchantRegister;

public class EnchantPoisonedArrows extends IEnchantBowPotionTemplate {

    public static final String ID = "poisoned_arrows";

    public EnchantPoisonedArrows(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM, PotionEffectType.POISON);
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(EnchantRegister.CONFUSING_ARROWS);
        this.addConflict(EnchantRegister.ELECTRIFIED_ARROWS);
        this.addConflict(EnchantRegister.EXPLOSIVE_ARROWS);
        this.addConflict(EnchantRegister.WITHERED_ARROWS);
        this.addConflict(EnchantRegister.DRAGONFIRE_ARROWS);
    }
}
