package su.nightexpress.excellentenchants.hook.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProtocolLibHook {

    private static boolean registered = false;

    public static void setup(@NotNull EnchantsPlugin plugin) {
        if (registered) return;

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.SET_SLOT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                if (!EnchantUtils.canUpdateDisplay(event.getPlayer())) return;

                ItemStack item = packet.getItemModifier().read(0);
                packet.getItemModifier().write(0, update(item));
            }
        });

        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                if (!EnchantUtils.canUpdateDisplay(event.getPlayer())) return;

                List<ItemStack> items = packet.getItemListModifier().readSafely(0);
                items.replaceAll(ProtocolLibHook::update);

                packet.getItemListModifier().write(0, items);
            }
        });

        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.OPEN_WINDOW_MERCHANT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                if (!EnchantUtils.canUpdateDisplay(event.getPlayer())) return;

                List<MerchantRecipe> list = new ArrayList<>();
                packet.getMerchantRecipeLists().read(0).forEach(recipe -> {
                    ItemStack result = update(recipe.getResult());
                    if (result == null) return;

                    MerchantRecipe r2 = new MerchantRecipe(result, recipe.getUses(), recipe.getMaxUses(), recipe.hasExperienceReward(), recipe.getVillagerExperience(), recipe.getPriceMultiplier(), recipe.getDemand(), recipe.getSpecialPrice());
                    r2.setIngredients(recipe.getIngredients());
                    list.add(r2);
                });

                packet.getMerchantRecipeLists().write(0, list);
            }
        });

        registered = true;
    }

    @Nullable
    public static ItemStack update(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir() || !EnchantUtils.canHaveDescription(item)) return item;

        ItemStack copy = new ItemStack(item);
        ItemMeta meta = copy.getItemMeta();
        if (meta == null || meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) return item;

        Map<CustomEnchantment, Integer> enchants = EnchantUtils.getCustomEnchantments(meta);
        if (enchants.isEmpty()) return item;

        List<String> metaLore = meta.getLore();
        List<String> lore = metaLore == null ? new ArrayList<>() : metaLore;

        enchants.forEach((enchant, level) -> {
            int chargesAmount = enchant.getCharges(meta);
            lore.addAll(NightMessage.asLegacy(enchant.getDescription(level, chargesAmount)));
        });

        meta.setLore(lore);
        copy.setItemMeta(meta);
        return copy;
    }
}
