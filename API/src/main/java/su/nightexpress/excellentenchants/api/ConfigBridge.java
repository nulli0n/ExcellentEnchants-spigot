package su.nightexpress.excellentenchants.api;

public class ConfigBridge {

    private static int enchantsTickInterval;

    private static boolean globalDistEnchanting;
    private static boolean globalDistTrading;
    private static boolean globalDistMobEquipment;
    private static boolean globalDistTradeEquipment;
    private static boolean globalDistRandomLoot;

    public static int getEnchantsTickInterval() {
        return enchantsTickInterval;
    }

    public static void setEnchantsTickInterval(int enchantsTickInterval) {
        ConfigBridge.enchantsTickInterval = enchantsTickInterval;
    }

    public static boolean isGlobalDistEnchanting() {
        return globalDistEnchanting;
    }

    public static void setGlobalDistEnchanting(boolean globalDistEnchanting) {
        ConfigBridge.globalDistEnchanting = globalDistEnchanting;
    }

    public static boolean isGlobalDistTrading() {
        return globalDistTrading;
    }

    public static void setGlobalDistTrading(boolean globalDistTrading) {
        ConfigBridge.globalDistTrading = globalDistTrading;
    }

    public static boolean isGlobalDistMobEquipment() {
        return globalDistMobEquipment;
    }

    public static void setGlobalDistMobEquipment(boolean globalDistMobEquipment) {
        ConfigBridge.globalDistMobEquipment = globalDistMobEquipment;
    }

    public static boolean isGlobalDistTradeEquipment() {
        return globalDistTradeEquipment;
    }

    public static void setGlobalDistTradeEquipment(boolean globalDistTradeEquipment) {
        ConfigBridge.globalDistTradeEquipment = globalDistTradeEquipment;
    }

    public static boolean isGlobalDistRandomLoot() {
        return globalDistRandomLoot;
    }

    public static void setGlobalDistRandomLoot(boolean globalDistRandomLoot) {
        ConfigBridge.globalDistRandomLoot = globalDistRandomLoot;
    }
}
