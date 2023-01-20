package su.nightexpress.excellentenchants.nms;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.ItemUtil;

import java.util.Map;
import java.util.Set;

public interface EnchantNMS {

    int EFFECT_DURATION_MAX = 30 * 20;
    int EFFECT_DURATION_MIN = 20 * 20;

    // TODO Move in 'API' module?
    @Deprecated
    static int getEnchantmentLevel(@NotNull ItemStack item, @NotNull Enchantment enchant) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        return meta.getEnchantLevel(enchant);
    }

    @NotNull
    @Deprecated
    static Map<EquipmentSlot, ItemStack> getEquipmentEnchanted(@NotNull LivingEntity entity) {
        Map<EquipmentSlot, ItemStack> equipment = EntityUtil.getEquippedItems(entity);
        equipment.entrySet().removeIf(entry -> {
            ItemStack item = entry.getValue();
            EquipmentSlot slot = entry.getKey();
            if (item == null || item.getType().isAir() || item.getType() == Material.ENCHANTED_BOOK) return true;
            if ((slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND) && ItemUtil.isArmor(item)) return true;
            return !item.hasItemMeta();
        });
        return equipment;
    }

    // TODO Move in 'API' module?
    @Deprecated
    static int getEquippedEnchantLevel(@NotNull LivingEntity entity, @NotNull Enchantment enchant) {
        return getEquipmentEnchanted(entity).values().stream()
            .map(item -> getEnchantmentLevel(item, enchant)).max(Integer::compareTo).orElse(0);
    }

    void addEnchantmentEffect(@NotNull LivingEntity entity, @NotNull Enchantment enchant, @NotNull PotionEffect effect);

    @Nullable Enchantment getEnchantmentByEffect(@NotNull LivingEntity entity, @NotNull PotionEffect type);

    @NotNull Set<Block> handleFlameWalker(@NotNull LivingEntity entity, @NotNull Location location, int level);
}
