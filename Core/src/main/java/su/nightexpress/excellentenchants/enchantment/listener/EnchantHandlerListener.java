package su.nightexpress.excellentenchants.enchantment.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.EntityUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantDropContainer;
import su.nightexpress.excellentenchants.api.enchantment.type.*;
import su.nightexpress.excellentenchants.api.enchantment.meta.Arrowed;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;

public class EnchantHandlerListener extends AbstractListener<ExcellentEnchants> {

    private static final String META_PROJECTILE_WEAPON = "sourceWeapon";

    public EnchantHandlerListener(@NotNull EnchantManager enchantManager) {
        super(enchantManager.plugin());
    }

    private void setSourceWeapon(@NotNull Projectile projectile, @NotNull ItemStack item) {
        projectile.setMetadata(META_PROJECTILE_WEAPON, new FixedMetadataValue(plugin, item));
    }

    @Nullable
    private ItemStack getSourceWeapon(@NotNull Projectile projectile) {
        return projectile.hasMetadata(META_PROJECTILE_WEAPON) ? (ItemStack) projectile.getMetadata(META_PROJECTILE_WEAPON).get(0).value() : null;
    }

    private void removeSourceWeapon(@NotNull Projectile projectile) {
        projectile.removeMetadata(META_PROJECTILE_WEAPON, plugin);
    }

    // ---------------------------------------------------------------
    // Combat Attacking Enchants
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantCombatMelee(EntityDamageEvent e) {
        if (e.getCause() == DamageCause.THORNS) return;
        if (!(e.getEntity() instanceof LivingEntity victim)) return;

        if (e instanceof EntityDamageByEntityEvent ede) {
            LivingEntity damager = null;
            if (ede.getDamager() instanceof LivingEntity living) {
                damager = living;
            }
            else if (ede.getDamager() instanceof Projectile pj && pj.getShooter() instanceof LivingEntity living) {
                damager = living;
            }
            if (damager == null || damager.equals(victim)) return;

            if (ede.getDamager() instanceof Projectile projectile) {
                this.handleCombatBowEnchants(ede, projectile, victim);
            }
            else {
                this.handleCombatWeaponEnchants(ede, damager, victim);
            }
            this.handleCombatArmorEnchants(ede, damager, victim);
        }
        else {
            this.handleArmorEnchants(e, victim);
        }
    }

    private void handleCombatWeaponEnchants(@NotNull EntityDamageByEntityEvent e,
                                            @NotNull LivingEntity damager, @NotNull LivingEntity victim) {
        EntityEquipment equipment = damager.getEquipment();
        if (equipment == null) return;

        ItemStack weapon = equipment.getItemInMainHand();
        if (weapon.getType().isAir() || weapon.getType() == Material.ENCHANTED_BOOK) return;

        EnchantManager.getExcellentEnchantments(weapon, CombatEnchant.class).forEach((combatEnchant, level) -> {
            if (combatEnchant.isOutOfCharges(weapon)) return;
            if (combatEnchant.onAttack(e, damager, victim, weapon, level)) {
                combatEnchant.consumeCharges(weapon);
            }
        });
    }

    private void handleCombatArmorEnchants(@NotNull EntityDamageByEntityEvent e,
                                           @NotNull LivingEntity damager, @NotNull LivingEntity victim) {
        EntityEquipment equipDamager = damager.getEquipment();
        if (equipDamager == null) return;

        ItemStack weaponDamager = equipDamager.getItemInMainHand();

        for (ItemStack armor : EntityUtil.getEquippedArmor(victim).values()) {
            if (armor == null || armor.getType().isAir()) continue;

            EnchantManager.getExcellentEnchantments(armor, CombatEnchant.class).forEach((combatEnchant, level) -> {
                if (combatEnchant.isOutOfCharges(armor)) return;
                if (combatEnchant.onProtect(e, damager, victim, weaponDamager, level)) {
                    combatEnchant.consumeCharges(armor);
                }
            });
        }
    }

    private void handleArmorEnchants(@NotNull EntityDamageEvent e, @NotNull LivingEntity entity) {
        EnchantManager.getEquippedEnchants(entity, DamageEnchant.class).forEach((item, enchants) -> {
            enchants.forEach((enchant, level) -> {
                if (enchant.isOutOfCharges(item)) return;
                if (enchant.onDamage(e, entity, item, level)) {
                    enchant.consumeCharges(item);
                }
            });
        });
    }

    private void handleCombatBowEnchants(@NotNull EntityDamageByEntityEvent e, @NotNull Projectile projectile,
                                         @NotNull LivingEntity victim) {
        if (!(projectile.getShooter() instanceof LivingEntity shooter)) return;

        ItemStack bow = this.getSourceWeapon(projectile);
        if (bow == null || bow.getType().isAir() || bow.getType() == Material.ENCHANTED_BOOK) return;

        EnchantManager.getExcellentEnchantments(bow, BowEnchant.class).forEach((bowEnchant, level) -> {
            bowEnchant.onDamage(e, projectile, shooter, victim, bow, level);
        });
    }

