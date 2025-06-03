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
import org.jetbrains.annotations.Nullable;
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

    private static class Listener implements PacketListener {

        @Override
        public void onPacketSend(PacketSendEvent event) {
            PacketTypeCommon type = event.getPacketType();
            Player player = event.getPlayer();
            if (player == null) return;
            if (!EnchantUtils.canUpdateDisplay(player)) return;

            switch (type) {
                case PacketType.Play.Server.SET_SLOT -> {
                    WrapperPlayServerSetSlot setSlot = new WrapperPlayServerSetSlot(event);

                    ItemStack item = EnchantUtils.addDescription(toBukkit(setSlot.getItem()));
                    setSlot.setItem(fromBukkit(item));
                }
                case PacketType.Play.Server.WINDOW_ITEMS -> {
                    WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(event);

                    windowItems.getItems().replaceAll(packetItem -> {
                        return fromBukkit(EnchantUtils.addDescription(toBukkit(packetItem)));
                    });
                }
                case PacketType.Play.Server.MERCHANT_OFFERS -> {
                    WrapperPlayServerMerchantOffers merchantOffers = new WrapperPlayServerMerchantOffers(event);

                    List<MerchantOffer> offers = merchantOffers.getMerchantOffers();
                    offers.forEach(offer -> {
                        ItemStack result = toBukkit(offer.getOutputItem());
                        offer.setOutputItem(fromBukkit(EnchantUtils.addDescription(result)));
                    });
                }
                default -> {
                    return;
                }
            }

            event.markForReEncode(true);
        }

        @NotNull
        private static ItemStack toBukkit(@NotNull com.github.retrooper.packetevents.protocol.item.ItemStack pooperStack) {
            return SpigotConversionUtil.toBukkitItemStack(pooperStack);
        }

        @Nullable
        private static com.github.retrooper.packetevents.protocol.item.ItemStack fromBukkit(@Nullable ItemStack itemStack) {
            return itemStack == null ? null : SpigotConversionUtil.fromBukkitItemStack(itemStack);
        }
    }
}
