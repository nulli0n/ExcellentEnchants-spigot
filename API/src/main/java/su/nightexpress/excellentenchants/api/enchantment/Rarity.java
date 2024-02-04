package su.nightexpress.excellentenchants.api.enchantment;

public enum Rarity {

    COMMON(10),
    UNCOMMON(5),
    RARE(2),
    VERY_RARE(1);

    private final int weight;

    Rarity(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return this.weight;
    }
}
