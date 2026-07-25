package su.nightexpress.excellentenchants.tooltip.handler;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentenchants.api.tooltip.TooltipController;
import su.nightexpress.excellentenchants.api.tooltip.TooltipHandler;

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

        this.listener = new Listener(this.plugin, this.controller);
        this.listener.register();
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
            PacketContainer packet = event.getPacket();
            Player player = event.getPlayer();

            if (player == null || event.isPlayerTemporary()) return;
            if (!this.controller.isReadyForTooltipUpdate(player)) return;

            PacketType type = packet.getType();
            if (type == PacketType.Play.Server.SET_SLOT || type == PacketType.Play.Server.SET_PLAYER_INVENTORY || type == PacketType.Play.Server.SET_CURSOR_ITEM) {
                ItemStack item = packet.getItemModifier().read(0);
                packet.getItemModifier().write(0, this.applyPlaceholders(player, this.controller.addDescription(item)));
            }
            else if (type == PacketType.Play.Server.WINDOW_ITEMS) {
                List<ItemStack> items = packet.getItemListModifier().readSafely(0);
                items.replaceAll(item -> this.applyPlaceholders(player, this.controller.addDescription(item)));

                packet.getItemListModifier().write(0, items);
            }
            else if (type == PacketType.Play.Server.OPEN_WINDOW_MERCHANT) {
                List<MerchantRecipe> list = new ArrayList<>();

                packet.getMerchantRecipeLists().read(0).forEach(old -> {
                    ItemStack result = this.applyPlaceholders(player, this.controller.addDescription(old.getResult()));

                    MerchantRecipe recipe = new MerchantRecipe(result, old.getUses(), old.getMaxUses(), old
                        .hasExperienceReward(), old.getVillagerExperience(), old.getPriceMultiplier(), old
                            .getDemand(), old.getSpecialPrice());
                    recipe.setIngredients(old.getIngredients());
                    list.add(recipe);
                });

                packet.getMerchantRecipeLists().write(0, list);
            }
        }

        private ItemStack applyPlaceholders(@NonNull Player player, @NonNull ItemStack itemStack) {
            if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return itemStack;

            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) return itemStack;

            List<String> lore = meta.getLore();
            if (lore != null && !lore.isEmpty()) {
                lore.replaceAll(line -> PlaceholderAPI.setPlaceholders(player, line));
                meta.setLore(lore);
            }

            itemStack.setItemMeta(meta);
            return itemStack;
        }
    }
}
