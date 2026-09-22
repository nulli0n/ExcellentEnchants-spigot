package su.nightexpress.excellentenchants.tooltip.handler;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMerchantOffers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetCursorItem;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPlayerInventory;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;

import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import su.nightexpress.excellentenchants.api.tooltip.TooltipController;
import su.nightexpress.excellentenchants.api.tooltip.TooltipHandler;

public class PacketTooltipHandler implements TooltipHandler {

    private final TooltipController controller;

    private Listener listener;

    public PacketTooltipHandler(TooltipController controller) {
        this.controller = controller;
    }

    @Override
    public void setup() {
        if (this.listener == null) {
            this.listener = new Listener(this.controller);
            this.listener.register();
        }
    }

    @Override
    public void shutdown() {
        if (this.listener != null) {
            this.listener.unregister();
            this.listener = null;
        }
    }

    private static class Listener implements PacketListener {

        private final TooltipController controller;

        private PacketListenerCommon backend;

        public Listener(@NonNull TooltipController controller) {
            this.controller = controller;
        }

        public void register() {
            this.backend = PacketEvents.getAPI().getEventManager().registerListener(this,
                PacketListenerPriority.NORMAL);
        }

        public void unregister() {
            PacketEvents.getAPI().getEventManager().unregisterListener(this.backend);
        }

        @Override
        public void onPacketSend(PacketSendEvent event) {
            PacketTypeCommon type = event.getPacketType();
            Player player = event.getPlayer();
            if (player == null) return;
            if (!this.controller.isReadyForTooltipUpdate(player)) return;

            switch (type) {
                case PacketType.Play.Server.SET_SLOT -> {
                    WrapperPlayServerSetSlot setSlot = new WrapperPlayServerSetSlot(event);

                    this.asBukkit(setSlot.getItem()).map(item -> this.controller.addDescription(item, player)).map(this::fromBukkit)
                        .ifPresent(setSlot::setItem);
                }
                case PacketType.Play.Server.WINDOW_ITEMS -> {
                    WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(event);

                    windowItems.getItems().replaceAll(original -> this.asBukkit(original).map(
                        item -> this.controller.addDescription(item, player)).map(this::fromBukkit).orElse(original));
                }
                case PacketType.Play.Server.SET_PLAYER_INVENTORY -> {
                    WrapperPlayServerSetPlayerInventory setPlayerInventory = new WrapperPlayServerSetPlayerInventory(event);

                    this.asBukkit(setPlayerInventory.getStack()).map(item -> this.controller.addDescription(item, player)).map(
                        this::fromBukkit).ifPresent(setPlayerInventory::setStack);
                }
                case PacketType.Play.Server.SET_CURSOR_ITEM -> {
                    WrapperPlayServerSetCursorItem setCursorItem = new WrapperPlayServerSetCursorItem(event);

                    this.asBukkit(setCursorItem.getStack()).map(item -> this.controller.addDescription(item, player)).map(this::fromBukkit)
                        .ifPresent(setCursorItem::setStack);
                }
                case PacketType.Play.Server.MERCHANT_OFFERS -> {
                    WrapperPlayServerMerchantOffers offers = new WrapperPlayServerMerchantOffers(event);

                    offers.getMerchantOffers().forEach(offer -> {
                        ItemStack result = this.toBukkit(offer.getOutputItem());
                        offer.setOutputItem(this.fromBukkit(this.controller.addDescription(result, player)));
                    });
                }
                default -> {
                    return;
                }
            }

            event.markForReEncode(true);
        }


        private Optional<ItemStack> asBukkit(com.github.retrooper.packetevents.protocol.item.@Nullable ItemStack pooperStack) {
            return Optional.ofNullable(pooperStack).map(SpigotConversionUtil::toBukkitItemStack);
        }


        private ItemStack toBukkit(com.github.retrooper.packetevents.protocol.item.ItemStack pooperStack) {
            return SpigotConversionUtil.toBukkitItemStack(pooperStack);
        }


        private com.github.retrooper.packetevents.protocol.item.ItemStack fromBukkit(ItemStack itemStack) {
            return SpigotConversionUtil.fromBukkitItemStack(itemStack);
        }
    }
}
