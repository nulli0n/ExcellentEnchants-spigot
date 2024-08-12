package su.nightexpress.excellentenchants.api;

public class ConfigBridge {

    private static int enchantsTickInterval;

    public static int getEnchantsTickInterval() {
        return enchantsTickInterval;
    }

    public static void setEnchantsTickInterval(int enchantsTickInterval) {
        ConfigBridge.enchantsTickInterval = enchantsTickInterval;
    }
}
