package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

import java.util.HashSet;
import java.util.Set;

public class SurvivalistEnchant extends ExcellentEnchant implements FishingEnchant, Chanced {

    public static final String ID = "survivalist";

    private final Set<CookingRecipe<?>> cookingRecipes;

    private ChanceImplementation chanceImplementation;

    public SurvivalistEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription("Automatically cooks fish if what is caught is raw.");
        this.getDefaults().setLevelMax(1);
        this.getDefaults().setTier(0.4);

        this.cookingRecipes = new HashSet<>();
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this, "100");

        this.cookingRecipes.clear();
        this.plugin.getServer().recipeIterator().forEachRemaining(recipe -> {
            if (recipe instanceof CookingRecipe<?> cookingRecipe) {
                this.cookingRecipes.add(cookingRecipe);
            }
        });
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.FISHING_ROD;
    }

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

        CookingRecipe<?> recipe = this.cookingRecipes.stream().filter(r -> r.getInput().isSimilar(stack)).findFirst().orElse(null);
        if (recipe == null) return false;

        ItemStack cooked = recipe.getResult();
        cooked.setAmount(stack.getAmount());
        drop.setItemStack(cooked);
        return false;
    }
}
