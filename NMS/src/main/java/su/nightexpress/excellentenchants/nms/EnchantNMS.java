package su.nightexpress.excellentenchants.nms;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;

import java.util.Set;

public interface EnchantNMS {

    void unfreezeRegistry();

    void freezeRegistry();

    void registerEnchantment(@NotNull IEnchantment enchantment);

    void sendAttackPacket(@NotNull Player player, int id);

    void retrieveHook(@NotNull FishHook hook, @NotNull ItemStack item);

    @Nullable
    @Deprecated ItemStack getSpawnEgg(@NotNull LivingEntity entity);

    @NotNull Set<Block> handleFlameWalker(@NotNull LivingEntity entity, @NotNull Location location, int level);

    @NotNull Item popResource(@NotNull Block block, @NotNull ItemStack item);
}
