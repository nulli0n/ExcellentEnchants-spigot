package su.nightexpress.excellentenchants.enchantment.registry.wrapper;

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
import su.nexmedia.engine.Version;
import su.nightexpress.excellentenchants.api.enchantment.meta.Arrowed;
import su.nightexpress.excellentenchants.api.enchantment.type.*;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

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
            if (enchant.onShoot(event, entity, item, level)) {
                if (enchant instanceof Arrowed arrowed && event.getProjectile() instanceof Projectile projectile) {
                    arrowed.addData(projectile);
                    arrowed.addTrail(projectile);
                }
                return true;
            }
            return false;
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
            Map<ItemStack, Map<BowEnchant, Integer>> map = new HashMap<>();
            ItemStack bow = EnchantUtils.getSourceWeapon(event.getEntity());
            if (bow != null) {
                map.put(bow, EnchantUtils.getExcellents(bow, enchantClass));
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

            Map<ItemStack, Map<BowEnchant, Integer>> map = new HashMap<>();
            ItemStack bow = EnchantUtils.getSourceWeapon(projectile);
            if (bow != null) {
                map.put(bow, EnchantUtils.getExcellents(bow, enchantClass));
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
            return EquipmentSlot.values();
        }

        @Override
        public boolean useEnchant(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, @NotNull DeathEnchant enchant, int level) {
            return enchant.onDeath(event, entity, item, level);
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
            if (Version.isBehind(Version.V1_19_R3)) return new EquipmentSlot[] {EquipmentSlot.HAND};

            return event.getHand() == null ? new EquipmentSlot[] {EquipmentSlot.HAND} : new EquipmentSlot[]{event.getHand()};
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
