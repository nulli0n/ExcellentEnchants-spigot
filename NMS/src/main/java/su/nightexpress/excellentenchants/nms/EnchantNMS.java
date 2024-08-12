package su.nightexpress.excellentenchants.nms;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.bridge.FlameWalker;

public interface EnchantNMS {

    void unfreezeRegistry();

    void freezeRegistry();

    void addExclusives(@NotNull CustomEnchantment data);

    @NotNull Enchantment registerEnchantment(@NotNull CustomEnchantment enchantment);

    void sendAttackPacket(@NotNull Player player, int id);

    void retrieveHook(@NotNull FishHook hook, @NotNull ItemStack item, @NotNull EquipmentSlot slot);

    @NotNull Material getItemBlockVariant(@NotNull Material material);

    boolean handleFlameWalker(@NotNull FlameWalker flameWalker, @NotNull LivingEntity entity, int level);

    @NotNull Item popResource(@NotNull Block block, @NotNull ItemStack item);
}
