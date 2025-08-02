package su.nightexpress.excellentenchants.api;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.config.ConfigBridge;

import java.util.HashSet;
import java.util.Set;

public class EnchantKeys {

    private static final Set<NamespacedKey> VANILLA_KEYS = new HashSet<>();

    public static final NamespacedKey AQUA_AFFINITY         = createVanilla("aqua_affinity");
    public static final NamespacedKey BANE_OF_ARTHROPODS    = createVanilla("bane_of_arthropods");
    public static final NamespacedKey BINDING_CURSE         = createVanilla("binding_curse");
    public static final NamespacedKey BLAST_PROTECTION      = createVanilla("blast_protection");
    public static final NamespacedKey BREACH                = createVanilla("breach");
    public static final NamespacedKey CHANNELING            = createVanilla("channeling");
    public static final NamespacedKey DENSITY               = createVanilla("density");
    public static final NamespacedKey DEPTH_STRIDER         = createVanilla("depth_strider");
    public static final NamespacedKey EFFICIENCY            = createVanilla("efficiency");
    public static final NamespacedKey FEATHER_FALLING       = createVanilla("feather_falling");
    public static final NamespacedKey FIRE_ASPECT           = createVanilla("fire_aspect");
    public static final NamespacedKey FIRE_PROTECTION       = createVanilla("fire_protection");
    public static final NamespacedKey FLAME                 = createVanilla("flame");
    public static final NamespacedKey FORTUNE               = createVanilla("fortune");
    public static final NamespacedKey FROST_WALKER          = createVanilla("frost_walker");
    public static final NamespacedKey IMPALING              = createVanilla("impaling");
    public static final NamespacedKey INFINITY              = createVanilla("infinity");
    public static final NamespacedKey KNOCKBACK             = createVanilla("knockback");
    public static final NamespacedKey LOOTING               = createVanilla("looting");
    public static final NamespacedKey LOYALTY               = createVanilla("loyalty");
    public static final NamespacedKey LUCK_OF_THE_SEA       = createVanilla("luck_of_the_sea");
    public static final NamespacedKey LURE                  = createVanilla("lure");
    public static final NamespacedKey MENDING               = createVanilla("mending");
    public static final NamespacedKey MULTISHOT             = createVanilla("multishot");
    public static final NamespacedKey PIERCING              = createVanilla("piercing");
    public static final NamespacedKey POWER                 = createVanilla("power");
    public static final NamespacedKey PROJECTILE_PROTECTION = createVanilla("projectile_protection");
    public static final NamespacedKey PROTECTION            = createVanilla("protection");
    public static final NamespacedKey PUNCH                 = createVanilla("punch");
    public static final NamespacedKey QUICK_CHARGE          = createVanilla("quick_charge");
    public static final NamespacedKey RESPIRATION           = createVanilla("respiration");
    public static final NamespacedKey RIPTIDE               = createVanilla("riptide");
    public static final NamespacedKey SHARPNESS             = createVanilla("sharpness");
    public static final NamespacedKey SILK_TOUCH            = createVanilla("silk_touch");
    public static final NamespacedKey SMITE                 = createVanilla("smite");
    public static final NamespacedKey SOUL_SPEED            = createVanilla("soul_speed");
    public static final NamespacedKey SWEEPING_EDGE         = createVanilla("sweeping_edge");
    public static final NamespacedKey SWIFT_SNEAK           = createVanilla("swift_sneak");
    public static final NamespacedKey THORNS                = createVanilla("thorns");
    public static final NamespacedKey UNBREAKING            = createVanilla("unbreaking");
    public static final NamespacedKey VANISHING_CURSE       = createVanilla("vanishing_curse");
    public static final NamespacedKey WIND_BURST            = createVanilla("wind_burst");

    public static boolean isVanilla(@NotNull NamespacedKey key) {
        return VANILLA_KEYS.contains(key);
    }

    @NotNull
    public static NamespacedKey adaptCustom(@NotNull NamespacedKey from) {
        return isVanilla(from) ? from : custom(from.getKey());
    }

    @NotNull
    private static NamespacedKey createVanilla(@NotNull String string) {
        NamespacedKey key = NamespacedKey.minecraft(string);
        VANILLA_KEYS.add(key);
        return key;
    }

    @NotNull
    public static NamespacedKey custom(@NotNull String string) {
        return new NamespacedKey(ConfigBridge.getNamespace(), string);
    }
}
