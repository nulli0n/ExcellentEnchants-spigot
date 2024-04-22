package su.nightexpress.excellentenchants.nms;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;

import java.util.Map;
import java.util.Set;

public interface EnchantNMS {

    void unfreezeRegistry();

    void freezeRegistry();

    void registerEnchantment(@NotNull EnchantmentData enchantment);

    void sendAttackPacket(@NotNull Player player, int id);

    void retrieveHook(@NotNull FishHook hook, @NotNull ItemStack item, @NotNull EquipmentSlot slot);

    @NotNull Map<Integer, Map<Enchantment, Integer>> getEnchantLists(@NotNull Inventory inventory, @NotNull ItemStack bukkitItem);

    @NotNull Material getItemBlockVariant(@NotNull Material material);

    @NotNull Set<Block> handleFlameWalker(@NotNull LivingEntity entity, @NotNull Location location, int level);

    @NotNull Item popResource(@NotNull Block block, @NotNull ItemStack item);
}
