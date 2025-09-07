package su.nightexpress.excellentenchants.manager.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantRegistry;
import su.nightexpress.excellentenchants.api.damage.DamageBonus;
import su.nightexpress.excellentenchants.api.damage.DamageBonusType;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.ProtectionEnchant;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.manager.AbstractListener;

import java.util.HashMap;
import java.util.Map;

public class EnchantListener extends AbstractListener<EnchantsPlugin> {

    private final EnchantManager manager;

    public EnchantListener(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        this.manager.handleItemEnchants(player, EquipmentSlot.HAND, EnchantRegistry.MINING, (item, enchant, level) -> enchant.onBreak(event, player, item, level));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();

        this.manager.handleItemEnchants(player, EquipmentSlot.HAND, EnchantRegistry.BLOCK_DROP, (item, enchant, level) -> enchant.onDrop(event, player, item, level));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShootBow(EntityShootBowEvent event) {
        LivingEntity entity = event.getEntity();
        EquipmentSlot slot = event.getHand();

        this.manager.handleItemEnchants(entity, slot, EnchantRegistry.BOW, (item, enchant, level) -> {
            if (!enchant.onShoot(event, entity, item, level)) return false;

            if (event.getProjectile() instanceof AbstractArrow arrow) {
                this.addArrowEffects(arrow, enchant, level);
            }

            return true;
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTridentLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile instanceof Trident trident)) return;
        if (!(projectile.getShooter() instanceof LivingEntity entity)) return;

        ItemStack weapon = trident.getWeapon();
        if (weapon == null) return;

        this.manager.handleItemEnchants(entity, weapon, EnchantRegistry.TRIDENT, (item, enchant, level) -> {
            if (!enchant.onLaunch(event, entity, item, level)) return false;

            this.addArrowEffects(trident, enchant, level);
            return true;
        });
    }

    private void addArrowEffects(@NotNull AbstractArrow arrow, @NotNull CustomEnchantment enchant, int level) {
        EnchantUtils.addArrowEnchant(arrow, enchant, level);

        if (enchant.hasVisualEffects() && enchant.hasComponent(EnchantComponent.ARROW)) {
            this.manager.addArrowEffect(arrow, enchant.getComponent(EnchantComponent.ARROW).getTrailEffect());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile instanceof AbstractArrow abstractArrow)) return;
        if (!(projectile.getShooter() instanceof LivingEntity shooter)) return;

        if (abstractArrow instanceof Arrow arrow) {
            this.manager.handleArrowEnchants(arrow, EnchantRegistry.ARROW, (item, enchant, level) -> {
                enchant.onHit(event, shooter, arrow, level);
                return false;
            });
        }
        else if (abstractArrow instanceof Trident trident) {
            this.manager.handleArrowEnchants(trident, EnchantRegistry.TRIDENT, (item, enchant, level) -> {
                enchant.onHit(event, shooter, trident, level);
                return false;
            });
        }

        this.plugin.runTask(abstractArrow, () -> this.manager.removeArrowEffects(abstractArrow));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageGeneric(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        DamageSource source = event.getDamageSource();
        if (source.getCausingEntity() != null) return;

        Map<ProtectionEnchant, DamageBonus> damageMap = new HashMap<>();

        this.manager.handleArmorEnchants(victim, EnchantRegistry.PROTECTION, (item, enchant, level) -> {
            if (event.isCancelled()) return false;

            DamageBonus damageBonus = damageMap.computeIfAbsent(enchant, k -> enchant.getDamageBonus());

            return enchant.onProtection(event, damageBonus, victim, item, level);
        });

        if (event.isCancelled()) return;

        double scalarBonus = 0D;
        double normalBonus = 0D;
        for (DamageBonus damageBonus : damageMap.values()) {
            double bonus = damageBonus.getBonusAmount();

            if (damageBonus.getType() == DamageBonusType.MULTIPLIER) {
                scalarBonus += bonus;
            }
            else {
                normalBonus += bonus;
            }
        }
        if (scalarBonus == 0D && normalBonus == 0D) return;

        double scale = 1 + (scalarBonus / 100D);
        double damageNormaled = event.getDamage() + normalBonus;
        double damageFinal = damageNormaled * scale;

        event.setDamage(Math.max(0, damageFinal));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        DamageSource source = event.getDamageSource();
        Entity directDamager = source.getDirectEntity();

        if (directDamager instanceof AbstractArrow abstractArrow) {
            if (!(abstractArrow.getShooter() instanceof LivingEntity shooter)) return;

            if (abstractArrow instanceof Arrow arrow) {
                this.manager.handleArrowEnchants(arrow, EnchantRegistry.ARROW, (item, enchant, level) -> {
                    enchant.onDamage(event, shooter, victim, arrow, level);
                    return false;
                });
            }
            else if (abstractArrow instanceof Trident trident) {
                this.manager.handleArrowEnchants(trident, EnchantRegistry.TRIDENT, (item, enchant, level) -> {
                    enchant.onDamage(event, shooter, victim, trident, level);
                    return false;
                });
            }
        }
        else if (directDamager instanceof LivingEntity damager) {
            if (source.getDamageType() == DamageType.THORNS) return;

            this.manager.handleItemEnchants(damager, EquipmentSlot.HAND, EnchantRegistry.ATTACK, (item, enchant, level) -> enchant.onAttack(event, damager, victim, item, level));
        }

        if (source.getCausingEntity() instanceof LivingEntity damager) {
            if (source.getDamageType() == DamageType.THORNS) return;

            this.manager.handleArmorEnchants(victim, EnchantRegistry.DEFEND, (item, enchant, level) -> enchant.onProtect(event, damager, victim, item, level));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true) // ignoreCancelled for Paper compatibility
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer != null) {
            this.manager.handleItemEnchants(killer, EquipmentSlot.HAND, EnchantRegistry.KILL, (item, enchant, level) -> enchant.onKill(event, entity, killer, item, level));
        }

        this.manager.handleArmorEnchants(entity, EnchantRegistry.DEATH, (item, enchant, level) -> enchant.onDeath(event, entity, item, level));

        if (event instanceof PlayerDeathEvent deathEvent) {
            Player player = deathEvent.getPlayer();
            this.manager.handleInventoryEnchants(player, EnchantRegistry.INVENTORY, (item, enchant, level) -> enchant.onDeath(deathEvent, player, item, level));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent event) {
        LivingEntity entity = event.getEntity();

        this.manager.handleArmorEnchants(entity, EnchantRegistry.RESURRECT, (item, enchant, level) -> enchant.onResurrect(event, entity, item, level));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        EquipmentSlot slot = event.getHand();
        if (slot == null) {
            slot = EnchantUtils.getItemHand(player, Material.FISHING_ROD);
        }
        if (slot == null) return;

        this.manager.handleItemEnchants(player, slot, EnchantRegistry.FISHING, (item, enchant, level) -> enchant.onFishing(event, item, level));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isFlying()) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) return;

