package su.nightexpress.excellentenchants.tooltip.handler;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.tooltip.TooltipHandler;
import su.nightexpress.excellentenchants.api.tooltip.TooltipController;

import java.util.ArrayList;
import java.util.List;

public class ProtocolTooltipHandler implements TooltipHandler {

    private static final PacketType[] PACKET_TYPES = {
        PacketType.Play.Server.SET_SLOT,
        PacketType.Play.Server.WINDOW_ITEMS,
        PacketType.Play.Server.SET_PLAYER_INVENTORY,
        PacketType.Play.Server.SET_CURSOR_ITEM,
        PacketType.Play.Server.OPEN_WINDOW_MERCHANT
    };

    private final JavaPlugin        plugin;
    private final TooltipController provider;

    private PacketListener listener;

    public ProtocolTooltipHandler(@NotNull JavaPlugin plugin, @NotNull TooltipController provider) {
        this.plugin = plugin;
        this.provider = provider;
    }

    @Override
    public void setup() {
        if (this.listener != null) return;

        this.listener = new PacketAdapter(this.plugin, PACKET_TYPES) {
            @Override
            public void onPacketSending(PacketEvent event) {
                handlePacketSending(event);
            }
        };

        ProtocolLibrary.getProtocolManager().addPacketListener(this.listener);
    }

    @Override
    public void shutdown() {
        if (this.listener != null) {
            ProtocolLibrary.getProtocolManager().removePacketListener(this.listener);
            this.listener = null;
        }
    }

    private void handlePacketSending(@NotNull PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Player player = event.getPlayer();

        if (!this.provider.isReadyForTooltipUpdate(player)) return;

        PacketType type = packet.getType();
        if (type == PacketType.Play.Server.SET_SLOT || type == PacketType.Play.Server.SET_PLAYER_INVENTORY || type == PacketType.Play.Server.SET_CURSOR_ITEM) {
            ItemStack item = packet.getItemModifier().read(0);
            packet.getItemModifier().write(0, this.provider.addDescription(item));
        }
        else if (type == PacketType.Play.Server.WINDOW_ITEMS) {
            List<ItemStack> items = packet.getItemListModifier().readSafely(0);
            items.replaceAll(this.provider::addDescription);

            packet.getItemListModifier().write(0, items);
        }
        else if (type == PacketType.Play.Server.OPEN_WINDOW_MERCHANT) {
            List<MerchantRecipe> list = new ArrayList<>();

            packet.getMerchantRecipeLists().read(0).forEach(old -> {
                ItemStack result = this.provider.addDescription(old.getResult());

                MerchantRecipe recipe = new MerchantRecipe(result, old.getUses(), old.getMaxUses(), old.hasExperienceReward(), old.getVillagerExperience(), old.getPriceMultiplier(), old.getDemand(), old.getSpecialPrice());
                recipe.setIngredients(old.getIngredients());
                list.add(recipe);
            });

            packet.getMerchantRecipeLists().write(0, list);
        }
    }
}
