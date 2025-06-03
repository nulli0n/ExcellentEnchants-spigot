package su.nightexpress.excellentenchants.api.config;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;

public class ConfigBridge {

    public static final String DIR_ENCHANTS = "/enchants/";
    public static final String DIR_MENU     = "/menu/";

    public static final String CONFIG_DISTRIBUTION = "distribution" + FileConfig.EXTENSION;
    public static final String ITEM_TYPES_FILE     = "item_types" + FileConfig.EXTENSION;

    public static final String NAMESPACE = "excellentenchants";

    public static final int LEVEL_CAP  = 255;
    public static final int WEIGHT_CAP = 1024;


    private static boolean isPaper = false;

    public static void load(@NotNull File dataDir, boolean isPaper) {
        ConfigBridge.isPaper = isPaper;

        FileConfig config = new FileConfig(dataDir.getAbsolutePath(), CONFIG_DISTRIBUTION);
        config.initializeOptions(DistributionConfig.class);
        config.saveChanges();
    }

    public static boolean isPaper() {
        return isPaper;
    }

    @NotNull
    public static String getNamespace() {
        return DistributionConfig.CUSTOM_NAMESPACE_ENABLED.get() ? NAMESPACE : NamespacedKey.MINECRAFT;
    }
}
