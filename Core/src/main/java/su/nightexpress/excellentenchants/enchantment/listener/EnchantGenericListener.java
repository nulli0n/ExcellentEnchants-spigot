package su.nightexpress.excellentenchants.enchantment.listener;

import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.EngineUtils;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.enchantment.EnchantPopulator;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.excellentenchants.hook.HookId;
import su.nightexpress.excellentenchants.hook.impl.MythicMobsHook;

import java.util.HashMap;
import java.util.Map;

public class EnchantGenericListener extends AbstractListener<ExcellentEnchants> {

    public EnchantGenericListener(@NotNull EnchantManager enchantManager) {
        super(enchantManager.plugin());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEnchantProjectileShoot(EntityShootBowEvent event) {
        if (event.getProjectile() instanceof Projectile projectile) {
            EnchantUtils.setSourceWeapon(projectile, event.getBow());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnchantProjectileLand(ProjectileHitEvent event) {
        this.plugin.runTask(task -> {
            EnchantUtils.removeSourceWeapon(event.getEntity());
        });
    }

    // ---------------------------------------------------------------
    // Update enchantment lore after grindstone
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantUpdateGrindstoneClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getType() != InventoryType.GRINDSTONE) return;
        if (event.getRawSlot() == 2) return;

        this.updateGrindstone(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantUpdateGrindstoneDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
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

                curses.putAll(EnchantUtils.getExcellents(source));
            }
            curses.entrySet().removeIf(entry -> !entry.getKey().isCursed());
            curses.forEach((excellentEnchant, level) -> {
                EnchantUtils.add(result, excellentEnchant, level, true);
            });
            EnchantUtils.updateDisplay(result);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantUpdatePickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Item item = event.getItem();
        ItemStack itemStack = item.getItemStack();
        if (EnchantUtils.updateDisplay(itemStack)) {
            item.setItemStack(itemStack);
        }
    }

    // ---------------------------------------------------------------
    // Handle Enchanting Table
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEnchantPopulateEnchantingTable(final EnchantItemEvent event) {
        ItemStack target = event.getItem();
        World world = event.getEnchanter().getWorld();

        EnchantPopulator populator = this.plugin.createPopulator(target, ObtainType.ENCHANTING)
            .withWorld(world)
            .withLevelGenerator(enchant -> enchant.getLevelByEnchantCost(event.getExpLevelCost()))
            .withDefaultPopulation(event.getEnchantsToAdd());

        event.getEnchantsToAdd().putAll(populator.createPopulation());

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            ItemStack result = event.getInventory().getItem(0);
            if (result == null) return;

            // Fix enchantments for Enchant Books.
            // Enchants are not added on book because they do not exists in NMS.
            // Server gets enchants from NMS to apply it on Book NBT tags.
            ItemMeta meta = result.getItemMeta();
            if (meta instanceof EnchantmentStorageMeta storageMeta) {
                event.getEnchantsToAdd().forEach((enchantment, level) -> {
                    if (!storageMeta.hasStoredEnchant(enchantment)) {
                        storageMeta.addStoredEnchant(enchantment, level, true);
                    }
                });
                result.setItemMeta(storageMeta);
            }

            event.getEnchantsToAdd().forEach((enchantment, level) -> {
                if (enchantment instanceof ExcellentEnchant enchant && enchant.isChargesEnabled()) {
                    EnchantUtils.restoreCharges(result, enchant, level);
                }
            });
            EnchantUtils.updateDisplay(result);

            event.getInventory().setItem(0, result);
        });
    }

    // ---------------------------------------------------------------
    // Adding Enchants to Villagers
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantPopulateVillagerAcquire(VillagerAcquireTradeEvent event) {
        MerchantRecipe origin = event.getRecipe();
        ItemStack result = origin.getResult();
        if (!EnchantUtils.isEnchantable(result)) return;

        EnchantPopulator populator = this.plugin.createPopulator(result, ObtainType.VILLAGER)
            .withWorld(event.getEntity().getWorld());
        if (!populator.populate()) return;

        int uses = origin.getUses();
        int maxUses = origin.getMaxUses();
        boolean expReward = origin.hasExperienceReward();
        int villagerExperience = origin.getVillagerExperience();
        float priceMultiplier = origin.getPriceMultiplier();
        int demand = origin.getDemand();
        int specialPrice = origin.getSpecialPrice();

        MerchantRecipe recipe = new MerchantRecipe(result, uses, maxUses, expReward, villagerExperience,
            priceMultiplier, demand, specialPrice);
        recipe.setIngredients(origin.getIngredients());
        event.setRecipe(recipe);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantPopulateLoot(LootGenerateEvent event) {
        if (Config.getObtainSettings(ObtainType.LOOT_GENERATION).isEmpty()) return;

        Entity entity = event.getEntity();
        InventoryHolder holder = event.getInventoryHolder();
        World world = event.getWorld();

        if (entity instanceof Minecart || holder instanceof Chest) {
            event.getLoot().forEach(item -> {
                if (item != null && EnchantUtils.isEnchantable(item)) {
                    this.plugin.createPopulator(item, ObtainType.LOOT_GENERATION)
                        .withWorld(world)
                        .populate();
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantPopulateFishing(PlayerFishEvent event) {
        if (Config.getObtainSettings(ObtainType.FISHING).isEmpty()) return;
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item item)) return;

        ItemStack itemStack = item.getItemStack();
        World world = item.getWorld();
        if (EnchantUtils.isEnchantable(itemStack)) {
            this.plugin.createPopulator(itemStack, ObtainType.FISHING).withWorld(world).populate();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantPopulateSpawn(CreatureSpawnEvent event) {
        //if (Config.getObtainSettings(ObtainType.MOB_SPAWNING).isEmpty()) return;
        LivingEntity entity = event.getEntity();
        if (entity.getType() == EntityType.ARMOR_STAND) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DISPENSE_EGG) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) return;

        this.plugin.runTaskLater(task -> {
            EntityEquipment equipment = entity.getEquipment();
            if (equipment == null) return;

            World world = entity.getWorld();
            boolean isMythic = EngineUtils.hasPlugin(HookId.MYTHIC_MOBS) && MythicMobsHook.isMythicMob(entity);
            boolean doPopulation = Config.getObtainSettings(ObtainType.MOB_SPAWNING).isPresent() && !isMythic;

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack item = equipment.getItem(slot);
                if (EnchantUtils.isEnchantable(item)) {
                    if (doPopulation) {
                        this.plugin.createPopulator(item, ObtainType.MOB_SPAWNING).withWorld(world).populate();
                    }
                    EnchantUtils.getExcellents(item).forEach((enchant, level) -> EnchantUtils.restoreCharges(item, enchant, level));
                    equipment.setItem(slot, item);
                }
            }
        }, 40L);
    }
}
