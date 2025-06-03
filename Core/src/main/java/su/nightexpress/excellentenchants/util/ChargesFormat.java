package su.nightexpress.excellentenchants.util;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;

public class ChargesFormat implements Writeable {

    private final int threshold;
    private final String format;

    public ChargesFormat(int threshold, @NotNull String format) {
        this.threshold = threshold;
        this.format = format;
    }

    @NotNull
    public String getFormatted(int charges) {
        return this.format.replace(EnchantsPlaceholders.GENERIC_AMOUNT, String.valueOf(charges));
    }

    public boolean isAboveThreshold(int percent) {
        return percent >= this.threshold;
    }

    public boolean isUnderThreshold(int percent) {
        return percent < this.threshold;
    }

    @NotNull
    public static ChargesFormat read(@NotNull FileConfig config, @NotNull String path) {
        int threshold = config.getInt(path + ".Threshold");
        String format = config.getString(path + ".Format", EnchantsPlaceholders.GENERIC_AMOUNT);

        return new ChargesFormat(threshold, format);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Threshold", this.threshold);
        config.set(path + ".Format", this.format);
    }

    public int getThreshold() {
        return this.threshold;
    }

    @NotNull
    public String getFormat() {
        return this.format;
    }
}
