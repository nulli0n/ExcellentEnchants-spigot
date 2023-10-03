package su.nightexpress.excellentenchants.enchantment.listener;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;

public class EnchantHandlerListener extends AbstractListener<ExcellentEnchants> {

    public EnchantHandlerListener(@NotNull EnchantManager enchantManager) {
        super(enchantManager.plugin());
    }

    // ---------------------------------------------------------------
    // Combat Attacking Enchants
    // ---------------------------------------------------------------
    /*@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantCombatMelee(EntityDamageEvent event) {
        if (event.getCause() == DamageCause.THORNS) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        if (event instanceof EntityDamageByEntityEvent ede) {
            if (ede.getDamager() instanceof Projectile projectile && this.getSourceWeapon(projectile) != null) {
                this.handleCombatBowEnchants(ede, projectile, victim);
                return;
            }

            if (!(ede.getDamager() instanceof LivingEntity damager) || damager == victim) return;

            this.handleCombatWeaponEnchants(ede, damager, victim);
            this.handleCombatArmorEnchants(ede, damager, victim);
        }
        else {
            this.handleArmorEnchants(event, victim);
        }
    }

    private void handleCombatWeaponEnchants(@NotNull EntityDamageByEntityEvent e,
                                            @NotNull LivingEntity damager, @NotNull LivingEntity victim) {
        EntityEquipment equipment = damager.getEquipment();
        if (equipment == null) return;

        ItemStack weapon = equipment.getItemInMainHand();
        if (weapon.getType().isAir() || weapon.getType() == Material.ENCHANTED_BOOK) return;

        EnchantUtils.getExcellents(weapon, CombatEnchant.class).forEach((combatEnchant, level) -> {
            if (combatEnchant.isOutOfCharges(weapon)) return;
            if (combatEnchant.onAttack(e, damager, victim, weapon, level)) {
                combatEnchant.consumeChargesNoUpdate(weapon, level);
            }
        });
        EnchantUtils.updateChargesDisplay(weapon);
    }

    private void handleCombatArmorEnchants(@NotNull EntityDamageByEntityEvent e,
                                           @NotNull LivingEntity damager, @NotNull LivingEntity victim) {
        EntityEquipment equipDamager = damager.getEquipment();
        if (equipDamager == null) return;

        ItemStack weaponDamager = equipDamager.getItemInMainHand();

        for (ItemStack armor : EntityUtil.getEquippedArmor(victim).values()) {
            if (armor == null || armor.getType().isAir()) continue;

            EnchantUtils.getExcellents(armor, CombatEnchant.class).forEach((combatEnchant, level) -> {
                if (combatEnchant.isOutOfCharges(armor)) return;
                if (combatEnchant.onProtect(e, damager, victim, weaponDamager, level)) {
                    combatEnchant.consumeChargesNoUpdate(armor, level);
                }
            });
            EnchantUtils.updateChargesDisplay(armor);
        }
    }

    private void handleArmorEnchants(@NotNull EntityDamageEvent e, @NotNull LivingEntity entity) {
        EnchantUtils.getEquipped(entity, DamageEnchant.class).forEach((item, enchants) -> {
            enchants.forEach((enchant, level) -> {
                if (enchant.isOutOfCharges(item)) return;
                if (enchant.onDamage(e, entity, item, level)) {
                    enchant.consumeChargesNoUpdate(item, level);
                }
            });
            EnchantUtils.updateChargesDisplay(item);
        });
    }

    private void handleCombatBowEnchants(@NotNull EntityDamageByEntityEvent e, @NotNull Projectile projectile,
                                         @NotNull LivingEntity victim) {
        if (!(projectile.getShooter() instanceof LivingEntity shooter)) return;

        ItemStack bow = this.getSourceWeapon(projectile);
        if (bow == null || bow.getType().isAir() || bow.getType() == Material.ENCHANTED_BOOK) return;

        EnchantUtils.getExcellents(bow, BowEnchant.class).forEach((bowEnchant, level) -> {
            bowEnchant.onDamage(e, projectile, shooter, victim, bow, level);
        });
    }

    // ---------------------------------------------------------------
    // Bow Shooting Enchants
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantBowShoot(EntityShootBowEvent event) {
        LivingEntity shooter = event.getEntity();
        if (shooter.getEquipment() == null) return;

        ItemStack bow = event.getBow();
        if (bow == null || bow.getType().isAir() || bow.getType() == Material.ENCHANTED_BOOK) return;

        EnchantUtils.getExcellents(bow, BowEnchant.class).forEach((bowEnchant, level) -> {
            if (bowEnchant.isOutOfCharges(bow)) return;
            if (bowEnchant.onShoot(event, shooter, bow, level)) {
                if (bowEnchant instanceof Arrowed arrowed && event.getProjectile() instanceof Projectile projectile) {
                    arrowed.addData(projectile);
                    arrowed.addTrail(projectile);
                }
                bowEnchant.consumeChargesNoUpdate(bow, level);
            }
        });
        EnchantUtils.updateChargesDisplay(bow);

        if (event.getProjectile() instanceof Projectile projectile) {
            this.setSourceWeapon(projectile, bow);
        }
    }

    // ---------------------------------------------------------------
    // Bow Hit Land Enchants
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantBowHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        ItemStack bow = this.getSourceWeapon(projectile);
        if (bow == null || bow.getType().isAir() || bow.getType() == Material.ENCHANTED_BOOK) return;

        EnchantUtils.getExcellents(bow, BowEnchant.class).forEach((bowEnchant, level) -> {
            bowEnchant.onHit(event, null, projectile, bow, level);
        });

        // Prevent to apply enchants multiple times on hits.
        this.plugin.runTask(task -> this.removeSourceWeapon(projectile));
    }

    // ---------------------------------------------------------------
    // Interaction Related Enchants
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnchantInteract(PlayerInteractEvent event) {
        if (event.useInteractedBlock() == Result.DENY) return;
        if (event.useItemInHand() == Result.DENY) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir() || item.getType() == Material.ENCHANTED_BOOK) return;

        Player player = event.getPlayer();
        EnchantUtils.getExcellents(item, InteractEnchant.class).forEach((interEnchant, level) -> {
            if (interEnchant.isOutOfCharges(item)) return;
            if (interEnchant.onInteract(event, player, item, level)) {
                interEnchant.consumeChargesNoUpdate(item, level);
            }
        });
        EnchantUtils.updateChargesDisplay(item);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEnchantFishing(PlayerFishEvent event) {
        Player player = event.getPlayer();

        ItemStack item = EnchantUtils.getFishingRod(player);
        if (item == null) return;

        EnchantUtils.getExcellents(item, FishingEnchant.class).forEach((enchant, level) -> {
            if (event.isCancelled()) return; // Check if event was cancelled by some enchantment.
            if (enchant.isOutOfCharges(item)) return;
            if (enchant.onFishing(event, item, level)) {
                enchant.consumeChargesNoUpdate(item, level);
            }
        });
        EnchantUtils.updateChargesDisplay(item);
    }

    // ---------------------------------------------------------------
    // Death Related Enchants
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnchantDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        EnchantUtils.getEquipped(entity, DeathEnchant.class).forEach((item, enchants) -> {
            enchants.forEach(((deathEnchant, level) -> {
                if (deathEnchant.isOutOfCharges(item)) return;
                if (deathEnchant.onDeath(event, entity, item, level)) {
                    deathEnchant.consumeChargesNoUpdate(item, level);
                }
            }));
            if (Config.ENCHANTMENTS_CHARGES_ENABLED.get()) {
                EnchantUtils.updateChargesDisplay(item);
            }
        });

        Player killer = entity.getKiller();
        if (killer == null) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon.getType().isAir() || weapon.getType() == Material.ENCHANTED_BOOK) return;

        EnchantUtils.getExcellents(weapon, DeathEnchant.class).forEach((deathEnchant, level) -> {
            if (deathEnchant.isOutOfCharges(weapon)) return;
            if (deathEnchant.onKill(event, entity, killer, level)) {
                deathEnchant.consumeChargesNoUpdate(weapon, level);
            }
        });
        EnchantUtils.updateChargesDisplay(weapon);
    }

    // Handle BlockBreak enchantments.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType().isAir() || tool.getType() == Material.ENCHANTED_BOOK) return;

        EnchantUtils.getExcellents(tool, BlockBreakEnchant.class).forEach((blockEnchant, level) -> {
            if (blockEnchant.isOutOfCharges(tool)) return;
            if (blockEnchant.onBreak(event, player, tool, level)) {
                blockEnchant.consumeChargesNoUpdate(tool, level);
            }
        });
        EnchantUtils.updateChargesDisplay(tool);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType().isAir() || tool.getType() == Material.ENCHANTED_BOOK) return;

        EnchantUtils.getExcellents(tool, BlockDropEnchant.class).forEach((enchant, level) -> {
            if (enchant.isOutOfCharges(tool)) return;
            if (enchant.onDrop(event, player, tool, level)) {
                enchant.consumeChargesNoUpdate(tool, level);
            }
        });
        EnchantUtils.updateChargesDisplay(tool);
    }*/
}
