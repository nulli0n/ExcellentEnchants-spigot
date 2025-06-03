package su.nightexpress.excellentenchants.enchantment.fishing;

import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class SurvivalistEnchant extends GameEnchantment implements FishingEnchant {

    private final Set<CookingRecipe<?>> cookingRecipes;

    public SurvivalistEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.oneHundred());

        this.cookingRecipes = new HashSet<>();
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
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
    public EnchantPriority getFishingPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return false;
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
