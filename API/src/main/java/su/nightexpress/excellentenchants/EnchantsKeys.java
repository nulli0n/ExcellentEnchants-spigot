package su.nightexpress.excellentenchants;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantKeyFactory;

public class EnchantsKeys {

    public static final String NAMESPACE = "excellentenchants";

    public static final NamespacedKey AQUA_AFFINITY         = NamespacedKey.minecraft("aqua_affinity");
    public static final NamespacedKey BANE_OF_ARTHROPODS    = NamespacedKey.minecraft("bane_of_arthropods");
    public static final NamespacedKey BINDING_CURSE         = NamespacedKey.minecraft("binding_curse");
    public static final NamespacedKey BLAST_PROTECTION      = NamespacedKey.minecraft("blast_protection");
    public static final NamespacedKey BREACH                = NamespacedKey.minecraft("breach");
    public static final NamespacedKey CHANNELING            = NamespacedKey.minecraft("channeling");
    public static final NamespacedKey DENSITY               = NamespacedKey.minecraft("density");
    public static final NamespacedKey DEPTH_STRIDER         = NamespacedKey.minecraft("depth_strider");
    public static final NamespacedKey EFFICIENCY            = NamespacedKey.minecraft("efficiency");
    public static final NamespacedKey FEATHER_FALLING       = NamespacedKey.minecraft("feather_falling");
    public static final NamespacedKey FIRE_ASPECT           = NamespacedKey.minecraft("fire_aspect");
    public static final NamespacedKey FIRE_PROTECTION       = NamespacedKey.minecraft("fire_protection");
    public static final NamespacedKey FLAME                 = NamespacedKey.minecraft("flame");
    public static final NamespacedKey FORTUNE               = NamespacedKey.minecraft("fortune");
    public static final NamespacedKey FROST_WALKER          = NamespacedKey.minecraft("frost_walker");
    public static final NamespacedKey IMPALING              = NamespacedKey.minecraft("impaling");
    public static final NamespacedKey INFINITY              = NamespacedKey.minecraft("infinity");
    public static final NamespacedKey KNOCKBACK             = NamespacedKey.minecraft("knockback");
    public static final NamespacedKey LOOTING               = NamespacedKey.minecraft("looting");
    public static final NamespacedKey LOYALTY               = NamespacedKey.minecraft("loyalty");
    public static final NamespacedKey LUCK_OF_THE_SEA       = NamespacedKey.minecraft("luck_of_the_sea");
    public static final NamespacedKey LURE                  = NamespacedKey.minecraft("lure");
    public static final NamespacedKey MENDING               = NamespacedKey.minecraft("mending");
    public static final NamespacedKey MULTISHOT             = NamespacedKey.minecraft("multishot");
    public static final NamespacedKey PIERCING              = NamespacedKey.minecraft("piercing");
    public static final NamespacedKey POWER                 = NamespacedKey.minecraft("power");
    public static final NamespacedKey PROJECTILE_PROTECTION = NamespacedKey.minecraft("projectile_protection");
    public static final NamespacedKey PROTECTION            = NamespacedKey.minecraft("protection");
    public static final NamespacedKey PUNCH                 = NamespacedKey.minecraft("punch");
    public static final NamespacedKey QUICK_CHARGE          = NamespacedKey.minecraft("quick_charge");
    public static final NamespacedKey RESPIRATION           = NamespacedKey.minecraft("respiration");
    public static final NamespacedKey RIPTIDE               = NamespacedKey.minecraft("riptide");
    public static final NamespacedKey SHARPNESS             = NamespacedKey.minecraft("sharpness");
    public static final NamespacedKey SILK_TOUCH            = NamespacedKey.minecraft("silk_touch");
    public static final NamespacedKey SMITE                 = NamespacedKey.minecraft("smite");
    public static final NamespacedKey SOUL_SPEED            = NamespacedKey.minecraft("soul_speed");
    public static final NamespacedKey SWEEPING_EDGE         = NamespacedKey.minecraft("sweeping_edge");
    public static final NamespacedKey SWIFT_SNEAK           = NamespacedKey.minecraft("swift_sneak");
    public static final NamespacedKey THORNS                = NamespacedKey.minecraft("thorns");
    public static final NamespacedKey UNBREAKING            = NamespacedKey.minecraft("unbreaking");
    public static final NamespacedKey VANISHING_CURSE       = NamespacedKey.minecraft("vanishing_curse");
    public static final NamespacedKey WIND_BURST            = NamespacedKey.minecraft("wind_burst");

    private static EnchantKeyFactory keyFactory = value -> new NamespacedKey(NAMESPACE, value);

    public static void setVanillaNamespace() {
        keyFactory = NamespacedKey::minecraft;
    }

    @NotNull
    public static NamespacedKey create(@NotNull String value) {
        return keyFactory.create(value);
    }
}
