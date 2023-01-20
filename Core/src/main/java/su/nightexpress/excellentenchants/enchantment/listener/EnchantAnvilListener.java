package su.nightexpress.excellentenchants.enchantment.listener;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.MessageUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.ExcellentEnchantsAPI;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EnchantAnvilListener extends AbstractListener<ExcellentEnchants> {

    private static final NamespacedKey RECHARGED = new NamespacedKey(ExcellentEnchantsAPI.PLUGIN, "item.recharged");

    public EnchantAnvilListener(@NotNull ExcellentEnchants plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvilRename(PrepareAnvilEvent e) {
        AnvilInventory inventory = e.getInventory();

        ItemStack first = inventory.getItem(0);
        ItemStack second = inventory.getItem(1);
        ItemStack result = e.getResult();

        if (first == null) first = new ItemStack(Material.AIR);
        if (second == null) second = new ItemStack(Material.AIR);
        if (result == null) result = new ItemStack(Material.AIR);

        // Check if source item is an enchantable single item.
        if (first.getType().isAir() || first.getAmount() > 1 || !EnchantManager.isEnchantable(first)) return;

        if (this.handleRename(e, first, second, result)) return;
        if (this.handleRecharge(e, first, second, result)) return;

        this.handleEnchantMerging(e, first, second, result);
    }

    private boolean handleRename(@NotNull PrepareAnvilEvent e,
                                 @NotNull ItemStack first, @NotNull ItemStack second, @NotNull ItemStack result) {

        if (!second.getType().isAir()) return false;
        if (result.getType() != first.getType()) return false;

        ItemStack result2 = new ItemStack(result);
        EnchantManager.getExcellentEnchantments(first).forEach((hasEnch, hasLevel) -> {
            EnchantManager.addEnchantment(result2, hasEnch, hasLevel, true);
        });
        e.setResult(result2);
        return true;
    }

    private boolean handleRecharge(@NotNull PrepareAnvilEvent e,
                                   @NotNull ItemStack first, @NotNull ItemStack second, @NotNull ItemStack result) {
        if (second.getType().isAir()) return false;

        Set<ExcellentEnchant> chargeable = EnchantManager.getExcellentEnchantments(first).keySet().stream()
            .filter(en -> en.isChargesEnabled() && en.isChargesFuel(second) && !en.isFullOfCharges(first))
            .collect(Collectors.toSet());
        if (chargeable.isEmpty()) return false;

        ItemStack result2 = new ItemStack(first);
        chargeable.forEach(enchant -> EnchantManager.rechargeEnchantmentCharges(result2, enchant));
        PDCUtil.setData(result2, RECHARGED, true);
        e.setResult(result2);
        this.plugin.runTask(c -> e.getInventory().setRepairCost(chargeable.size()), false);
        return true;
    }

    private boolean handleEnchantMerging(@NotNull PrepareAnvilEvent e,
                                         @NotNull ItemStack first, @NotNull ItemStack second, @NotNull ItemStack result) {
        // Validate items in the first two slots.
        if (second.getType().isAir() || second.getAmount() > 1 || !EnchantManager.isEnchantable(second)) return false;
        if (first.getType() == Material.ENCHANTED_BOOK && second.getType() != first.getType()) return false;

        ItemStack result2 = new ItemStack(result.getType().isAir() ? first : result);
        Map<ExcellentEnchant, Integer> enchantments = EnchantManager.getExcellentEnchantments(first);
        AtomicInteger repairCost = new AtomicInteger(e.getInventory().getRepairCost());

        // Merge only if it's Item + Item, Item + Enchanted book or Enchanted Book + Enchanted Book
        if (second.getType() == Material.ENCHANTED_BOOK || second.getType() == first.getType()) {
            EnchantManager.getExcellentEnchantments(second).forEach((enchant, level) -> {
                enchantments.merge(enchant, level, (oldLvl, newLvl) -> (oldLvl.equals(newLvl)) ? (oldLvl + 1) : (Math.max(oldLvl, newLvl)));
            });
        }

        // Recalculate operation cost depends on enchantments merge cost.
        enchantments.forEach((enchant, level) -> {
            if (EnchantManager.addEnchantment(result2, enchant, level, false)) {
                repairCost.addAndGet(enchant.getAnvilMergeCost(level));
            }
        });

        if (first.equals(result2)) return false;

        e.setResult(result2);

        // NMS ContainerAnvil will set level cost to 0 right after calling the event, need 1 tick delay.
        this.plugin.runTask((c) -> e.getInventory().setRepairCost(repairCost.get()), false);
        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClickAnvil(InventoryClickEvent e) {
        if (!(e.getInventory() instanceof AnvilInventory inventory)) return;
        if (e.getRawSlot() != 2) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || !PDCUtil.getBooleanData(item, RECHARGED)) return;

        Player player = (Player) e.getWhoClicked();
        if (player.getLevel() < inventory.getRepairCost()) return;

        player.setLevel(player.getLevel() - inventory.getRepairCost());
        PDCUtil.removeData(item, RECHARGED);
        e.getView().setCursor(item);
        e.setCancelled(false);

        MessageUtil.sound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE);

        ItemStack second = inventory.getItem(1);
        if (second != null && !second.getType().isAir()) {
            second.setAmount(second.getAmount() - 1);
        }
        inventory.setItem(0, null);
        //inventory.setItem(1, null);
        inventory.setItem(2, null);
    }
}
