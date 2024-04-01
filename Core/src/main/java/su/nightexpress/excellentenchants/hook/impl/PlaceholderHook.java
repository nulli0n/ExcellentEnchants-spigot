package su.nightexpress.excellentenchants.hook.impl;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.StringUtil;

public class PlaceholderHook {

    private static EnchantsExpansion expansion;

    public static void setup(@NotNull EnchantsPlugin plugin) {
        if (expansion == null) {
            expansion = new EnchantsExpansion(plugin);
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

        private final EnchantsPlugin plugin;

        public EnchantsExpansion(@NotNull EnchantsPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        @NotNull
        public String getIdentifier() {
            return this.plugin.getName().toLowerCase();
        }

        @Override
        @NotNull
        public String getAuthor() {
            return this.plugin.getDescription().getAuthors().get(0);
        }

        @Override
        @NotNull
        public String getVersion() {
            return this.plugin.getDescription().getVersion();
        }

        @Override
        public boolean persist() {
            return true;
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

                EnchantmentData enchant = EnchantRegistry.getByKey(NamespacedKey.minecraft(chargesSplit[1].toLowerCase()));
                if (enchant == null) return null;

                return String.valueOf(enchant.getCharges(item));
            }

            if (params.startsWith("charges_maximum_")) {
                String[] chargesSplit = params.substring("charges_maximum_".length()).split(":");
                if (chargesSplit.length < 2) return null;

                EnchantmentData enchant = EnchantRegistry.getByKey(NamespacedKey.minecraft(chargesSplit[0].toLowerCase()));
                if (enchant == null) return null;

                int level = NumberUtil.getInteger(chargesSplit[1], 1);

                return String.valueOf(enchant.getChargesMax(level));
            }

            return super.onPlaceholderRequest(player, params);
        }
    }
}
