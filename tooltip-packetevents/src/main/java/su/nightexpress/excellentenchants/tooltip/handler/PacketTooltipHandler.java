package su.nightexpress.excellentenchants.tooltip.handler;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentenchants.api.tooltip.TooltipController;
import su.nightexpress.excellentenchants.api.tooltip.TooltipHandler;

import java.util.Optional;

public class PacketTooltipHandler implements TooltipHandler {

    private final TooltipController controller;

    private Listener listener;

    public PacketTooltipHandler(@NotNull TooltipController controller) {
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
            this.backend = PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.NORMAL);
        }

        public void unregister() {
            PacketEvents.getAPI().getEventManager().unregisterListener(this.backend);
        }

        @Override
        public void onPacketSend(@NotNull PacketSendEvent event) {
            PacketTypeCommon type = event.getPacketType();
            Player player = event.getPlayer();
            if (player == null) return;
            if (!this.controller.isReadyForTooltipUpdate(player)) return;

            switch (type) {
                case PacketType.Play.Server.SET_SLOT -> {
                    WrapperPlayServerSetSlot setSlot = new WrapperPlayServerSetSlot(event);

                    this.asBukkit(setSlot.getItem()).map(this.controller::addDescription).map(this::fromBukkit).ifPresent(setSlot::setItem);
                }
                case PacketType.Play.Server.WINDOW_ITEMS -> {
                    WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(event);

                    windowItems.getItems().replaceAll(original -> this.asBukkit(original).map(this.controller::addDescription).map(this::fromBukkit).orElse(original));
                }
                case PacketType.Play.Server.SET_PLAYER_INVENTORY -> {
                    WrapperPlayServerSetPlayerInventory setPlayerInventory = new WrapperPlayServerSetPlayerInventory(event);

                    this.asBukkit(setPlayerInventory.getStack()).map(this.controller::addDescription).map(this::fromBukkit).ifPresent(setPlayerInventory::setStack);
                }
                case PacketType.Play.Server.SET_CURSOR_ITEM -> {
                    WrapperPlayServerSetCursorItem setCursorItem = new WrapperPlayServerSetCursorItem(event);

                    this.asBukkit(setCursorItem.getStack()).map(this.controller::addDescription).map(this::fromBukkit).ifPresent(setCursorItem::setStack);
                }
                case PacketType.Play.Server.MERCHANT_OFFERS -> {
                    WrapperPlayServerMerchantOffers offers = new WrapperPlayServerMerchantOffers(event);

                    offers.getMerchantOffers().forEach(offer -> {
                        ItemStack result = this.toBukkit(offer.getOutputItem());
                        offer.setOutputItem(this.fromBukkit(this.controller.addDescription(result)));
                    });
                }
                default -> {
                    return;
                }
            }

            event.markForReEncode(true);
        }

        @NotNull
        private Optional<ItemStack> asBukkit(@Nullable com.github.retrooper.packetevents.protocol.item.ItemStack pooperStack) {
            return Optional.ofNullable(pooperStack).map(SpigotConversionUtil::toBukkitItemStack);
        }

        @NotNull
        private ItemStack toBukkit(@NotNull com.github.retrooper.packetevents.protocol.item.ItemStack pooperStack) {
            return SpigotConversionUtil.toBukkitItemStack(pooperStack);
        }

        @NotNull
        private com.github.retrooper.packetevents.protocol.item.ItemStack fromBukkit(@NotNull ItemStack itemStack) {
            return SpigotConversionUtil.fromBukkitItemStack(itemStack);
        }
    }
}
