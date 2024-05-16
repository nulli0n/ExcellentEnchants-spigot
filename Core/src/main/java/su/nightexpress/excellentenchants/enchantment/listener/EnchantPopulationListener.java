package su.nightexpress.excellentenchants.enchantment.listener;

import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.DistributionWay;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.EnchantPopulator;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.excellentenchants.hook.HookPlugin;
import su.nightexpress.excellentenchants.hook.impl.MythicMobsHook;
import su.nightexpress.nightcore.manager.AbstractListener;
import su.nightexpress.nightcore.util.Plugins;

public class EnchantPopulationListener extends AbstractListener<EnchantsPlugin> {

    public EnchantPopulationListener(@NotNull EnchantsPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPopulateEnchantingTable(EnchantItemEvent event) {
        ItemStack target = event.getItem();
        World world = event.getEnchanter().getWorld();

        EnchantPopulator populator = this.plugin.createPopulator(target, DistributionWay.ENCHANTING)
            .withWorld(world)
            .withLevelGenerator((data, distribution) -> distribution.getLevelByEnchantCost(event.getExpLevelCost()))
            .withDefaultPopulation(event.getEnchantsToAdd());

        event.getEnchantsToAdd().putAll(populator.createPopulation());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPopulateVillagerAcquire(VillagerAcquireTradeEvent event) {
        if (Config.getDistributionWaySettings(DistributionWay.VILLAGER).isEmpty()) return;

        MerchantRecipe origin = event.getRecipe();
        ItemStack result = origin.getResult();
        if (!EnchantUtils.isEnchantable(result)) return;

        EnchantPopulator populator = this.plugin.createPopulator(result, DistributionWay.VILLAGER)
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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPopulateLoot(LootGenerateEvent event) {
        if (Config.getDistributionWaySettings(DistributionWay.LOOT_GENERATION).isEmpty()) return;

        Entity entity = event.getEntity();
        InventoryHolder holder = event.getInventoryHolder();
        World world = event.getWorld();

        if (entity instanceof Minecart || holder instanceof Chest) {
            event.getLoot().forEach(item -> {
                if (item != null && EnchantUtils.isEnchantable(item)) {
                    this.plugin.createPopulator(item, DistributionWay.LOOT_GENERATION)
                        .withWorld(world)
                        .withCondition((data, customDistribution) -> customDistribution.isGoodLootTable(event.getLootTable()))
                        .populate();
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPopulateFishing(PlayerFishEvent event) {
        if (Config.getDistributionWaySettings(DistributionWay.FISHING).isEmpty()) return;
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item item)) return;

        ItemStack itemStack = item.getItemStack();
        World world = item.getWorld();
        if (EnchantUtils.isEnchantable(itemStack)) {
            this.plugin.createPopulator(itemStack, DistributionWay.FISHING).withWorld(world).populate();
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPopulateSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getType() == EntityType.ARMOR_STAND) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DISPENSE_EGG) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) return;

        this.plugin.runTaskLater(task -> {
            EntityEquipment equipment = entity.getEquipment();
            if (equipment == null) return;

            World world = entity.getWorld();
            boolean isMythic = Plugins.isLoaded(HookPlugin.MYTHIC_MOBS) && MythicMobsHook.isMythicMob(entity);
            boolean doPopulation = Config.getDistributionWaySettings(DistributionWay.MOB_EQUIPMENT).isPresent() && !isMythic;

            for (EquipmentSlot slot : EnchantUtils.EQUIPMENT_SLOTS) {
                ItemStack item = equipment.getItem(slot);
                if (EnchantUtils.isEnchantable(item)) {
                    if (doPopulation) {
                        this.plugin.createPopulator(item, DistributionWay.MOB_EQUIPMENT).withWorld(world).populate();
                    }
                    EnchantUtils.getCustomEnchantments(item).forEach((enchant, level) -> enchant.restoreCharges(item, level));
                    equipment.setItem(slot, item);
                }
            }
        }, 40L); // меньше недостаточно
    }
}
