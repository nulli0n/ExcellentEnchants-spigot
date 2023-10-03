package su.nightexpress.excellentenchants.hook.impl;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentenchants.ExcellentEnchantsAPI;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;

public class PlaceholderHook {

    private static EnchantsExpansion expansion;

    public static void setup() {
        if (expansion == null) {
            expansion = new EnchantsExpansion();
            expansion.register();
        }
    }

    public static void shutdown() {
        if (expansion != null) {
            expansion.unregister();
            expansion = null;
        }
    }

    static class EnchantsExpansion extends PlaceholderExpansion {

        @Override
        @NotNull
        public String getIdentifier() {
            return "excellentenchants";
        }

        @Override
        @NotNull
        public String getAuthor() {
            return ExcellentEnchantsAPI.PLUGIN.getDescription().getAuthors().get(0);
        }

        @Override
        @NotNull
        public String getVersion() {
            return ExcellentEnchantsAPI.PLUGIN.getDescription().getVersion();
        }

        @Override
        @Nullable
        public String onPlaceholderRequest(Player player, @NotNull String params) {
            if (params.startsWith("charges_remaining_")) {
                String[] chargesSplit = params.substring("charges_remaining_".length()).split(":");
                if (chargesSplit.length < 2) return null;

                EquipmentSlot slot = StringUtil.getEnum(chargesSplit[0], EquipmentSlot.class).orElse(null);
                if (slot == null) return null;

                ItemStack item = player.getInventory().getItem(slot);
                if (item == null || item.getType().isAir()) return "-";

                ExcellentEnchant enchant = EnchantRegistry.getByKey(NamespacedKey.minecraft(chargesSplit[1].toLowerCase()));
                if (enchant == null) return null;

                return String.valueOf(enchant.getCharges(item));
            }
            if (params.startsWith("charges_maximum_")) {
                String[] chargesSplit = params.substring("charges_maximum_".length()).split(":");
                if (chargesSplit.length < 2) return null;

                ExcellentEnchant enchant = EnchantRegistry.getByKey(NamespacedKey.minecraft(chargesSplit[0].toLowerCase()));
                if (enchant == null) return null;

                int level = StringUtil.getInteger(chargesSplit[1], 1);

                return String.valueOf(enchant.getChargesMax(level));
            }
            return super.onPlaceholderRequest(player, params);
        }
    }
}
