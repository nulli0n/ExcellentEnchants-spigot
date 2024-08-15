package su.nightexpress.excellentenchants.hook.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.recipe.data.MerchantOffer;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMerchantOffers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.util.EnchantUtils;

import java.util.List;

public class PacketEventsHook {

    private static boolean registered;

    public static void setup(@NotNull EnchantsPlugin plugin) {
        if (registered) return;

        PacketEvents.getAPI().getEventManager().registerListener(new Listener(), PacketListenerPriority.NORMAL);
        registered = true;
    }

//    public static void shutdown() {
//        if (!registered) return;
//    }

    private static class Listener implements PacketListener {

        @Override
        public void onPacketSend(PacketSendEvent event) {
            PacketTypeCommon type = event.getPacketType();
            Player player = (Player) event.getPlayer();
            if (player == null) return;
            if (!EnchantUtils.canUpdateDisplay(player)) return;

            if (type == PacketType.Play.Server.SET_SLOT) {
                WrapperPlayServerSetSlot setSlot = new WrapperPlayServerSetSlot(event);

                ItemStack item = SpigotConversionUtil.toBukkitItemStack(setSlot.getItem());
                item = EnchantUtils.addDescription(item);

                setSlot.setItem(SpigotConversionUtil.fromBukkitItemStack(item));
                event.markForReEncode(true);
            }
            else if (type == PacketType.Play.Server.WINDOW_ITEMS) {
                WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(event);

                windowItems.getItems().replaceAll(packetItem -> {
                    return SpigotConversionUtil.fromBukkitItemStack(EnchantUtils.addDescription(SpigotConversionUtil.toBukkitItemStack(packetItem)));
                });

                event.markForReEncode(true);
            }
            else if (type == PacketType.Play.Server.MERCHANT_OFFERS) {
                WrapperPlayServerMerchantOffers merchantOffers = new WrapperPlayServerMerchantOffers(event);

                List<MerchantOffer> offers = merchantOffers.getMerchantOffers();
                offers.forEach(offer -> {
                    ItemStack result = SpigotConversionUtil.toBukkitItemStack(offer.getOutputItem());
                    offer.setOutputItem(SpigotConversionUtil.fromBukkitItemStack(EnchantUtils.addDescription(result)));
                });

                event.markForReEncode(true);
            }
        }
    }
}
