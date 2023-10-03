package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

public class CurseOfDeathEnchant extends ExcellentEnchant implements DeathEnchant, Chanced {

    public static final String ID = "curse_of_death";

    private ChanceImplementation chanceImplementation;

    public CurseOfDeathEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription("When killing players, you have a chance of dying too.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0D);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

        this.chanceImplementation = ChanceImplementation.create(this, Placeholders.ENCHANTMENT_LEVEL + " * 0.1");
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @NotNull
    @Override
    public Chanced getChanceImplementation() {
        return this.chanceImplementation;
    }

    @Override
    public boolean isCursed() {
        return true;
    }

    @Override
    public boolean onDeath(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, ItemStack item, int level) {
        return false;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull Player killer, ItemStack weapon, int level) {
        if (!(entity instanceof Player dead)) return false;
        if (!this.checkTriggerChance(level)) return false;

        killer.setHealth(0D);
        return true;
    }
}
