package su.nightexpress.excellentenchants.api.enchantment;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.random.Rnd;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public enum Rarity {

    COMMON(10),
    UNCOMMON(5),
    RARE(2),
    VERY_RARE(1);

    private int weight;

    Rarity(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return this.weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @NotNull
    public static Rarity getByWeight() {
        Map<Rarity, Double> map = new HashMap<>();

        for (Rarity rarity : Rarity.values()) {
            map.put(rarity, (double) rarity.getWeight());
        }

        return Rnd.getByWeight(map);
    }
}