    // ---------------------------------------------------------------
    // Bow Shooting Enchants
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantBowShoot(EntityShootBowEvent e) {
        LivingEntity shooter = e.getEntity();
        if (shooter.getEquipment() == null) return;

        ItemStack bow = e.getBow();
        if (bow == null || bow.getType().isAir() || bow.getType() == Material.ENCHANTED_BOOK) return;

        EnchantManager.getExcellentEnchantments(bow, BowEnchant.class).forEach((bowEnchant, level) -> {
            if (bowEnchant.isOutOfCharges(bow)) return;
            if (bowEnchant.onShoot(e, shooter, bow, level)) {
                if (bowEnchant instanceof Arrowed arrowed && e.getProjectile() instanceof Projectile projectile) {
                    arrowed.addData(projectile);
                    arrowed.addTrail(projectile);
                }
                bowEnchant.consumeCharges(bow);
            }
        });

        if (e.getProjectile() instanceof Projectile projectile) {
            this.setSourceWeapon(projectile, bow);
        }
    }

    // ---------------------------------------------------------------
    // Bow Hit Land Enchants
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantBowHit(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();

        ItemStack bow = this.getSourceWeapon(projectile);
        if (bow == null || bow.getType().isAir() || bow.getType() == Material.ENCHANTED_BOOK) return;

        EnchantManager.getExcellentEnchantments(bow, BowEnchant.class).forEach((bowEnchant, level) -> {
            bowEnchant.onHit(e, projectile, bow, level);
        });

        // Prevent to apply enchants multiple times on hits.
        this.plugin.getScheduler().runTask(this.plugin, c -> this.removeSourceWeapon(projectile));
    }

    // ---------------------------------------------------------------
    // Interaction Related Enchants
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnchantInteract(PlayerInteractEvent e) {
        if (e.useInteractedBlock() == Result.DENY) return;
        if (e.useItemInHand() == Result.DENY) return;

        ItemStack item = e.getItem();
        if (item == null || item.getType().isAir() || item.getType() == Material.ENCHANTED_BOOK) return;

        Player player = e.getPlayer();
        EnchantManager.getExcellentEnchantments(item, InteractEnchant.class).forEach((interEnchant, level) -> {
            if (interEnchant.isOutOfCharges(item)) return;
            if (interEnchant.onInteract(e, player, item, level)) {
                interEnchant.consumeCharges(item);
            }
        });
    }

    // ---------------------------------------------------------------
    // Death Related Enchants
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnchantDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();

        EnchantManager.getEquippedEnchants(entity, DeathEnchant.class).forEach((item, enchants) -> {
            enchants.forEach(((deathEnchant, level) -> {
                if (deathEnchant.isOutOfCharges(item)) return;
                if (deathEnchant.onDeath(e, entity, level)) {
                    deathEnchant.consumeCharges(item);
                }
            }));
        });

        Player killer = entity.getKiller();
        if (killer == null) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon.getType().isAir() || weapon.getType() == Material.ENCHANTED_BOOK) return;

        EnchantManager.getExcellentEnchantments(weapon, DeathEnchant.class).forEach((deathEnchant, level) -> {
            if (deathEnchant.isOutOfCharges(weapon)) return;
            if (deathEnchant.onKill(e, entity, killer, level)) {
                deathEnchant.consumeCharges(weapon);
            }
        });
    }

    // Handle BlockBreak enchantments.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType().isAir() || tool.getType() == Material.ENCHANTED_BOOK) return;

        EnchantManager.getExcellentEnchantments(tool, BlockBreakEnchant.class).forEach((blockEnchant, level) -> {
            if (blockEnchant.isOutOfCharges(tool)) return;
            if (blockEnchant.onBreak(e, player, tool, level)) {
                blockEnchant.consumeCharges(tool);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantBlockDropItem(BlockDropItemEvent e) {
        Player player = e.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType().isAir() || tool.getType() == Material.ENCHANTED_BOOK) return;

        EnchantDropContainer dropContainer = new EnchantDropContainer(e);
        EnchantManager.getExcellentEnchantments(tool, BlockDropEnchant.class).forEach((enchant, level) -> {
            if (enchant.isOutOfCharges(tool)) return;
            if (enchant.onDrop(e, dropContainer, player, tool, level)) {
                enchant.consumeCharges(tool);
            }
        });

        BlockState state = e.getBlockState();
        World world = state.getWorld();
        Location location = state.getLocation();

        dropContainer.getDrop().forEach(item -> world.dropItem(location, item));
    }
}
