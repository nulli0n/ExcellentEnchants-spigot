package su.nightexpress.excellentenchants.hook.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.util.*;
import java.util.stream.Collectors;

public class ProtocolHook {

    private static boolean isRegistered = false;

    public static void setup(@NotNull EnchantsPlugin plugin) {
        if (isRegistered) return;

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        // Not worked :(
        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Login.Server.SUCCESS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                packet.getBooleans().write(0, false);
            }
        });

        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.SET_SLOT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                ItemStack item = packet.getItemModifier().read(0);
                boolean isCreative = event.getPlayer().getGameMode() == GameMode.CREATIVE;
                packet.getItemModifier().write(0, update(item, isCreative));
            }
        });

        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                List<ItemStack> items = packet.getItemListModifier().readSafely(0);
                boolean isCreative = event.getPlayer().getGameMode() == GameMode.CREATIVE;

                items.replaceAll(itemStack -> update(itemStack, isCreative));
                packet.getItemListModifier().write(0, items);
            }
        });

        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.OPEN_WINDOW_MERCHANT) {
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
    public static ItemStack update(@Nullable ItemStack item, boolean isCreative) {
        if (item == null || item.getType().isAir()) return item;

        ItemStack copy = new ItemStack(item);
        ItemMeta meta = copy.getItemMeta();
        if (meta == null || meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) return item;

        Map<EnchantmentData, Integer> enchants = EnchantUtils.getCustomEnchantments(meta)
            .entrySet().stream()
            .sorted(Comparator.comparing((Map.Entry<EnchantmentData, Integer> entry) -> entry.getKey().getRarity().getWeight())
                .thenComparing(entry -> entry.getKey().getName()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (old,nev) -> nev, LinkedHashMap::new));
        if (enchants.isEmpty()) return item;

        List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        if (!lore.isEmpty()) {
            enchants.keySet().forEach(enchant -> lore.removeIf(line -> line.contains(enchant.getName())));
            if (isCreative) {
                enchants.forEach((enchant, level) -> {
                    lore.removeAll(enchant.getDescriptionReplaced(level));
                });
            }
        }
        if (EnchantUtils.canHaveDescription(item) && !isCreative) {
            enchants.forEach((enchant, level) -> {
                lore.addAll(0, enchant.getDescriptionReplaced(level));
            });
        }
        enchants.forEach((enchant, level) -> {
            int charges = enchant.getCharges(meta);
            lore.add(0, NightMessage.asLegacy(enchant.getNameFormatted(level, charges)));
        });

        meta.setLore(lore);
        copy.setItemMeta(meta);
        return copy;
    }
}
