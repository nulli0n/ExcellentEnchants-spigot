package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class DoubleCatchEnchant extends ExcellentEnchant implements FishingEnchant, Chanced {

    public static final String ID = "double_catch";

    private ChanceImplementation chanceImplementation;

    public DoubleCatchEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.LOWEST);
        this.getDefaults().setDescription("Increases amount of caught item by x2 with " + Placeholders.ENCHANTMENT_CHANCE + "% chance.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.5);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "10.0 * " + Placeholders.ENCHANTMENT_LEVEL);
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.FISHING_ROD;
    }

    @Override
    @NotNull
    public ChanceImplementation getChanceImplementation() {
        return this.chanceImplementation;
    }

    @Override
    public boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return false;
        if (!(event.getCaught() instanceof Item drop)) return false;
        if (!this.isAvailableToUse(event.getPlayer())) return false;
        if (!this.checkTriggerChance(level)) return false;

        ItemStack stack = drop.getItemStack();
        stack.setAmount(Math.min(stack.getMaxStackSize(), stack.getAmount() * 2));
        drop.setItemStack(stack);

        return true;
    }
}
