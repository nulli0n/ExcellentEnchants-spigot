package su.nightexpress.excellentenchants.api.enchantment.meta;

public class EnchantMeta {

    private Probability   probability;
    private PotionEffects potionEffects;
    private ArrowEffects  arrowEffects;
    private Period        period;

    public EnchantMeta() {

    }

    public Probability getProbability() {
        return probability;
    }

    public void setProbability(Probability probability) {
        this.probability = probability;
    }

    public PotionEffects getPotionEffects() {
        return potionEffects;
    }

    public void setPotionEffects(PotionEffects potionEffects) {
        this.potionEffects = potionEffects;
    }

    public ArrowEffects getArrowEffects() {
        return arrowEffects;
    }

    public void setArrowEffects(ArrowEffects arrowEffects) {
        this.arrowEffects = arrowEffects;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }
}
