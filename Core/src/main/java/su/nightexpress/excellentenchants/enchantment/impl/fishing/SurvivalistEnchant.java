package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import org.bukkit.entity.Item;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
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
import java.util.HashSet;
import java.util.Set;

public class SurvivalistEnchant extends GameEnchantment implements FishingEnchant, ChanceMeta {

    public static final String ID = "survivalist";

    private final Set<CookingRecipe<?>> cookingRecipes;

    public SurvivalistEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.treasure(TradeType.SNOW_SPECIAL));

        this.cookingRecipes = new HashSet<>();
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            "Automatically cooks fish if what is caught is raw.",
            EnchantRarity.RARE,
            1,
            ItemCategories.FISHING_ROD
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config));

        this.cookingRecipes.clear();
        this.plugin.getServer().recipeIterator().forEachRemaining(recipe -> {
            if (recipe instanceof CookingRecipe<?> cookingRecipe && cookingRecipe.getInput().getType().isItem() && !cookingRecipe.getResult().getType().isAir()) {
                this.cookingRecipes.add(cookingRecipe);
            }
        });
    }

//    @Override
//    public void clear() {
//        this.cookingRecipes.clear();
//    }

    @NotNull
    @Override
    public EventPriority getFishingPriority() {
        return EventPriority.HIGH;
    }

    @Override
    public boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!(event.getCaught() instanceof Item drop)) return false;

        ItemStack stack = drop.getItemStack();

        CookingRecipe<?> recipe = this.cookingRecipes.stream().filter(rec -> rec.getInputChoice().test(stack)).findFirst().orElse(null);
        if (recipe == null) return false;

        ItemStack cooked = recipe.getResult();
        cooked.setAmount(stack.getAmount());
        drop.setItemStack(cooked);
        return false;
    }
}
