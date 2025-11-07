package su.nightexpress.excellentenchants.manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.*;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.type.ProjectileEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantData;
import su.nightexpress.excellentenchants.enchantment.EnchantDataRegistry;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.EnchantHolder;
import su.nightexpress.excellentenchants.enchantment.EnchantRegistry;
import su.nightexpress.excellentenchants.manager.block.TickedBlock;
import su.nightexpress.excellentenchants.manager.damage.Explosion;
import su.nightexpress.excellentenchants.manager.listener.AnvilListener;
import su.nightexpress.excellentenchants.manager.listener.EnchantListener;
import su.nightexpress.excellentenchants.manager.listener.GenericListener;
import su.nightexpress.excellentenchants.manager.listener.SlotListener;
import su.nightexpress.excellentenchants.manager.menu.EnchantsMenu;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.EntityUtil;
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

        if (Version.isPaper()) {
            this.addListener(new SlotListener(this.plugin, this));
        }

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
        EnchantDataRegistry.getMap().forEach(this::loadEnchant);
        this.plugin.info("Loaded " + EnchantRegistry.getRegistered().size() + " enchantments.");
    }

    private boolean loadEnchant(@NotNull String id, @NotNull EnchantData data) {
        CustomEnchantment registered = EnchantRegistry.getById(id);
        if (registered != null) {
            return registered.load();
        }

        File file = new File(plugin.getDataFolder() + EnchantFiles.DIR_ENCHANTS, id + ".yml");
        if (!file.exists()) {
            this.plugin.error("No config present for the '" + id + "' enchantment.");
            return false;
        }

        CustomEnchantment enchantment = data.getProvider().create(this.plugin, file, data);

        Enchantment bukkitEnchant = BukkitThing.getByKey(RegistryType.ENCHANTMENT, EnchantKeys.create(id));

        if (bukkitEnchant == null) {
            this.plugin.error("No registered bukkit enchant found for '" + id + "'.");
            return false;
        }

        enchantment.load();
        enchantment.onRegister(bukkitEnchant);
        EnchantRegistry.registerEnchant(enchantment);
        return true;
    }

    public void updateCache(@NotNull LivingEntity entity, @NotNull EquipmentSlot slot, @Nullable ItemStack itemStack) {
        EnchantRegistry.getHolders().forEach(holder -> {
            if (!holder.isCacheable()) return;

            if (itemStack == null || itemStack.getType().isAir() || !EnchantUtils.isEquipment(itemStack)) {
                holder.removeCache(entity, slot);
                return;
            }

            Map<CustomEnchantment, Integer> allEnchants = EnchantUtils.getCustomEnchantments(itemStack);
            holder.updateCache(entity, slot, itemStack, allEnchants);
        });
    }

    public void clearCache(@NotNull LivingEntity entity) {
        EnchantRegistry.getHolders().forEach(holder -> {
            if (!holder.isCacheable()) return;

            holder.clearCache(entity);
        });
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
        this.getPassiveEnchantEntities().forEach(entity -> this.handleCached(entity, EntityUtil.EQUIPMENT_SLOTS, EnchantRegistry.PASSIVE, (item, enchant, level) -> enchant.onTrigger(entity, item, level)));
    }

    @NotNull
    private Set<LivingEntity> getPassiveEnchantEntities() {
        Set<LivingEntity> entities = new HashSet<>(Players.getOnline());

        if (Config.PASSIVE_ENCHANTS_ALLOW_FOR_MOBS.get()) {
            this.plugin.getServer().getWorlds().forEach(world -> entities.addAll(world.getLivingEntities()));
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

    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFF_HAND};

    public <T extends CustomEnchantment> void handleArmorEnchants(@NotNull LivingEntity entity, @NotNull EnchantHolder<T> holder, @NotNull EnchantUsage<T> usage) {
        this.handleCached(entity, ARMOR_SLOTS, holder, usage);
    }

    public <T extends CustomEnchantment> void handleInventoryEnchants(@NotNull Player player, @NotNull EnchantHolder<T> holder, @NotNull EnchantUsage<T> usage) {
        this.handleFully(player, EnchantUtils.getAll(player, holder), holder::getPriority, usage);
    }

    public <T extends CustomEnchantment> void handleItemEnchants(@NotNull LivingEntity entity,
                                                                 @NotNull EquipmentSlot slot,
                                                                 @NotNull EnchantHolder<T> holder,
                                                                 @NotNull EnchantUsage<T> usage) {
        this.handleCached(entity, new EquipmentSlot[]{slot}, holder, usage);
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

    public <T extends CustomEnchantment> void handleCached(@NotNull LivingEntity entity,
                                                           @NotNull EquipmentSlot[] slots,
                                                           @NotNull EnchantHolder<T> holder,
                                                           @NotNull EnchantUsage<T> usage) {

        Map<ItemStack, Map<T, Integer>> enchantMap = new HashMap<>();
        boolean noCache = entity.getType() != EntityType.PLAYER || !holder.isCacheable() || Version.isSpigot();

        for (EquipmentSlot slot : slots) {
            if (noCache || slot == EquipmentSlot.HAND) {
                ItemStack itemStack = EntityUtil.getItemInSlot(entity, slot);
                if (itemStack == null || itemStack.getType().isAir() || !EnchantUtils.isEquipment(itemStack)) continue;

                enchantMap.put(itemStack, EnchantUtils.getCustomEnchantments(itemStack, holder));
            }
            else {
                EnchantedItem<T> enchantedItem = holder.getCached(entity, slot);
                if (enchantedItem == null) continue;

                enchantMap.put(enchantedItem.getItemStack(), enchantedItem.getEnchants());
            }
        }

        this.handleFully(entity, enchantMap, holder::getPriority, usage);
    }

    public <T extends CustomEnchantment> void handleFully(@NotNull LivingEntity entity,
                                                          @NotNull Map<ItemStack, Map<T, Integer>> enchantMap,
                                                          @NotNull Function<T, EnchantPriority> priority,
                                                          @NotNull EnchantUsage<T> usage) {

        this.handleDirect(enchantMap, priority, (itemStack, enchant, level) -> {
            if (!enchant.isAvailableToUse(entity)) return false;
            if (enchant.isOutOfCharges(itemStack)) return false;
            if (enchant.hasComponent(EnchantComponent.PERIODIC) && !enchant.isTriggerTime(entity)) return false;
            if (enchant.hasComponent(EnchantComponent.PROBABILITY) && !enchant.testTriggerChance(level)) return false;
            if (!usage.useEnchant(itemStack, enchant, level)) return false;

            enchant.consumeCharges(itemStack, level); // TODO Re-add equipment for mobs to apply changes
            return true;
        });
    }

    public <T extends CustomEnchantment> void handleDirect(@NotNull Map<ItemStack, Map<T, Integer>> enchantMap,
                                                           @NotNull Function<T, EnchantPriority> priority,
                                                           @NotNull EnchantUsage<T> usage) {
        enchantMap.forEach((itemStack, enchants) ->
              enchants.entrySet().stream().sorted(Comparator.comparingInt(entry ->
                    priority.apply(entry.getKey()).ordinal())).forEach(entry -> {
            T enchant = entry.getKey();
            int level = entry.getValue();

            usage.useEnchant(itemStack, enchant, level);
        }));
    }
}
