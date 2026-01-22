package su.nightexpress.excellentenchants.enchantment;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsFiles;
import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.nightcore.configuration.AbstractConfig;
import su.nightexpress.nightcore.configuration.ConfigProperty;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.nightcore.util.LowerCase;

import java.util.Collections;
import java.util.Map;
import java.util.Set;


public class EnchantSettings extends AbstractConfig {

    private final ConfigProperty<Long> arrowEffectsTickInterval = this.addProperty(ConfigTypes.LONG, "Arrow_Effects.Tick_Interval",
        1L,
        "Sets tick interval for arrow & trident particle effect trails added by enchantments.",
        "[Increase for performance; Decrease for better visuals]",
        "[20 ticks = 1 second]",
        "[Default is 1]"
    );

    private final ConfigProperty<Integer> passiveEnchantsTickInterval = this.addProperty(ConfigTypes.INT, "Passive_Enchants.Interval",
        1,
        "Tick interval for passive enchantments (in seconds).",
        "Passive enchants depends on entity's 'ticksLived' value, changing this value may result in passive enchants not triggered correctly.",
        "[Default is 1]"
    );

    private final ConfigProperty<Boolean> passiveEnchantsAllowForMobs = this.addProperty(ConfigTypes.BOOLEAN, "Passive_Enchants.AllowForMobs",
        true,
        "Controls whether mobs are affected by effects of passive enchantments.",
        "[Disable for performance; Enable for better experience]",
        "[Default is true]"
    );

    private final ConfigProperty<Map<String, Set<String>>> disabledEnchantsByWorld = this.addProperty(ConfigTypes.forMapWithLowerKeys(ConfigTypes.STRING_SET_LOWER_CASE),
        "Disabled.ByWorld",
        Map.of(
            "your_world_name", Set.of("enchantment_name", "ice_aspect"),
            "another_world", Set.of("another_enchantment", "ice_aspect")
        ),
        "Put here CUSTOM enchantment names that you want to disable in specific worlds.",
        "To disable all enchantments for a world, use '" + EnchantsPlaceholders.WILDCARD + "' instead of enchantment names.",
        "Enchantment names are equal to their config file names in the '" + EnchantsFiles.DIR_ENCHANTS + "' directory.",
        "[*] This setting only disables enchantment effects, not the enchantment distribution there!"
    );

    private final ConfigProperty<Integer> anvilEnchantLimit = this.addProperty(ConfigTypes.INT, "Anvil.Enchant_Limit",
        5,
        "Prevents item from being enchanted using anvil if it already contains specific amount of custom enchantments.",
        "[Default is 5]"
    );

    public long getArrowEffectsTickInterval() {
        return this.arrowEffectsTickInterval.get();
    }

    public int getPassiveEnchantsTickInterval() {
        return this.passiveEnchantsTickInterval.get();
    }

    public boolean isPassiveEnchantsAllowedForMobs() {
        return this.passiveEnchantsAllowForMobs.get();
    }

    public boolean isEnchantDisabledInWorld(@NotNull World world, @NotNull CustomEnchantment enchantment) {
        return this.disabledEnchantsByWorld.get().getOrDefault(LowerCase.INTERNAL.apply(world.getName()), Collections.emptySet()).contains(enchantment.getId());
    }

    public int getAnvilEnchantsLimit() {
        return this.anvilEnchantLimit.get();
    }
}
