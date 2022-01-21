package su.nightexpress.excellentenchants.config;

public class ObtainSettings {

    private final int    enchantsTotalMax;
    private final double enchantsCustomGenerationChance;
    private final int    enchantsCustomMin;
    private final int    enchantsCustomMax;

    public ObtainSettings(int enchantsTotalMax, double enchantsCustomGenerationChance, int enchantsCustomMin, int enchantsCustomMax) {
        this.enchantsTotalMax = enchantsTotalMax;
        this.enchantsCustomGenerationChance = enchantsCustomGenerationChance;
        this.enchantsCustomMin = enchantsCustomMin;
        this.enchantsCustomMax = enchantsCustomMax;
    }

    public int getEnchantsTotalMax() {
        return enchantsTotalMax;
    }

    public double getEnchantsCustomGenerationChance() {
        return enchantsCustomGenerationChance;
    }

    public int getEnchantsCustomMin() {
        return enchantsCustomMin;
    }

    public int getEnchantsCustomMax() {
        return enchantsCustomMax;
    }

    @Override
    public String toString() {
        return "ObtainSettings{" + "enchantsTotalMax=" + enchantsTotalMax + ", enchantsCustomGenerationChance=" + enchantsCustomGenerationChance + ", enchantsCustomMin=" + enchantsCustomMin + ", enchantsCustomMax=" + enchantsCustomMax + '}';
    }
}
