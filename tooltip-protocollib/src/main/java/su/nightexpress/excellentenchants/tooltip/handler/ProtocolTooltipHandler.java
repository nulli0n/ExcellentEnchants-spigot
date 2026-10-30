package su.nightexpress.excellentenchants.tooltip.handler;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentenchants.api.tooltip.TooltipController;
import su.nightexpress.excellentenchants.api.tooltip.TooltipHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ProtocolTooltipHandler implements TooltipHandler {

    private final JavaPlugin        plugin;
    private final TooltipController controller;

    private Listener listener;

    public ProtocolTooltipHandler(JavaPlugin plugin, TooltipController controller) {
        this.plugin = plugin;
        this.controller = controller;
    }

    @Override
    public void setup() {
        if (this.listener != null) return;

        this.patchProtocolLibIfNecessary();

        this.listener = new Listener(this.plugin, this.controller);
        this.listener.register();
    }

    private void patchProtocolLibIfNecessary() {
        try {
            Field field = MinecraftReflection.class.getDeclaredField("asCraftMirror");
            field.setAccessible(true);
            if (field.get(null) == null) {
                Method method;
                try {
                    method = MinecraftReflection.getCraftItemStackClass()
                        .getMethod("asCraftMirror", MinecraftReflection.getItemStackClass());
                } catch (NoSuchMethodException e) {
                    // In Paper 26.3, asCraftMirror was renamed to asBukkitMirror
                    method = MinecraftReflection.getCraftItemStackClass()
                        .getMethod("asBukkitMirror", MinecraftReflection.getItemStackClass());
                }
                field.set(null, Accessors.getMethodAccessor(method));
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void shutdown() {
        if (this.listener != null) {
            this.listener.unregister();
            this.listener = null;
        }
    }

    private static class Listener extends PacketAdapter {

        private static final PacketType[] PACKET_TYPES = {PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS, PacketType.Play.Server.SET_PLAYER_INVENTORY, PacketType.Play.Server.SET_CURSOR_ITEM, PacketType.Play.Server.OPEN_WINDOW_MERCHANT
        };

        private final TooltipController controller;

        public Listener(@NonNull JavaPlugin plugin, @NonNull TooltipController controller) {
            super(plugin, PACKET_TYPES);
            this.controller = controller;
        }

        public void register() {
            ProtocolLibrary.getProtocolManager().addPacketListener(this);
        }

        public void unregister() {
            ProtocolLibrary.getProtocolManager().removePacketListener(this);
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            this.handlePacketSending(event);
        }

        private void handlePacketSending(PacketEvent event) {
            try {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();

                if (player == null || event.isPlayerTemporary()) return;
                if (!this.controller.isReadyForTooltipUpdate(player)) return;

                PacketType type = packet.getType();
                if (type == PacketType.Play.Server.SET_SLOT || type == PacketType.Play.Server.SET_PLAYER_INVENTORY || type == PacketType.Play.Server.SET_CURSOR_ITEM) {
                    ItemStack item = packet.getItemModifier().read(0);
                    packet.getItemModifier().write(0, this.controller.addDescription(item));
                }
                else if (type == PacketType.Play.Server.WINDOW_ITEMS) {
                    List<ItemStack> items = packet.getItemListModifier().readSafely(0);
                    if (items != null) {
                        items.replaceAll(this.controller::addDescription);
                        packet.getItemListModifier().write(0, items);
                    }
                }
                else if (type == PacketType.Play.Server.OPEN_WINDOW_MERCHANT) {
                    List<MerchantRecipe> list = new ArrayList<>();

                    packet.getMerchantRecipeLists().read(0).forEach(old -> {
                        ItemStack result = this.controller.addDescription(old.getResult());

                        MerchantRecipe recipe = new MerchantRecipe(result, old.getUses(), old.getMaxUses(), old
                            .hasExperienceReward(), old.getVillagerExperience(), old.getPriceMultiplier(), old
                                .getDemand(), old.getSpecialPrice());
                        recipe.setIngredients(old.getIngredients());
                        list.add(recipe);
                    });

                    packet.getMerchantRecipeLists().write(0, list);
                }
            } catch (Throwable ignored) {
            }
        }
    }


}
