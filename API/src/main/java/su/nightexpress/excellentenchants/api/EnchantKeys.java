package su.nightexpress.excellentenchants.api;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.config.ConfigBridge;

public class EnchantKeys {

    public static final NamespacedKey AQUA_AFFINITY         = key("aqua_affinity");
    public static final NamespacedKey BANE_OF_ARTHROPODS    = key("bane_of_arthropods");
    public static final NamespacedKey BINDING_CURSE         = key("binding_curse");
    public static final NamespacedKey BLAST_PROTECTION      = key("blast_protection");
    public static final NamespacedKey BREACH                = key("breach");
    public static final NamespacedKey CHANNELING            = key("channeling");
    public static final NamespacedKey DENSITY               = key("density");
    public static final NamespacedKey DEPTH_STRIDER         = key("depth_strider");
    public static final NamespacedKey EFFICIENCY            = key("efficiency");
    public static final NamespacedKey FEATHER_FALLING       = key("feather_falling");
    public static final NamespacedKey FIRE_ASPECT           = key("fire_aspect");
    public static final NamespacedKey FIRE_PROTECTION       = key("fire_protection");
    public static final NamespacedKey FLAME                 = key("flame");
    public static final NamespacedKey FORTUNE               = key("fortune");
    public static final NamespacedKey FROST_WALKER          = key("frost_walker");
    public static final NamespacedKey IMPALING              = key("impaling");
    public static final NamespacedKey INFINITY              = key("infinity");
    public static final NamespacedKey KNOCKBACK             = key("knockback");
    public static final NamespacedKey LOOTING               = key("looting");
    public static final NamespacedKey LOYALTY               = key("loyalty");
    public static final NamespacedKey LUCK_OF_THE_SEA       = key("luck_of_the_sea");
    public static final NamespacedKey LURE                  = key("lure");
    public static final NamespacedKey MENDING               = key("mending");
    public static final NamespacedKey MULTISHOT             = key("multishot");
    public static final NamespacedKey PIERCING              = key("piercing");
    public static final NamespacedKey POWER                 = key("power");
    public static final NamespacedKey PROJECTILE_PROTECTION = key("projectile_protection");
    public static final NamespacedKey PROTECTION            = key("protection");
    public static final NamespacedKey PUNCH                 = key("punch");
    public static final NamespacedKey QUICK_CHARGE          = key("quick_charge");
    public static final NamespacedKey RESPIRATION           = key("respiration");
    public static final NamespacedKey RIPTIDE               = key("riptide");
    public static final NamespacedKey SHARPNESS             = key("sharpness");
    public static final NamespacedKey SILK_TOUCH            = key("silk_touch");
    public static final NamespacedKey SMITE                 = key("smite");
    public static final NamespacedKey SOUL_SPEED            = key("soul_speed");
    public static final NamespacedKey SWEEPING_EDGE         = key("sweeping_edge");
    public static final NamespacedKey SWIFT_SNEAK           = key("swift_sneak");
    public static final NamespacedKey THORNS                = key("thorns");
    public static final NamespacedKey UNBREAKING            = key("unbreaking");
    public static final NamespacedKey VANISHING_CURSE       = key("vanishing_curse");
    public static final NamespacedKey WIND_BURST            = key("wind_burst");

    @NotNull
    private static NamespacedKey key(@NotNull String string) {
        return NamespacedKey.minecraft(string);
    }

    @NotNull
    public static NamespacedKey custom(@NotNull String string) {
        return new NamespacedKey(ConfigBridge.getNamespace(), string);
    }
}
