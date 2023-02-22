package su.nightexpress.excellentenchants.enchantment.listener;

import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.hooks.Hooks;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;

import java.util.HashMap;
import java.util.Map;

public class EnchantGenericListener extends AbstractListener<ExcellentEnchants> {

    public EnchantGenericListener(@NotNull EnchantManager enchantManager) {
        super(enchantManager.plugin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnchantPotionEffectQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        player.getActivePotionEffects().stream()
            .filter(effect -> EnchantManager.isEnchantmentEffect(player, effect)).forEach(effect -> {
                player.removePotionEffect(effect.getType());
        });
    }

    // ---------------------------------------------------------------
    // Update enchantment lore after grindstone
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantUpdateGrindstoneClick(InventoryClickEvent e) {
        Inventory inventory = e.getInventory();
        if (inventory.getType() != InventoryType.GRINDSTONE) return;
        if (e.getRawSlot() == 2) return;

        this.updateGrindstone(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantUpdateGrindstoneDrag(InventoryDragEvent e) {
        Inventory inventory = e.getInventory();
        if (inventory.getType() != InventoryType.GRINDSTONE) return;

        this.updateGrindstone(inventory);
    }

    private void updateGrindstone(@NotNull Inventory inventory) {
        this.plugin.getServer().getScheduler().runTask(plugin, () -> {
            ItemStack result = inventory.getItem(2);
            if (result == null || result.getType().isAir()) return;

            Map<ExcellentEnchant, Integer> curses = new HashMap<>();
            for (int slot = 0; slot < 2; slot++) {
                ItemStack source = inventory.getItem(slot);
                if (source == null || source.getType().isAir()) continue;

                curses.putAll(EnchantManager.getExcellentEnchantments(source));
            }
            curses.entrySet().removeIf(entry -> !entry.getKey().isCursed());
            curses.forEach((excellentEnchant, level) -> {
                EnchantManager.addEnchantment(result, excellentEnchant, level, true);
            });
            EnchantManager.updateEnchantmentsDisplay(result);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantUpdatePickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        Item item = e.getItem();
        ItemStack itemStack = item.getItemStack();
        if (EnchantManager.updateEnchantmentsDisplay(itemStack)) {
            item.setItemStack(itemStack);
        }
    }

    // ---------------------------------------------------------------
    // Handle Enchanting Table
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEnchantPopulateEnchantingTable(final EnchantItemEvent e) {
        ItemStack target = e.getItem();
        Map<Enchantment, Integer> enchantsPrepared = e.getEnchantsToAdd();
        Map<Enchantment, Integer> enchantsToPopulate = EnchantManager.getEnchantsToPopulate(target, ObtainType.ENCHANTING, enchantsPrepared, enchant -> enchant.getLevelByEnchantCost(e.getExpLevelCost()));

        enchantsPrepared.putAll(enchantsToPopulate);

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            ItemStack result = e.getInventory().getItem(0);
            if (result == null) return;

            // Fix enchantments for Enchant Books.
            // Enchants are not added on book because they do not exists in NMS.
            // Server gets enchants from NMS to apply it on Book NBT tags.
            ItemMeta meta = result.getItemMeta();
            if (meta instanceof EnchantmentStorageMeta storageMeta) {
                e.getEnchantsToAdd().forEach((enchantment, level) -> {
                    if (!storageMeta.hasStoredEnchant(enchantment)) {
                        storageMeta.addStoredEnchant(enchantment, level, true);
                    }
                });
                result.setItemMeta(storageMeta);
            }

            e.getEnchantsToAdd().forEach((enchantment, level) -> {
                if (enchantment instanceof ExcellentEnchant enchant && enchant.isChargesEnabled()) {
                    EnchantManager.restoreEnchantmentCharges(result, enchant);
                }
            });
            EnchantManager.updateEnchantmentsDisplay(result);

            e.getInventory().setItem(0, result);
        });
    }

    // ---------------------------------------------------------------
    // Adding Enchants to Villagers
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantPopulateVillagerAcquire(VillagerAcquireTradeEvent e) {
        MerchantRecipe recipe = e.getRecipe();
        ItemStack result = recipe.getResult();

        if (!EnchantManager.isEnchantable(result)) return;
        if (!EnchantManager.populateEnchantments(result, ObtainType.VILLAGER)) return;

        int uses = recipe.getUses();
        int maxUses = recipe.getMaxUses();
        boolean expReward = recipe.hasExperienceReward();
        int villagerExperience = recipe.getVillagerExperience();
        float priceMultiplier = recipe.getPriceMultiplier();

        MerchantRecipe recipe2 = new MerchantRecipe(result, uses, maxUses, expReward, villagerExperience, priceMultiplier);
        recipe2.setIngredients(recipe.getIngredients());
        e.setRecipe(recipe2);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantPopulateLoot(LootGenerateEvent e) {
        if (Config.getObtainSettings(ObtainType.LOOT_GENERATION).isEmpty()) return;

        Entity entity = e.getEntity();
        InventoryHolder holder = e.getInventoryHolder();

        if (entity instanceof Minecart || holder instanceof Chest) {
            e.getLoot().forEach(item -> {
                if (item != null && EnchantManager.isEnchantable(item)) {
                    EnchantManager.populateEnchantments(item, ObtainType.LOOT_GENERATION);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantPopulateFishing(PlayerFishEvent e) {
        if (Config.getObtainSettings(ObtainType.FISHING).isEmpty()) return;
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(e.getCaught() instanceof Item item)) return;

        ItemStack itemStack = item.getItemStack();
        if (EnchantManager.isEnchantable(itemStack)) {
            EnchantManager.populateEnchantments(itemStack, ObtainType.FISHING);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantPopulateSpawn(CreatureSpawnEvent e) {
        //if (Config.getObtainSettings(ObtainType.MOB_SPAWNING).isEmpty()) return;
        LivingEntity entity = e.getEntity();

        this.plugin.runTaskLater(task -> {
            EntityEquipment equipment = entity.getEquipment();
            if (equipment == null) return;

            boolean isMythic = Hooks.isMythicMob(entity);
            boolean doPopulation = Config.getObtainSettings(ObtainType.MOB_SPAWNING).isPresent() && !isMythic;

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack item = equipment.getItem(slot);
                if (EnchantManager.isEnchantable(item)) {
                    if (doPopulation) EnchantManager.populateEnchantments(item, ObtainType.MOB_SPAWNING);
                    EnchantManager.getExcellentEnchantments(item).keySet().forEach(enchant -> EnchantManager.restoreEnchantmentCharges(item, enchant));
                    equipment.setItem(slot, item);
                }
            }
        }, 40L);
    }
}
