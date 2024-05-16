package su.nightexpress.excellentenchants.enchantment.listener;

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
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Keys;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.manager.AbstractListener;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.wrapper.UniSound;

import java.util.HashMap;
import java.util.Map;

public class EnchantAnvilListener extends AbstractListener<EnchantsPlugin> {

    public EnchantAnvilListener(@NotNull EnchantsPlugin plugin) {
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

        //if (first.getType().isAir() || first.getAmount() > 1 || !EnchantUtils.isEnchantable(first)) return;

        //if (this.handleRename(event, first, second, result)) return;

        if (this.handleRecharge(event, first, second)) return;

        this.handleCombine(event, first, second, result);

        this.plugin.runTask(task -> {
            ItemStack updated = event.getResult();
            if (updated == null || updated.getType().isAir()) return;

            EnchantUtils.updateDisplay(updated);
            inventory.setItem(2, updated);
        });
    }

    /*private boolean handleRename(@NotNull PrepareAnvilEvent event, @NotNull ItemStack first, @NotNull ItemStack second, @NotNull ItemStack result) {
        if (!(second.getType().isAir() || second.getType() != first.getType() && !EnchantUtils.isEnchantedBook(second))) return false;
        if (result.getType() != first.getType()) return false;

        ItemStack renamed = new ItemStack(result);
        EnchantUtils.getCustomEnchantments(first).forEach((hasEnch, hasLevel) -> EnchantUtils.add(renamed, hasEnch.getEnchantment(), hasLevel, true));
        EnchantUtils.updateDisplay(renamed);

        event.setResult(renamed);
        return true;
    }*/

    private boolean handleRecharge(@NotNull PrepareAnvilEvent event, @NotNull ItemStack first, @NotNull ItemStack second) {
        if (second.getType().isAir()) return false;

        Map<EnchantmentData, Integer> chargable = new HashMap<>();
        EnchantUtils.getCustomEnchantments(first).forEach((data, level) -> {
            if (data.isChargesEnabled() && data.isChargesFuel(second) && !data.isFullOfCharges(first)) {
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
        EnchantUtils.updateDisplay(recharged);
        event.setResult(recharged);

        this.plugin.runTask(task -> event.getInventory().setRepairCost(chargable.size()));
        return true;
    }

    private boolean handleCombine(@NotNull PrepareAnvilEvent event, @NotNull ItemStack first, @NotNull ItemStack second, @NotNull ItemStack result) {
        ItemStack merged = new ItemStack(result.getType().isAir() ? first : result);
        if (EnchantUtils.countCustomEnchantments(merged) > Config.CORE_ITEM_ENCHANT_LIMIT.get()) {
            event.setResult(null);
            return false;
        }

        Map<EnchantmentData, Integer> chargesMap = new HashMap<>();
        EnchantUtils.getCustomEnchantments(result).forEach((data, level) -> {
            if (!data.isChargesEnabled()) return;

            int chargesFirst = data.getCharges(first);
            int chargesSecond = data.getCharges(second);

            chargesMap.put(data, chargesFirst + chargesSecond);
            data.setCharges(merged, level, chargesFirst + chargesSecond);
        });
        if (!chargesMap.isEmpty()) {
            event.setResult(merged);
            return true;
        }
        return false;

        /*
        if (second.getType().isAir() || second.getAmount() > 1 || !EnchantUtils.isEnchantable(second)) return false;
        if (EnchantUtils.isEnchantedBook(first) && second.getType() != first.getType()) return false;
        Map<EnchantmentData, Integer> firstEnchants = EnchantUtils.getCustomEnchantments(first);
        //Map<EnchantmentData, Integer> secondEnchants = EnchantUtils.getCustomEnchantments(second);
        Map<EnchantmentData, Integer> charges = new HashMap<>(firstEnchants.keySet().stream().collect(Collectors.toMap(k -> k, v -> v.getCharges(first))));
        AtomicInteger repairCost = new AtomicInteger(event.getInventory().getRepairCost());

        if (EnchantUtils.isEnchantedBook(second) || second.getType() == first.getType()) {
            EnchantUtils.getCustomEnchantments(second).forEach((data, level) -> {
                int maxMergeLevel = data.getMaxMergeLevel() < 0 ? data.getMaxLevel() : data.getMaxMergeLevel();

                firstEnchants.merge(data, level, (oldLvl, newLvl) -> oldLvl.equals(newLvl) ? Math.min(maxMergeLevel, oldLvl + 1) : Math.max(oldLvl, newLvl));
                charges.merge(data, data.getCharges(second), Integer::sum);
            });
        }

        firstEnchants.forEach((enchantmentData, level) -> {
            if (EnchantUtils.add(merged, enchantmentData.getEnchantment(), level, false)) {
                repairCost.addAndGet(enchantmentData.getAnvilMergeCost(level));
                enchantmentData.setCharges(merged, level, charges.getOrDefault(enchantmentData, 0));
            }
        });

        if (first.equals(merged)) return false;

        EnchantUtils.updateDisplay(merged);
        event.setResult(merged);
        this.plugin.runTask(task -> event.getInventory().setRepairCost(repairCost.get()));
        return true;*/
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClickAnvil(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory instanceof AnvilInventory anvilInventory)) return;
        if (event.getRawSlot() != 2) return;

        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        int count = PDCUtil.getInt(item, Keys.itemRecharged).orElse(0);
        if (count == 0) return;

        Player player = (Player) event.getWhoClicked();
        if (player.getLevel() < anvilInventory.getRepairCost()) return;

        player.setLevel(player.getLevel() - anvilInventory.getRepairCost());
        PDCUtil.remove(item, Keys.itemRecharged);
        event.getView().setCursor(item);
        event.setCancelled(false);

        UniSound.of(Sound.BLOCK_ENCHANTMENT_TABLE_USE).play(player);

        ItemStack second = anvilInventory.getItem(1);
        if (second != null && !second.getType().isAir()) {
            second.setAmount(second.getAmount() - count);
        }

        anvilInventory.setItem(0, null);
        anvilInventory.setItem(2, null);
    }
}