        this.manager.handleArmorEnchants(player, EnchantRegistry.MOVE, (item, enchant, level) -> enchant.onMove(event, player, item, level));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        EquipmentSlot slot = event.getHand();
        if (slot == null) return;

        this.manager.handleItemEnchants(player, slot, EnchantRegistry.INTERACT, (item, enchant, level) -> enchant.onInteract(event, player, item, level));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        this.manager.handleItemEnchants(player, itemStack, EnchantRegistry.DURABILITY, (item, enchant, level) -> enchant.onItemDamage(event, player, item, level));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null || itemStack.getType().isAir()) return;

        this.manager.handleItemEnchants(player, itemStack, EnchantRegistry.CONTAINER, (item, enchant, level) -> enchant.onClick(event, player, item, level));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemStack = event.getItemInHand();

        BlockEnchant enchant = EnchantUtils.getBlockEnchant(itemStack);
        if (enchant == null) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        enchant.onPlace(event, player, block, itemStack);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockStoreClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getWhoClicked();

        if (inventory.getType() != InventoryType.CRAFTING) {
            int size = inventory.getSize();
            int hotkey = event.getHotbarButton();
            ItemStack itemStack = null;

            if (event.getRawSlot() >= size) {
                itemStack = event.getCurrentItem();
            }
            else if (hotkey >= 0) {
                itemStack = player.getInventory().getItem(hotkey);
            }
            else if (event.getClick() == ClickType.SWAP_OFFHAND) {
                itemStack = player.getInventory().getItemInOffHand();
            }
            if (itemStack == null) return;

            BlockEnchant enchant = EnchantUtils.getBlockEnchant(itemStack);
            if (enchant == null) return;

            if (!enchant.canPlaceInContainers()) {
                event.setCancelled(true);
                return;
            }

            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        ItemStack cursor = event.getView().getCursor(); // Creative is shit, undetectable.
        if (cursor == null) return;

        boolean isLeftClick = (event.isLeftClick() && !event.isShiftClick()) || event.getClick() == ClickType.CREATIVE;
        if (!isLeftClick) return;

        BlockEnchant enchant;
        if (cursor.getType() == Material.BUNDLE) {
            enchant = EnchantUtils.getBlockEnchant(clickedItem);
        }
        else if (clickedItem.getType() == Material.BUNDLE) {
            enchant = EnchantUtils.getBlockEnchant(cursor);
        }
        else return;

        if (enchant != null && !enchant.canPlaceInContainers()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockStoreDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();

        ItemStack itemStack = event.getCursor();
        if (itemStack == null) return;

        BlockEnchant enchant = EnchantUtils.getBlockEnchant(itemStack);
        if (enchant == null || enchant.canPlaceInContainers()) return;

        if (event.getRawSlots().stream().anyMatch(slot -> {
            ItemStack inSlot = inventory.getItem(slot);
            return inSlot != null && inSlot.getType() == Material.BUNDLE;
        })) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockHopper(InventoryPickupItemEvent event) {
        ItemStack itemStack = event.getItem().getItemStack();

        BlockEnchant enchant = EnchantUtils.getBlockEnchant(itemStack);
        if (enchant == null || enchant.canPlaceInContainers()) return;

        event.setCancelled(true);
    }
}
