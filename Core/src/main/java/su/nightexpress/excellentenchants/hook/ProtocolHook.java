package su.nightexpress.excellentenchants.hook;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.ExcellentEnchantsAPI;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.manager.EnchantManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProtocolHook {

    private static boolean isRegistered = false;

    public static void setup() {
        if (isRegistered) return;

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(ExcellentEnchantsAPI.PLUGIN, PacketType.Play.Server.SET_SLOT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                ItemStack item = packet.getItemModifier().read(0);
                boolean isCreative = event.getPlayer().getGameMode() == GameMode.CREATIVE;
                //else {
                    packet.getItemModifier().write(0, update(item, isCreative));
                //}
            }
        });

        manager.addPacketListener(new PacketAdapter(ExcellentEnchantsAPI.PLUGIN, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                List<ItemStack> items = packet.getItemListModifier().readSafely(0);
                boolean isCreative = event.getPlayer().getGameMode() == GameMode.CREATIVE;

                for (int index = 0; index < items.size(); index++) {
                    ItemStack item = items.get(index);
                    items.set(index, update(item, isCreative));
                }
                packet.getItemListModifier().write(0, items);
            }
        });

        isRegistered = true;
    }

    @Nullable
    private static ItemStack update(@Nullable ItemStack item, boolean isCreative) {
        if (item == null || item.getType().isAir()) return item;

        ItemStack copy = new ItemStack(item);
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return item;

        List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        Map<ExcellentEnchant, Integer> enchants = EnchantManager.getExcellentEnchantments(item);

        enchants.keySet().forEach(enchant -> lore.removeIf(line -> line.contains(enchant.getDisplayName())));
        if (isCreative) {
            enchants.forEach((enchant, integer) -> {
                lore.removeAll(Config.formatDescription(enchant.getDescription(integer)));
            });
        }
        if (Config.ENCHANTMENTS_DESCRIPTION_ENABLED && !isCreative) {
            enchants.forEach((enchant, integer) -> {
                lore.addAll(0, Config.formatDescription(enchant.getDescription(integer)));
            });
        }
        enchants.forEach((enchant, integer) -> {
            lore.add(0, enchant.getNameFormatted(integer));
        });

        meta.setLore(lore);
        copy.setItemMeta(meta);
        return copy;
    }
}
