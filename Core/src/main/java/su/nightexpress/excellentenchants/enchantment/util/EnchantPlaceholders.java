package su.nightexpress.excellentenchants.enchantment.util;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.Pair;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class EnchantPlaceholders {

    private final List<Pair<String, Function<Integer, String>>> keys;

    public EnchantPlaceholders() {
        this(new ArrayList<>());
    }

    public EnchantPlaceholders(@NotNull EnchantPlaceholders other) {
        this(other.getKeys());
    }

    public EnchantPlaceholders(@NotNull List<Pair<String, Function<Integer, String>>> keys) {
        this.keys = new ArrayList<>(keys);
    }

    @NotNull
    public static EnchantPlaceholders fusion(@NotNull EnchantPlaceholders... others) {
        EnchantPlaceholders map = new EnchantPlaceholders();
        for (EnchantPlaceholders other : others) {
            map.add(other);
        }
        return map;
    }

    @NotNull
    public List<Pair<String, Function<Integer, String>>> getKeys() {
        return keys;
    }

    @NotNull
    public EnchantPlaceholders add(@NotNull EnchantPlaceholders other) {
        this.getKeys().addAll(other.getKeys());
        return this;
    }

    @NotNull
    public EnchantPlaceholders add(@NotNull String key, @NotNull String replacer) {
        this.add(key, level -> replacer);
        return this;
    }

    @NotNull
    public EnchantPlaceholders add(@NotNull String key, @NotNull Supplier<String> replacer) {
        this.add(key, level -> replacer.get());
        return this;
    }

    @NotNull
    public EnchantPlaceholders add(@NotNull String key, @NotNull Function<Integer, String> replacer) {
        this.getKeys().add(Pair.of(key, replacer));
        return this;
    }

    public void clear() {
        this.getKeys().clear();
    }

    @NotNull
    public PlaceholderMap toMap(int level) {
        List<Pair<String, Supplier<String>>> list = new ArrayList<>();
        this.getKeys().forEach(pair -> {
            list.add(Pair.of(pair.getFirst(), () -> pair.getSecond().apply(level)));
        });
        return new PlaceholderMap(list);
    }
}
