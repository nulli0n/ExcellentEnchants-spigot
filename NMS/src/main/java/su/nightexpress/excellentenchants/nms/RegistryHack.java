package su.nightexpress.excellentenchants.nms;

import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

public interface RegistryHack {

    void unfreezeRegistry();

    void freezeRegistry();

    void addExclusives(@NotNull CustomEnchantment data);

    @Nullable
    Enchantment registerEnchantment(@NotNull CustomEnchantment enchantment);
}
