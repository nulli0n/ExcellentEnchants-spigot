package su.nightexpress.excellentenchants.nms;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.EntityUtil;

import java.util.Arrays;
import java.util.Set;

public interface EnchantNMS {

    // TODO Move in 'API' module?
    @Deprecated
    static int getEnchantmentLevel(@NotNull ItemStack item, @NotNull Enchantment enchant) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        return meta.getEnchantLevel(enchant);
    }

    // TODO Move in 'API' module?
    @Deprecated
    static int getEquippedEnchantLevel(@NotNull LivingEntity entity, @NotNull Enchantment enchant) {
        return Arrays.stream(EntityUtil.getEquipment(entity)).filter(enchant::canEnchantItem)
            .map(item -> getEnchantmentLevel(item, enchant)).max(Integer::compareTo).orElse(0);
    }

    void addEnchantmentEffect(@NotNull LivingEntity entity, @NotNull Enchantment enchant, @NotNull PotionEffect effect);

    @Nullable Enchantment getEnchantmentByEffect(@NotNull LivingEntity entity, @NotNull PotionEffect type);

    @NotNull Set<Block> handleFlameWalker(@NotNull LivingEntity entity, @NotNull Location location, int level);
}
