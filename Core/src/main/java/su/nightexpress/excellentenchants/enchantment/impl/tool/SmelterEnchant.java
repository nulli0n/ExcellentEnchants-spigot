package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.bukkit.NightSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;

public class SmelterEnchant extends GameEnchantment implements ChanceMeta, BlockDropEnchant {

    public static final String ID = "smelter";

    private NightSound sound;
    private boolean    disableOnCrouch;

    private final Set<Material>      exemptedItems;
    private final Set<FurnaceRecipe> recipes;

    public SmelterEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));

        this.exemptedItems = new HashSet<>();
        this.recipes = new HashSet<>();
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            Lists.newList("Smelts mined blocks with " + ENCHANTMENT_CHANCE + "% chance."),
            EnchantRarity.RARE,
            5,
            ItemCategories.TOOL,
            ItemCategories.MINING_TOOLS,
            Lists.newSet(
                SilkSpawnerEnchant.ID,
                BukkitThing.toString(Enchantment.SILK_TOUCH)
            )
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(10, 8, 1, 100)));

        this.disableOnCrouch = ConfigValue.create("Settings.Disable_On_Crouch",
            true,
            "Sets whether or not enchantment will have no effect when crouching."
        ).read(config);

        this.sound = ConfigValue.create("Settings.Sound", NightSound.of(Sound.BLOCK_LAVA_EXTINGUISH), "Sound to play on smelting.").read(config);

        this.recipes.clear();
        this.exemptedItems.clear();

        this.exemptedItems.addAll(ConfigValue.forSet("Settings.Exempted_Blocks",
            BukkitThing::getMaterial,
            (cfg, path, set) -> cfg.set(path, set.stream().map(BukkitThing::toString).toList()),
            Lists.newSet(Material.COBBLESTONE),
            "List of blocks / items that are immune to the Smelter effect."
        ).read(config));

        this.plugin.getServer().recipeIterator().forEachRemaining(recipe -> {
            if (!(recipe instanceof FurnaceRecipe furnaceRecipe)) return;

            if (!this.exemptedItems.contains(furnaceRecipe.getInput().getType())) {
                this.recipes.add(furnaceRecipe);
            }
        });
    }

//    @Override
//    public void clear() {
//        this.recipes.clear();
//        this.exemptedItems.clear();
//    }

    @Override
    public boolean onDrop(@NotNull BlockDropItemEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        if (this.disableOnCrouch && entity instanceof Player player && player.isSneaking()) return false;

        BlockState state = event.getBlockState();
        if (state instanceof Container) return false;
        if (!this.checkTriggerChance(level)) return false;

        List<ItemStack> smelts = new ArrayList<>();
        event.getItems().removeIf(drop -> {
            FurnaceRecipe recipe = this.recipes.stream().filter(rec -> rec.getInputChoice().test(drop.getItemStack())).findFirst().orElse(null);
            if (recipe == null) return false;

            // Copy amount of the origin drop item. Fixes Fortune compatibility and overall original drop amount.
            // Hopefully furnaces will not require multiple input items in the future, otherwise we'll suck here :D
            int amount = drop.getItemStack().getAmount();
            ItemStack result = new ItemStack(recipe.getResult());
            result.setAmount(amount);

            smelts.add(result);
            return true;
        });
        if (smelts.isEmpty()) return false;

        smelts.forEach(itemStack -> this.plugin.populateResource(event, itemStack));

        Block block = state.getBlock();
        if (this.hasVisualEffects()) {
            Location location = LocationUtil.setCenter3D(block.getLocation());
            UniParticle.of(Particle.FLAME).play(location, 0.25, 0.05, 20);
            this.sound.play(location);
        }
        return true;
    }
}
