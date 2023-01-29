package su.nightexpress.excellentenchants.hook.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.ExcellentEnchantsAPI;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;

import java.util.*;
import java.util.stream.Collectors;

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
                packet.getItemModifier().write(0, update(item, isCreative));
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

        manager.addPacketListener(new PacketAdapter(ExcellentEnchantsAPI.PLUGIN, PacketType.Play.Server.OPEN_WINDOW_MERCHANT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                List<MerchantRecipe> list = new ArrayList<>();
                boolean isCreative = event.getPlayer().getGameMode() == GameMode.CREATIVE;
                packet.getMerchantRecipeLists().read(0).forEach(recipe -> {
                    ItemStack result = update(recipe.getResult(), isCreative);
                    if (result == null) return;

                    MerchantRecipe r2 = new MerchantRecipe(result, recipe.getUses(), recipe.getMaxUses(), recipe.hasExperienceReward(), recipe.getVillagerExperience(), recipe.getPriceMultiplier(), recipe.getDemand(), recipe.getSpecialPrice());
                    r2.setIngredients(recipe.getIngredients());
                    list.add(r2);
                });
                packet.getMerchantRecipeLists().write(0, list);
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

        Map<ExcellentEnchant, Integer> enchants = EnchantManager.getExcellentEnchantments(item)
            .entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey().getTier().getPriority()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (old,nev) -> nev, LinkedHashMap::new));
        if (enchants.isEmpty()) return item;

        List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        if (!lore.isEmpty()) {
            enchants.keySet().forEach(enchant -> lore.removeIf(line -> line.contains(enchant.getDisplayName())));
            if (isCreative) {
                enchants.forEach((enchant, level) -> {
                    lore.removeAll(enchant.formatDescription(level));
                });
            }
        }
        if (Config.ENCHANTMENTS_DESCRIPTION_ENABLED.get() && !isCreative) {
            enchants.forEach((enchant, level) -> {
                lore.addAll(0, enchant.formatDescription(level));
            });
        }
        enchants.forEach((enchant, level) -> {
            int charges = EnchantManager.getEnchantmentCharges(item, enchant);
            lore.add(0, enchant.getNameFormatted(level, charges));
        });

        meta.setLore(lore);
        copy.setItemMeta(meta);
        return copy;
    }
}
