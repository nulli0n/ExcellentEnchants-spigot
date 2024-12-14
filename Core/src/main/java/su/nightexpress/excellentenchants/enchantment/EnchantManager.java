package su.nightexpress.excellentenchants.enchantment;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.ConfigBridge;
import su.nightexpress.excellentenchants.api.EnchantmentID;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.bridge.FlameWalker;
import su.nightexpress.excellentenchants.api.enchantment.meta.PeriodMeta;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.listener.AnvilListener;
import su.nightexpress.excellentenchants.enchantment.listener.GenericListener;
import su.nightexpress.excellentenchants.enchantment.menu.EnchantsMenu;
import su.nightexpress.excellentenchants.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.manager.AbstractManager;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EnchantManager extends AbstractManager<EnchantsPlugin> {

    private final Set<PassiveEnchant> passiveEnchants;

    private EnchantsMenu enchantsMenu;

    public EnchantManager(@NotNull EnchantsPlugin plugin) {
        super(plugin);

        this.passiveEnchants = new HashSet<>(EnchantRegistry.getEnchantments(PassiveEnchant.class));
    }

    protected void onLoad() {
        this.enchantsMenu = new EnchantsMenu(this.plugin);

        this.addListener(new GenericListener(this.plugin, this));
        this.addListener(new AnvilListener(this.plugin));

        this.addAsyncTask(this::displayProjectileTrails, Config.CORE_PROJECTILE_PARTICLE_INTERVAL.get());

        if (!this.passiveEnchants.isEmpty()) {
            this.addTask(this::updatePassiveEnchantEffects, ConfigBridge.getEnchantsTickInterval());
        }

        CustomEnchantment enchantment = EnchantRegistry.getById(EnchantmentID.FLAME_WALKER);
        if (enchantment instanceof FlameWalker flameWalker) {
            this.addTask(flameWalker::tickBlocks, 1);
        }
    }

    @Override
    protected void onShutdown() {
        CustomEnchantment enchantment = EnchantRegistry.getById(EnchantmentID.FLAME_WALKER);
        if (enchantment instanceof FlameWalker flameWalker) {
            flameWalker.removeBlocks();
        }

        if (this.enchantsMenu != null) this.enchantsMenu.clear();
    }

    public void openEnchantsMenu(@NotNull Player player) {
        this.enchantsMenu.open(player);
    }

    private void displayProjectileTrails() {
        EnchantUtils.getEnchantedProjectiles().removeIf(enchantedProjectile -> {
            if (!enchantedProjectile.isValid()) {
                return true;
            }

            enchantedProjectile.playParticles();
            return false;
        });
    }

    private void updatePassiveEnchantEffects() {
        Set<PassiveEnchant> readyEnchants = this.passiveEnchants.stream()
            .peek(PeriodMeta::consumeTicks)
            .filter(PeriodMeta::isTriggerTime)
            .collect(Collectors.toSet());
        if (readyEnchants.isEmpty()) return;

        Set<LivingEntity> entities = this.getPassiveEnchantEntities();

        readyEnchants.forEach(enchant -> {
            for (LivingEntity entity : entities) {
                EnchantUtils.getEquipped(entity, enchant).forEach((item, level) -> {
                    if (!enchant.isAvailableToUse(entity)) return;
                    if (enchant.isOutOfCharges(item)) return;
                    if (enchant.onTrigger(entity, item, level)) {
                        enchant.consumeCharges(item, level);
                    }
                });
            }

            enchant.updateTriggerTime();
        });
    }

    @NotNull
    private Set<LivingEntity> getPassiveEnchantEntities() {
        Set<LivingEntity> list = new HashSet<>(plugin.getServer().getOnlinePlayers());

        if (Config.CORE_PASSIVE_ENCHANTS_FOR_MOBS.get()) {
            plugin.getServer().getWorlds().stream().filter(world -> !world.getPlayers().isEmpty()).forEach(world -> {
                list.addAll(world.getEntitiesByClass(LivingEntity.class));
            });
        }
        list.removeIf(entity -> entity.isDead() || !entity.isValid());
        return list;
    }
}

