package su.nightexpress.excellentenchants.manager.listener;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.config.DistributionConfig;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Keys;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.manager.AbstractListener;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.sound.VanillaSound;

import java.util.HashMap;
import java.util.Map;

public class AnvilListener extends AbstractListener<EnchantsPlugin> {

    public AnvilListener(@NotNull EnchantsPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvilRename(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack first = inventory.getItem(0);
        ItemStack second = inventory.getItem(1);
        ItemStack result = event.getResult();

        if (first == null) first = new ItemStack(Material.AIR);
        if (second == null) second = new ItemStack(Material.AIR);
        if (result == null) result = new ItemStack(Material.AIR);

        if (this.handleRecharge(event, first, second)) return;

        this.handleCombine(event, first, second, result);
    }

    private boolean handleRecharge(@NotNull PrepareAnvilEvent event, @NotNull ItemStack first, @NotNull ItemStack second) {
        if (!Config.isChargesEnabled()) return false;
        if (second.getType().isAir()) return false;

        Map<CustomEnchantment, Integer> chargable = new HashMap<>();
        EnchantUtils.getCustomEnchantments(first).forEach((data, level) -> {
            if (data.isChargesFuel(second) && !data.isFullOfCharges(first)) {
                chargable.put(data, level);
            }
        });
        if (chargable.isEmpty()) return false;

        int count;
        ItemStack recharged = new ItemStack(first);
        for (count = 0; count < second.getAmount() && !chargable.keySet().stream().allMatch(data -> data.isFullOfCharges(recharged)); ++count) {
            chargable.forEach((enchant, level) -> enchant.fuelCharges(recharged, level));
        }

        PDCUtil.set(recharged, Keys.itemRecharged, count);
        event.setResult(recharged);

        this.plugin.runTask(event.getView().getPlayer(), () -> event.getView().setRepairCost(chargable.size()));
        return true;
    }

    private boolean handleCombine(@NotNull PrepareAnvilEvent event, @NotNull ItemStack first, @NotNull ItemStack second, @NotNull ItemStack result) {
        ItemStack merged = new ItemStack(result.getType().isAir() ? first : result);

        if (EnchantUtils.countCustomEnchantments(merged) > DistributionConfig.ANVIL_ENCHANT_LIMIT.get()) {
            event.setResult(null);
            return false;
        }

        Map<CustomEnchantment, Integer> chargesMap = new HashMap<>();
        EnchantUtils.getCustomEnchantments(result).forEach((enchantment, level) -> {
            int chargesFirst = enchantment.getCharges(first);
            int chargesSecond = enchantment.getCharges(second);

            chargesMap.put(enchantment, chargesFirst + chargesSecond);
            enchantment.setCharges(merged, level, chargesFirst + chargesSecond);
        });

        if (!chargesMap.isEmpty()) {
            event.setResult(merged);
            return true;
        }

        return false;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClickAnvil(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory instanceof AnvilInventory anvilInventory)) return;
        if (!(event.getView() instanceof AnvilView anvilView)) return;
        if (event.getRawSlot() != 2) return;

        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        int count = PDCUtil.getInt(item, Keys.itemRecharged).orElse(0);
        if (count == 0) return;

        Player player = (Player) event.getWhoClicked();
        if (player.getLevel() < anvilView.getRepairCost()) return;

        player.setLevel(player.getLevel() - anvilView.getRepairCost());
        PDCUtil.remove(item, Keys.itemRecharged);
        event.getView().setCursor(item);
        event.setCancelled(false);

        VanillaSound.of(Sound.BLOCK_ENCHANTMENT_TABLE_USE).play(player);

        ItemStack second = anvilInventory.getItem(1);
        if (second != null && !second.getType().isAir()) {
            second.setAmount(second.getAmount() - count);
        }

        anvilInventory.setItem(0, null);
        anvilInventory.setItem(2, null);
    }
}

