package su.nightexpress.excellentenchants.enchantment;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.impl.armor.FlameWalkerEnchant;
import su.nightexpress.excellentenchants.enchantment.listener.EnchantAnvilListener;
import su.nightexpress.excellentenchants.enchantment.listener.EnchantGenericListener;
import su.nightexpress.excellentenchants.enchantment.listener.EnchantPopulationListener;
import su.nightexpress.excellentenchants.enchantment.listener.EnchantVanillaListener;
import su.nightexpress.excellentenchants.enchantment.menu.EnchantmentsListMenu;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.Pair;

import java.util.*;

public class EnchantManager extends AbstractManager<EnchantsPlugin> {

    private final Set<Pair<PassiveEnchant, EnchantmentData>> passiveEnchants;

    private EnchantmentsListMenu enchantmentsListMenu;

    public EnchantManager(@NotNull EnchantsPlugin plugin) {
        super(plugin);
        this.passiveEnchants = new HashSet<>();

        EnchantRegistry.getEnchantments(PassiveEnchant.class).forEach(passiveEnchant -> {
            EnchantmentData enchantmentData = EnchantRegistry.getById(passiveEnchant.getId());
            if (enchantmentData == null) return;

            this.passiveEnchants.add(Pair.of(passiveEnchant, enchantmentData));
        });
    }

    protected void onLoad() {
        this.enchantmentsListMenu = new EnchantmentsListMenu(this.plugin);

        this.addListener(new EnchantGenericListener(this.plugin, this));
        this.addListener(new EnchantAnvilListener(this.plugin));

        if (Config.isCustomDistribution()) {
            this.plugin.info("Using custom distribution system. Applying patches...");
            this.addListener(new EnchantPopulationListener(this.plugin));
        }
        else {
            this.plugin.info("Using vanilla distribution. Applying enchanting table patches...");
            this.addListener(new EnchantVanillaListener(this.plugin));
        }

        this.addTask(this.plugin.createAsyncTask(this::displayProjectileTrails).setTicksInterval(Config.CORE_PROJECTILE_PARTICLE_INTERVAL.get()));
        this.addTask(this.plugin.createTask(this::updatePassiveEnchantEffects).setTicksInterval(Config.CORE_PASSIVE_ENCHANTS_TRIGGER_INTERVAL.get()));
        if (EnchantRegistry.isRegistered(FlameWalkerEnchant.ID)) {
            this.addTask(this.plugin.createTask(FlameWalkerEnchant::tickBlocks).setSecondsInterval(1));
        }
    }

    @Override
    protected void onShutdown() {
        if (this.enchantmentsListMenu != null) this.enchantmentsListMenu.clear();

        /*if (EnchantRegistry.isRegistered(FlameWalkerEnchant.ID)) {
            FlameWalkerEnchant.clear();
        }*/
    }

    @NotNull
    public EnchantmentsListMenu getEnchantmentsListMenu() {
        return enchantmentsListMenu;
    }

    public void openEnchantsMenu(@NotNull Player player) {
        this.enchantmentsListMenu.open(player);
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
        if (this.passiveEnchants.isEmpty()) return;

        Set<LivingEntity> entities = this.getPassiveEnchantEntities();

        this.passiveEnchants.forEach(pair -> {
            PassiveEnchant enchant = pair.getFirst();
            EnchantmentData enchantmentData = pair.getSecond();
            if (!enchant.isTriggerTime()) return;

            for (LivingEntity entity : entities) {
                EnchantUtils.getEquipped(entity, enchantmentData).forEach((item, level) -> {
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

