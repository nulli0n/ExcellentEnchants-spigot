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
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.util.Version;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.util.*;
import java.util.stream.Collectors;

public class ProtocolLibHook {

    private static boolean isRegistered = false;

    public static void setup(@NotNull EnchantsPlugin plugin) {
        if (isRegistered) return;

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.SET_SLOT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                if (EnchantUtils.isIgnoringDisplayUpdate(event.getPlayer())) return;

                ItemStack item = packet.getItemModifier().read(0);
                packet.getItemModifier().write(0, update(item));
            }
        });

        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                if (EnchantUtils.isIgnoringDisplayUpdate(event.getPlayer())) return;

                List<ItemStack> items = packet.getItemListModifier().readSafely(0);
                items.replaceAll(ProtocolLibHook::update);

                packet.getItemListModifier().write(0, items);
            }
        });

        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.OPEN_WINDOW_MERCHANT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                if (EnchantUtils.isIgnoringDisplayUpdate(event.getPlayer())) return;

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

        isRegistered = true;
    }

    @Nullable
    public static ItemStack update(@Nullable ItemStack item) {
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

        if (EnchantUtils.canHaveDescription(item)) {
            enchants.forEach((enchant, level) -> {
                int charges = enchant.getCharges(meta);
                lore.addAll(0, NightMessage.asLegacy(enchant.getDescriptionReplaced(level, charges)));
            });
        }

        if (Version.isBehind(Version.MC_1_21)) {
            enchants.forEach((enchant, level) -> {
                int charges = enchant.getCharges(meta);
                lore.addFirst(NightMessage.asLegacy(enchant.getNameFormatted(level, charges)));
            });
        }

        meta.setLore(lore);
        copy.setItemMeta(meta);
        return copy;
    }
}
