package su.nightexpress.excellentenchants.enchantment.registry.wrapper;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.enchantment.data.ArrowData;
import su.nightexpress.excellentenchants.api.enchantment.type.*;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.excellentenchants.enchantment.util.EnchantedProjectile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DataGathers {

    public static final DataGather<BlockBreakEvent, BlockBreakEnchant> BLOCK_BREAK = new DataGather<>() {

        @Override
        @NotNull
        public LivingEntity getEntity(@NotNull BlockBreakEvent event) {
            return event.getPlayer();
        }

        @Override
        public boolean checkPriority(@NotNull BlockBreakEnchant enchant, @NotNull EventPriority priority) {
            return enchant.getBreakPriority() == priority;
        }

        @NotNull
        @Override
        public EquipmentSlot[] getEnchantSlots(@NotNull BlockBreakEvent event) {
            return new EquipmentSlot[]{EquipmentSlot.HAND};
        }

        @Override
        public boolean useEnchant(@NotNull BlockBreakEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, @NotNull BlockBreakEnchant enchant, int level) {
            return enchant.onBreak(event, entity, item, level);
        }
    };

    public static final DataGather<BlockDropItemEvent, BlockDropEnchant> BLOCK_DROP = new DataGather<>() {

        @Override
        @NotNull
        public LivingEntity getEntity(@NotNull BlockDropItemEvent event) {
            return event.getPlayer();
        }

        @Override
        public boolean checkPriority(@NotNull BlockDropEnchant enchant, @NotNull EventPriority priority) {
            return enchant.getDropPriority() == priority;
        }

        @NotNull
        @Override
        public EquipmentSlot[] getEnchantSlots(@NotNull BlockDropItemEvent event) {
            return new EquipmentSlot[]{EquipmentSlot.HAND};
        }

        @Override
        public boolean useEnchant(@NotNull BlockDropItemEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, @NotNull BlockDropEnchant enchant, int level) {
            return enchant.onDrop(event, entity, item, level);
        }
    };

    public static final DataGather<EntityShootBowEvent, BowEnchant> BOW_SHOOT = new DataGather<>() {

        @Override
        @NotNull
        public LivingEntity getEntity(@NotNull EntityShootBowEvent event) {
            return event.getEntity();
        }

        @Override
        public boolean checkPriority(@NotNull BowEnchant enchant, @NotNull EventPriority priority) {
            return enchant.getShootPriority() == priority;
        }

        @NotNull
        @Override
        public EquipmentSlot[] getEnchantSlots(@NotNull EntityShootBowEvent event) {
            return new EquipmentSlot[]{event.getHand()};
        }

        @Override
        public boolean useEnchant(@NotNull EntityShootBowEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, @NotNull BowEnchant enchant, int level) {
            boolean onShoot = enchant.onShoot(event, entity, item, level);

            if (event.getProjectile() instanceof Projectile projectile) {
                EnchantedProjectile enchantedProjectile = EnchantUtils.getEnchantedProjectile(projectile);
                if (enchantedProjectile != null && onShoot) {
                    enchantedProjectile.getEnchantments().put(enchant, level);
                    if (enchant.hasVisualEffects() && enchant instanceof ArrowData arrowData && !arrowData.getProjectileTrail().isEmpty()) {
                        enchantedProjectile.getParticles().add(arrowData.getProjectileTrail());
                    }
                }
            }

            return onShoot;
        }
    };

    public static final DataGather<ProjectileHitEvent, BowEnchant> PROJECTILE_HIT = new DataGather<>() {

        @Override
        @Nullable
        public LivingEntity getEntity(@NotNull ProjectileHitEvent event) {
            return event.getEntity().getShooter() instanceof LivingEntity entity ? entity : null;
        }

        @Override
        public boolean checkPriority(@NotNull BowEnchant enchant, @NotNull EventPriority priority) {
            return enchant.getHitPriority() == priority;
        }

        @NotNull
        @Override
        public EquipmentSlot[] getEnchantSlots(@NotNull ProjectileHitEvent event) {
            return new EquipmentSlot[0];
        }

        @NotNull
        @Override
        public Map<ItemStack, Map<BowEnchant, Integer>> getEnchants(@NotNull ProjectileHitEvent event, @NotNull Class<BowEnchant> enchantClass, @NotNull LivingEntity entity) {
            EnchantedProjectile enchantedProjectile = EnchantUtils.getEnchantedProjectile(event.getEntity());
            if (enchantedProjectile == null) return Collections.emptyMap();

            Map<ItemStack, Map<BowEnchant, Integer>> map = new HashMap<>();
            ItemStack bow = enchantedProjectile.getItem();
            if (bow != null) {
                map.put(bow, enchantedProjectile.getEnchantments());
            }
            return map;
        }

        @Override
        public boolean useEnchant(@NotNull ProjectileHitEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, @NotNull BowEnchant enchant, int level) {
            return enchant.onHit(event, entity, event.getEntity(), item, level);
        }
    };

    public static final DataGather<EntityDamageByEntityEvent, BowEnchant> ENTITY_DAMAGE_SHOOT = new DataGather<>() {

        @Override
        @Nullable
        public LivingEntity getEntity(@NotNull EntityDamageByEntityEvent event) {
            if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof LivingEntity shooter) {
                return shooter;
            }
            return null;
        }

        @Override
        public boolean checkPriority(@NotNull BowEnchant enchant, @NotNull EventPriority priority) {
            return enchant.getDamagePriority() == priority;
        }

        @NotNull
        @Override
        public EquipmentSlot[] getEnchantSlots(@NotNull EntityDamageByEntityEvent event) {
            return new EquipmentSlot[0];
        }

        @NotNull
        @Override
        public Map<ItemStack, Map<BowEnchant, Integer>> getEnchants(@NotNull EntityDamageByEntityEvent event, @NotNull Class<BowEnchant> enchantClass, @NotNull LivingEntity entity) {
            if (!(event.getDamager() instanceof Projectile projectile)) return Collections.emptyMap();

            EnchantedProjectile enchantedProjectile = EnchantUtils.getEnchantedProjectile(projectile);
            if (enchantedProjectile == null) return Collections.emptyMap();

            Map<ItemStack, Map<BowEnchant, Integer>> map = new HashMap<>();
            ItemStack bow = enchantedProjectile.getItem();
            if (bow != null) {
                map.put(bow, enchantedProjectile.getEnchantments());
            }
            return map;
        }

        @Override
        public boolean useEnchant(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull ItemStack item, @NotNull BowEnchant enchant, int level) {
            if (!(event.getDamager() instanceof Projectile projectile)) return false;
            if (!(event.getEntity() instanceof LivingEntity victim)) return false;

            return enchant.onDamage(event, projectile, damager, victim, item, level);
        }
    };

    public static final DataGather<EntityDamageByEntityEvent, CombatEnchant> ENTITY_DAMAGE_ATTACK = new DataGather<>() {

        @Override
        @Nullable
        public LivingEntity getEntity(@NotNull EntityDamageByEntityEvent event) {
            if (event.getDamager() instanceof LivingEntity entity) {
                return entity;
            }
            return null;
        }

        @Override
        public boolean checkPriority(@NotNull CombatEnchant enchant, @NotNull EventPriority priority) {
            return enchant.getAttackPriority() == priority;
        }

        @NotNull
        @Override
        public EquipmentSlot[] getEnchantSlots(@NotNull EntityDamageByEntityEvent event) {
            return new EquipmentSlot[]{EquipmentSlot.HAND};
        }

        @Override
        public boolean useEnchant(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull ItemStack item, @NotNull CombatEnchant enchant, int level) {
            if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) return false;
            if (!(event.getEntity() instanceof LivingEntity victim)) return false;

            return enchant.onAttack(event, damager, victim, item, level);
        }
    };

    public static final DataGather<EntityDamageByEntityEvent, CombatEnchant> ENTITY_DAMAGE_DEFENSE = new DataGather<>() {

        @Override
        @Nullable
        public LivingEntity getEntity(@NotNull EntityDamageByEntityEvent event) {
            Entity entity = event.getEntity();
            return entity instanceof Player player ? player : null;
        }

        @Override
        public boolean checkPriority(@NotNull CombatEnchant enchant, @NotNull EventPriority priority) {
            return enchant.getProtectPriority() == priority;
        }

        @NotNull
        @Override
        public EquipmentSlot[] getEnchantSlots(@NotNull EntityDamageByEntityEvent event) {
            return new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        }

        @Override
        public boolean useEnchant(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity victim, @NotNull ItemStack item, @NotNull CombatEnchant enchant, int level) {
            if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) return false;
            if (!(event.getDamager() instanceof LivingEntity damager)) return false;

            return enchant.onProtect(event, damager, victim, item, level);
        }
    };

    public static final DataGather<EntityDeathEvent, DeathEnchant> ENTITY_KILL = new DataGather<>() {

        @Override
        @Nullable
        public LivingEntity getEntity(@NotNull EntityDeathEvent event) {
            return event.getEntity().getKiller();
        }

        @Override
        public boolean checkPriority(@NotNull DeathEnchant enchant, @NotNull EventPriority priority) {
            return enchant.getKillPriority() == priority;
        }

        @NotNull
        @Override
        public EquipmentSlot[] getEnchantSlots(@NotNull EntityDeathEvent event) {
            return new EquipmentSlot[] {EquipmentSlot.HAND};
        }

        @Override
        public boolean useEnchant(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, @NotNull DeathEnchant enchant, int level) {
            if (!(entity instanceof Player killer)) return false;

            return enchant.onKill(event, event.getEntity(), killer, item, level);
        }
    };

    public static final DataGather<EntityDeathEvent, DeathEnchant> ENTITY_DEATH = new DataGather<>() {

        @Override
        @NotNull
        public LivingEntity getEntity(@NotNull EntityDeathEvent event) {
            return event.getEntity();
        }

        @Override
        public boolean checkPriority(@NotNull DeathEnchant enchant, @NotNull EventPriority priority) {
            return enchant.getDeathPriority() == priority;
        }

        @NotNull
        @Override
        public EquipmentSlot[] getEnchantSlots(@NotNull EntityDeathEvent event) {
            return EnchantUtils.EQUIPMENT_SLOTS;
        }

        @Override
        public boolean useEnchant(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, @NotNull DeathEnchant enchant, int level) {
            return enchant.onDeath(event, entity, item, level);
        }
    };

    public static final DataGather<EntityResurrectEvent, DeathEnchant> ENTITY_RESURRECT = new DataGather<>() {

        @Override
        @NotNull
        public LivingEntity getEntity(@NotNull EntityResurrectEvent event) {
            return event.getEntity();
        }

        @Override
        public boolean checkPriority(@NotNull DeathEnchant enchant, @NotNull EventPriority priority) {
            return enchant.getDeathPriority() == priority;
        }

        @NotNull
        @Override
        public EquipmentSlot[] getEnchantSlots(@NotNull EntityResurrectEvent event) {
            return EnchantUtils.EQUIPMENT_SLOTS;
        }

        @Override
        public boolean useEnchant(@NotNull EntityResurrectEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, @NotNull DeathEnchant enchant, int level) {
            return enchant.onResurrect(event, entity, item, level);
        }
    };

    public static final DataGather<PlayerFishEvent, FishingEnchant> FISHING = new DataGather<>() {

        @Override
        @NotNull
        public LivingEntity getEntity(@NotNull PlayerFishEvent event) {
            return event.getPlayer();
        }

        @Override
        public boolean checkPriority(@NotNull FishingEnchant enchant, @NotNull EventPriority priority) {
            return enchant.getFishingPriority() == priority;
        }

        @NotNull
        @Override
        public EquipmentSlot[] getEnchantSlots(@NotNull PlayerFishEvent event) {
            return event.getHand() == null ? new EquipmentSlot[] {EnchantUtils.getItemHand(event.getPlayer(), Material.FISHING_ROD)} : new EquipmentSlot[]{event.getHand()};
        }

        @Override
        public boolean useEnchant(@NotNull PlayerFishEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, @NotNull FishingEnchant enchant, int level) {
            return enchant.onFishing(event, item, level);
        }
    };

    public static final DataGather<PlayerInteractEvent, InteractEnchant> INTERACT = new DataGather<>() {

        @NotNull
        @Override
        public LivingEntity getEntity(@NotNull PlayerInteractEvent event) {
            return event.getPlayer();
        }

        @Override
        public boolean checkPriority(@NotNull InteractEnchant enchant, @NotNull EventPriority priority) {
            return enchant.getInteractPriority() == priority;
        }

        @NotNull
        @Override
        public EquipmentSlot[] getEnchantSlots(@NotNull PlayerInteractEvent event) {
            return event.getHand() == null ? new EquipmentSlot[] {EquipmentSlot.HAND} : new EquipmentSlot[]{event.getHand()};
        }

        @Override
        public boolean useEnchant(@NotNull PlayerInteractEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, @NotNull InteractEnchant enchant, int level) {
            return enchant.onInteract(event, entity, item, level);
        }
    };
}
