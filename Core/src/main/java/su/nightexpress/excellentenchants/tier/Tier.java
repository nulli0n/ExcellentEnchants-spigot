package su.nightexpress.excellentenchants.tier;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Tier implements IPlaceholder {

    public static final Tier DEFAULT = new Tier(Placeholders.DEFAULT, 100, "Default", ChatColor.WHITE, new HashMap<>());

    static {
        Stream.of(ObtainType.values()).forEach(type -> DEFAULT.getChance().put(type, 100D));
    }

    private final String                  id;
    private final int                     priority;
    private final String                  name;
    private final ChatColor               color;
    private final Map<ObtainType, Double> chance;

    private final Set<ExcellentEnchant> enchants;

    public Tier(@NotNull String id, int priority, @NotNull String name, @NotNull ChatColor color,
                @NotNull Map<ObtainType, Double> chance) {
        this.id = id.toLowerCase();
        this.priority = priority;
        this.name = StringUtil.color(name);
        this.color = color;
        this.chance = chance;
        this.enchants = new HashSet<>();
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> str
            .replace(Placeholders.TIER_ID, this.getId())
            .replace(Placeholders.TIER_NAME, this.getName())
            ;
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    public int getPriority() {
        return priority;
    }

    @NotNull
    public String getName() {
        return this.getColor() + this.name;
    }

    @NotNull
    public ChatColor getColor() {
        return this.color;
    }

    @NotNull
    public Map<ObtainType, Double> getChance() {
        return this.chance;
    }

    public double getChance(@NotNull ObtainType obtainType) {
        return this.getChance().getOrDefault(obtainType, 0D);
    }

    @NotNull
    public Set<ExcellentEnchant> getEnchants() {
        return this.enchants;
    }

    @NotNull
    public Set<ExcellentEnchant> getEnchants(@NotNull ObtainType obtainType) {
        return this.getEnchants(obtainType, null);
    }

    @NotNull
    public Set<ExcellentEnchant> getEnchants(@NotNull ObtainType obtainType, @Nullable ItemStack item) {
        Set<ExcellentEnchant> set = this.getEnchants().stream()
            .filter(enchant -> enchant.getObtainChance(obtainType) > 0)
            .filter(enchant -> item == null || enchant.canEnchantItem(item))
            .collect(Collectors.toCollection(HashSet::new));
        set.removeIf(enchant -> obtainType == ObtainType.ENCHANTING && (enchant.isTreasure() || enchant.isCursed()));
        return set;
    }
}
