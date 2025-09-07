package su.nightexpress.excellentenchants.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.*;
import su.nightexpress.excellentenchants.api.config.ConfigBridge;
import su.nightexpress.excellentenchants.api.config.DistributionConfig;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.type.ProjectileEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.manager.block.TickedBlock;
import su.nightexpress.excellentenchants.manager.damage.Explosion;
import su.nightexpress.excellentenchants.manager.listener.AnvilListener;
import su.nightexpress.excellentenchants.manager.listener.EnchantListener;
import su.nightexpress.excellentenchants.manager.listener.GenericListener;
import su.nightexpress.excellentenchants.manager.menu.EnchantsMenu;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.Version;
import su.nightexpress.nightcore.util.bridge.RegistryType;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class EnchantManager extends AbstractManager<EnchantsPlugin> {

    private final Map<AbstractArrow, Set<UniParticle>> arrowEffects;
    private final Map<Location, TickedBlock>           tickedBlocks;
    private final Map<UUID, Explosion>                 explosions;

    private EnchantsMenu enchantsMenu;

    public EnchantManager(@NotNull EnchantsPlugin plugin) {
        super(plugin);
        this.arrowEffects = new ConcurrentHashMap<>();
        this.tickedBlocks = new HashMap<>();
        this.explosions = new HashMap<>();
    }

    protected void onLoad() {
        this.loadEnchants();

        this.enchantsMenu = new EnchantsMenu(this.plugin);

        this.addListener(new GenericListener(this.plugin, this));
        this.addListener(new AnvilListener(this.plugin));
        this.addListener(new EnchantListener(this.plugin, this));

        this.addAsyncTask(this::tickArrowEffects, Config.ARROW_EFFECTS_TICK_INTERVAL.get());

        if (!EnchantRegistry.PASSIVE.isEmpty()) {
            this.addTask(this::tickPassiveEnchants, Config.PASSIVE_ENCHANTS_TICK_INTERVAL.get());
        }

        this.addTask(this::tickBlocks, 1L);
    }

    @Override
    protected void onShutdown() {
        this.restoreBlocks();

        if (this.enchantsMenu != null) this.enchantsMenu.clear();

        this.arrowEffects.clear();
        this.tickedBlocks.clear();
        this.explosions.clear();
    }

    private void loadEnchants() {
        if (EnchantRegistry.isLocked()) {
            EnchantRegistry.getRegistered().forEach(CustomEnchantment::load);
            return;
        }

        if (Version.isSpigot()) {
            this.plugin.getRegistryHack().unfreezeRegistry();
        }

        EnchantRegistry.getDataMap().forEach((enchantId, data) -> {
            if (DistributionConfig.isDisabled(enchantId)) return;

            EnchantProvider<?> provider = EnchantRegistry.getProviderById(enchantId);
            if (provider == null) {
                this.plugin.error("No provider present for the '" + enchantId + "' enchantment!");
                return;
            }

            this.loadEnchant(enchantId, data, provider);
        });

        if (Version.isSpigot()) {
            EnchantRegistry.getRegistered().forEach(enchantment -> this.plugin.getRegistryHack().addExclusives(enchantment));

            this.plugin.getRegistryHack().freezeRegistry();
        }

        EnchantRegistry.lock();

        this.plugin.info("Loaded " + EnchantRegistry.getRegistered().size() + " enchantments.");
    }

    private boolean loadEnchant(@NotNull String id, @NotNull EnchantData data, @NotNull EnchantProvider<?> provider) {
        File file = new File(plugin.getDataFolder() + ConfigBridge.DIR_ENCHANTS, id + ".yml");
        if (!file.exists()) {
            this.plugin.error("No config present for the '" + id + "' enchantment.");
            return false;
        }

        CustomEnchantment enchantment = provider.create(file, data);

        Enchantment bukkitEnchant;
        if (Version.isSpigot()) {
            bukkitEnchant = this.plugin.getRegistryHack().registerEnchantment(enchantment);
        }
        else {
            bukkitEnchant = RegistryType.ENCHANTMENT.getRegistry().get(EnchantKeys.custom(id));
        }

        if (bukkitEnchant == null) {
            this.plugin.error("No registered bukkit enchant found for '" + id + "'.");
            return false;
        }

        enchantment.load();
        enchantment.onRegister(bukkitEnchant);
        EnchantRegistry.registerEnchant(enchantment);
        return true;
    }

    public void openEnchantsMenu(@NotNull Player player) {
        this.enchantsMenu.open(player);
    }

    public void addArrowEffect(@NotNull AbstractArrow arrow, @NotNull UniParticle particle) {
        this.arrowEffects.computeIfAbsent(arrow, k -> new HashSet<>()).add(particle);
    }

    public void removeArrowEffects(@NotNull AbstractArrow arrow) {
        this.arrowEffects.remove(arrow);
    }

    private void tickArrowEffects() {
        this.arrowEffects.keySet().removeIf(arrow -> !arrow.isValid() || arrow.isDead());
        this.arrowEffects.forEach((arrow, effects) -> effects.forEach(particle -> particle.play(arrow.getLocation(), 0f, 0f, 10)));
    }

    private void tickBlocks() {
        this.tickedBlocks.values().removeIf(tickedBlock -> {
            this.plugin.runTask(tickedBlock.getLocation(), tickedBlock::tick);
            return tickedBlock.isDead();
        });
    }

    private void restoreBlocks() {
        this.tickedBlocks.values().forEach(TickedBlock::restore);
    }

    private void tickPassiveEnchants() {
        this.getPassiveEnchantEntities().forEach(entity -> this.plugin.runTask(entity, () -> this.handleArmorEnchants(entity, EnchantRegistry.PASSIVE, (item, enchant, level) -> enchant.onTrigger(entity, item, level))));
    }

    @NotNull
    private Set<LivingEntity> getPassiveEnchantEntities() {
        Set<LivingEntity> entities = new HashSet<>(Players.getOnline());

        if (Config.PASSIVE_ENCHANTS_ALLOW_FOR_MOBS.get()) {
            Bukkit.getWorlds().forEach(world -> entities.addAll(world.getLivingEntities()));
        }

        entities.removeIf(Entity::isDead);

        return entities;
    }

    public void addTickedBlock(@NotNull Block block, @NotNull Material origin, @NotNull Material transform, int lifeTime) {
        Location location = block.getLocation();
        TickedBlock tickedBlock = new TickedBlock(location, origin, lifeTime);
        this.tickedBlocks.put(location, tickedBlock);

        this.plugin.runTask(location, () -> block.setType(transform));
    }

    public boolean removeTickedBlock(@NotNull Block block) {
        return this.removeTickedBlock(block.getLocation());
    }

    public boolean removeTickedBlock(@NotNull Location location) {
        TickedBlock tickedBlock = this.tickedBlocks.remove(location);
        if (tickedBlock == null) return false;

        this.plugin.runTask(location, tickedBlock::restore);
        return true;
    }

    public boolean createExplosion(@NotNull LivingEntity entity, @NotNull Location location, float power, boolean fire, boolean destroy, @NotNull Consumer<Explosion> consumer) {
        Explosion explosion = new Explosion(entity);
        consumer.accept(explosion);

        this.explosions.put(entity.getUniqueId(), explosion);

        final boolean[] result = {false};
        this.plugin.runTask(location, () -> result[0] = entity.getWorld().createExplosion(location, power, fire, destroy, entity));
        return result[0];
    }

    public void handleEnchantExplosion(@NotNull EntityExplodeEvent event, @NotNull LivingEntity entity) {
        Explosion explosion = this.explosions.get(entity.getUniqueId());
        if (explosion == null) return;

        explosion.handleExplosion(event);

        this.plugin.runTask(entity, () -> this.explosions.remove(entity.getUniqueId()));
    }

    public void handleEnchantExplosionDamage(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity entity) {
        Explosion explosion = this.explosions.get(entity.getUniqueId());
        if (explosion == null) return;

        explosion.handleDamage(event);
    }

    public <T extends CustomEnchantment> void handleArmorEnchants(@NotNull LivingEntity entity, @NotNull EnchantHolder<T> holder, @NotNull EnchantUsage<T> usage) {
        this.handleFully(entity, EnchantUtils.getEquipped(entity, holder), holder::getPriority, usage);
    }

    public <T extends CustomEnchantment> void handleInventoryEnchants(@NotNull Player player, @NotNull EnchantHolder<T> holder, @NotNull EnchantUsage<T> usage) {
        this.handleFully(player, EnchantUtils.getAll(player, holder), holder::getPriority, usage);
    }

    public <T extends CustomEnchantment> void handleItemEnchants(@NotNull LivingEntity entity,
                                                                 @NotNull EquipmentSlot slot,
                                                                 @NotNull EnchantHolder<T> holder,
                                                                 @NotNull EnchantUsage<T> usage) {
        ItemStack itemStack = EnchantUtils.getEquipped(entity, slot);
        if (itemStack == null || !EnchantUtils.isEquipment(itemStack)) return;

        this.handleItemEnchants(entity, itemStack, holder, usage);
    }

    public <T extends CustomEnchantment> void handleItemEnchants(@NotNull LivingEntity entity,
                                                                 @NotNull ItemStack itemStack,
                                                                 @NotNull EnchantHolder<T> holder,
                                                                 @NotNull EnchantUsage<T> usage) {
        Map<ItemStack, Map<T, Integer>> enchants = new HashMap<>();
        enchants.put(itemStack, EnchantUtils.getCustomEnchantments(itemStack, holder));

        this.handleFully(entity, enchants, holder::getPriority, usage);
    }

    public <P extends AbstractArrow, T extends ProjectileEnchant<P>> void handleArrowEnchants(@NotNull P projectile,
                                                                                              @NotNull EnchantHolder<T> holder,
                                                                                              @NotNull EnchantUsage<T> usage) {
        ItemStack bow = projectile.getWeapon();
        if (bow == null || !EnchantUtils.isEquipment(bow)) return;

        Map<ItemStack, Map<T, Integer>> enchants = new HashMap<>();
        enchants.put(bow, EnchantUtils.getArrowEnchants(projectile, holder));

        this.handleDirect(enchants, holder::getPriority, usage);
    }

    public <T extends CustomEnchantment> void handleFully(@NotNull LivingEntity entity,
                                                          @NotNull Map<ItemStack, Map<T, Integer>> enchantMap,
                                                          @NotNull Function<T, EnchantPriority> priority,
                                                          @NotNull EnchantUsage<T> usage) {

        this.handleDirect(enchantMap, priority, (item, enchant, level) -> {
            if (!enchant.isAvailableToUse(entity)) return false;
            if (enchant.isOutOfCharges(item)) return false;
            if (enchant.hasComponent(EnchantComponent.PERIODIC) && !enchant.isTriggerTime(entity)) return false;
            if (enchant.hasComponent(EnchantComponent.PROBABILITY) && !enchant.testTriggerChance(level)) return false;
            if (!usage.useEnchant(item, enchant, level)) return false;

            enchant.consumeCharges(item, level);
            return true;
        });
    }

    public <T extends CustomEnchantment> void handleDirect(@NotNull Map<ItemStack, Map<T, Integer>> enchantMap,
                                                           @NotNull Function<T, EnchantPriority> priority,
                                                           @NotNull EnchantUsage<T> handler) {
        enchantMap.forEach((item, enchants) -> enchants.entrySet().stream().sorted(Comparator.comparingInt(entry -> priority.apply(entry.getKey()).ordinal())).forEach(entry -> {
            T enchant = entry.getKey();
            int level = entry.getValue();

            handler.useEnchant(item, enchant, level);
        }));
    }
}

