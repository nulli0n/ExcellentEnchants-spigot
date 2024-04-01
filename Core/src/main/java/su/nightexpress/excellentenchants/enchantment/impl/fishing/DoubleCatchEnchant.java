package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class DoubleCatchEnchant extends AbstractEnchantmentData implements FishingEnchant, ChanceData {

    public static final String ID = "double_catch";

    private ChanceSettingsImpl chanceSettings;

    public DoubleCatchEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription("Increases amount of caught item by x2 with " + ENCHANTMENT_CHANCE + "% chance.");
        this.setMaxLevel(3);
        this.setRarity(Rarity.RARE);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(4, 2, 1, 100));
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.FISHING_ROD;
    }

    @NotNull
    @Override
    public EventPriority getFishingPriority() {
        return EventPriority.HIGHEST;
    }

    @Override
    @NotNull
    public ChanceSettings getChanceSettings() {
        return this.chanceSettings;
    }

    @Override
    public boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return false;
        if (!(event.getCaught() instanceof Item drop)) return false;
        if (!this.checkTriggerChance(level)) return false;

        ItemStack stack = drop.getItemStack();
        stack.setAmount(Math.min(stack.getMaxStackSize(), stack.getAmount() * 2));
        drop.setItemStack(stack);

        return true;
    }
}
