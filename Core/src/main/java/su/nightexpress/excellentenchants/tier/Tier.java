package su.nightexpress.excellentenchants.tier;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.ITier;
import su.nightexpress.excellentenchants.api.enchantment.ObtainType;

import java.util.Map;

public class Tier implements ITier {

    private final String                  id;
    private final int                     priority;
    private final String                  name;
    private final String               color;
    private final Map<ObtainType, Double> chance;
    private final PlaceholderMap placeholderMap;

    public Tier(@NotNull String id, int priority, @NotNull String name, @NotNull String color,
                @NotNull Map<ObtainType, Double> chance) {
        this.id = id.toLowerCase();
        this.priority = priority;
        this.name = Colorizer.apply(name);
        this.color = Colorizer.apply(color);
        this.chance = chance;
        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.TIER_ID, this::getId)
            .add(Placeholders.TIER_NAME, this::getName)
            .add(Placeholders.TIER_OBTAIN_CHANCE_ENCHANTING, () -> NumberUtil.format(this.getChance(ObtainType.ENCHANTING)))
            .add(Placeholders.TIER_OBTAIN_CHANCE_VILLAGER, () -> NumberUtil.format(this.getChance(ObtainType.VILLAGER)))
            .add(Placeholders.TIER_OBTAIN_CHANCE_LOOT_GENERATION, () -> NumberUtil.format(this.getChance(ObtainType.LOOT_GENERATION)))
            .add(Placeholders.TIER_OBTAIN_CHANCE_FISHING, () -> NumberUtil.format(this.getChance(ObtainType.FISHING)))
            .add(Placeholders.TIER_OBTAIN_CHANCE_MOB_SPAWNING, () -> NumberUtil.format(this.getChance(ObtainType.MOB_SPAWNING)))
        ;
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Name", this.getName());
        cfg.set(path + ".Color", this.getColor());
        cfg.set(path + ".Priority", this.getPriority());
        cfg.remove(path + ".Obtain_Chance");
        this.getChance().forEach((type, chance) -> {
            cfg.set(path + ".Obtain_Chance." + type.name(), chance);
        });
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
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
    public String getColor() {
        return this.color;
    }

    @NotNull
    public Map<ObtainType, Double> getChance() {
        return this.chance;
    }

    public double getChance(@NotNull ObtainType obtainType) {
        return this.getChance().getOrDefault(obtainType, 0D);
    }
}
