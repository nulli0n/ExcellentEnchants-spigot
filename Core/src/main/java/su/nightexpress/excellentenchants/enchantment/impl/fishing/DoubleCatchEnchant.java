package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import org.bukkit.entity.Item;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;

public class DoubleCatchEnchant extends GameEnchantment implements FishingEnchant, ChanceMeta {

    public static final String ID = "double_catch";

    public DoubleCatchEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.PLAINS_SPECIAL));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            "Increases amount of caught item by x2 with " + ENCHANTMENT_CHANCE + "% chance.",
            EnchantRarity.LEGENDARY,
            3,
            ItemCategories.FISHING_ROD
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(4, 2, 1, 100)));
    }

    @NotNull
    @Override
    public EventPriority getFishingPriority() {
        return EventPriority.HIGHEST;
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
