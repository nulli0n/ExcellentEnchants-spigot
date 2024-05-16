package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class SurvivalistEnchant extends AbstractEnchantmentData implements FishingEnchant, ChanceData {

    public static final String ID = "survivalist";

    private final Set<CookingRecipe<?>> cookingRecipes;

    private ChanceSettingsImpl chanceSettings;

    public SurvivalistEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription("Automatically cooks fish if what is caught is raw.");
        this.setMaxLevel(1);
        this.setRarity(Rarity.UNCOMMON);

        this.cookingRecipes = new HashSet<>();
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config);

        this.plugin.getServer().recipeIterator().forEachRemaining(recipe -> {
            if (recipe instanceof CookingRecipe<?> cookingRecipe && cookingRecipe.getInput().getType().isItem()) {
                this.cookingRecipes.add(cookingRecipe);
            }
        });
    }

    @Override
    public void clear() {
        this.cookingRecipes.clear();
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
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

        CookingRecipe<?> recipe = this.cookingRecipes.stream().filter(rec -> rec.getInputChoice().test(stack)).findFirst().orElse(null);
        if (recipe == null) return false;

        ItemStack cooked = recipe.getResult();
        cooked.setAmount(stack.getAmount());
        drop.setItemStack(cooked);
        return false;
    }
}
