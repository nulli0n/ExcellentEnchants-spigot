package su.nightexpress.excellentenchants.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;

public class ObtainSettings {

    private final boolean isEnabled;
    private final int     enchantsTotalMax;
    private final double enchantsCustomGenerationChance;
    private final int    enchantsCustomMin;
    private final int    enchantsCustomMax;

    public ObtainSettings(boolean isEnabled,
                          int enchantsTotalMax, double enchantsCustomGenerationChance,
                          int enchantsCustomMin, int enchantsCustomMax) {
        this.isEnabled = isEnabled;
        this.enchantsTotalMax = enchantsTotalMax;
        this.enchantsCustomGenerationChance = enchantsCustomGenerationChance;
        this.enchantsCustomMin = enchantsCustomMin;
        this.enchantsCustomMax = enchantsCustomMax;
    }

    @NotNull
    public static ObtainSettings read(@NotNull JYML cfg, @NotNull String path) {
        boolean isEnabled = cfg.getBoolean(path + ".Enabled");
        int enchantsTotalMax = cfg.getInt(path + ".Enchantments.Total_Maximum", 4);
        double enchantsCustomGenerationChance = cfg.getDouble(path + ".Enchantments.Custom_Generation_Chance", 50D);
        int enchantsCustomMin = cfg.getInt(path + ".Enchantments.Custom_Minimum", 0);
        int enchantsCustomMax = cfg.getInt(path + ".Enchantments.Custom_Maximum", 2);

        return new ObtainSettings(isEnabled, enchantsTotalMax, enchantsCustomGenerationChance, enchantsCustomMin, enchantsCustomMax);
    }

    public static void write(@NotNull JYML cfg, @NotNull String path, @NotNull ObtainSettings settings) {
        cfg.set(path + ".Enabled", settings.isEnabled());
        cfg.set(path + ".Enchantments.Total_Maximum", settings.getEnchantsTotalMax());
        cfg.set(path + ".Enchantments.Custom_Generation_Chance", settings.getEnchantsCustomGenerationChance());
        cfg.set(path + ".Enchantments.Custom_Minimum", settings.getEnchantsCustomMin());
        cfg.set(path + ".Enchantments.Custom_Maximum", settings.getEnchantsCustomMax());
    }

    public boolean isEnabled() {
        return isEnabled;
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
