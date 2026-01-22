package su.nightexpress.excellentenchants.enchantment;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.EnchantsUtils;
import su.nightexpress.excellentenchants.api.EnchantDefinition;
import su.nightexpress.excellentenchants.api.EnchantDistribution;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.component.ComponentLoader;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Charges;
import su.nightexpress.excellentenchants.api.item.ItemSet;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.placeholder.PlaceholderList;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public abstract class GameEnchantment implements CustomEnchantment {

    protected final EnchantsPlugin plugin;
    protected final EnchantManager manager;
    protected final Path           file;
    protected final String         id;

    protected final Enchantment         enchantment;
    protected final EnchantDefinition   definition;
    protected final EnchantDistribution distribution;
    protected final boolean             curse;

    protected final Map<EnchantComponent<?>, ComponentLoader<?>> componentLoaders;
    protected final Map<EnchantComponent<?>, Optional<?>>        componentDatas;

    private final NamespacedKey            chargesKey;
    private final PlaceholderList<Integer> placeholders;

    private boolean hiddenFromList;
    private boolean visualEffects;
    private boolean chargeable;

    public GameEnchantment(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager, @NotNull Path file, @NotNull EnchantContext context) {
        this.plugin = plugin;
        this.manager = manager;
        this.file = file;
        this.id = context.id();
        this.enchantment = context.enchantment();
        this.definition = context.definition();
        this.distribution = context.distribution();
        this.curse = context.curse();
        this.componentLoaders = new HashMap<>();
        this.componentDatas = new HashMap<>();

        this.chargesKey = new NamespacedKey(this.plugin, this.getId() + "_charges");
        this.placeholders = EnchantsPlaceholders.forEnchant(this);
    }

    @Override
    public void load() {
        FileConfig.load(this.file).edit(config -> {
            this.loadSettings(config);
            this.loadAdditional(config);
        });
    }

    private void loadSettings(@NotNull FileConfig config) {
        this.hiddenFromList = ConfigValue.create("Settings.Hide_From_List",
            false,
            "Sets whether or not this enchantment will be hidden from Enchants GUI."
        ).read(config);

        this.visualEffects = ConfigValue.create("Settings.VisualEffects.Enabled",
            true,
            "Enables enchantment visual effects (mostly particles)."
        ).read(config);

        if (Config.isChargesEnabled() && !this.isCurse()) {
            this.chargeable = ConfigValue.create("Settings.Charges",
                true,
                "Controls if Charges are enabled for this enchantment."
            ).read(config);
        }

        if (this.isChargeable()) {
            this.addComponent(EnchantComponent.CHARGES, Charges.normal());
        }

        this.componentLoaders.forEach((component, loader) -> {
            var result = loader.load(config);
            this.componentDatas.put(component, Optional.of(result));
        });
    }

    protected abstract void loadAdditional(@NotNull FileConfig config);

    protected <T> void addComponent(@NotNull EnchantComponent<T> type, @NotNull T data) {
        this.componentLoaders.putIfAbsent(type, config -> type.read(config, data));
    }

    public <T> boolean hasComponent(@NotNull EnchantComponent<T> type) {
        return this.componentDatas.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public <T> T getComponent(@NotNull EnchantComponent<T> type) {
        Optional<?> optional = this.componentDatas.get(type);
        return (T) optional.orElseThrow(() -> new IllegalStateException("Enchantment doesn't have the " + type.getName() + " component."));
    }

    @Override
    public boolean testTriggerChance(int level) {
        return this.getComponent(EnchantComponent.PROBABILITY).checkTriggerChance(level);
    }

    public boolean addPotionEffect(@NotNull LivingEntity target, int level) {
        return this.getComponent(EnchantComponent.POTION_EFFECT).addEffect(target, level, this.visualEffects);
    }

    public boolean addPotionEffect(@NotNull Arrow arrow, int level) {
        return this.getComponent(EnchantComponent.POTION_EFFECT).addEffect(arrow, level, this.visualEffects);
    }

    @Override
    public boolean isTriggerTime(@NotNull LivingEntity entity) {
        return this.getComponent(EnchantComponent.PERIODIC).isTriggerTime(entity);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return this.placeholders.replacer(level);
    }

    protected void addPlaceholder(@NotNull String key, @NotNull Function<Integer, String> replacer) {
        this.placeholders.add(key, replacer);
    }

    @NotNull
    @Override
    public String getId() {
        return this.id;
    }

    @Override
    @NotNull
    public Enchantment getBukkitEnchantment() {
        if (this.enchantment == null) throw new IllegalStateException("Backend enchantment is not assigned!");

        return this.enchantment;
    }

    @NotNull
    @Override
    public EnchantDefinition getDefinition() {
        return this.definition;
    }

    @NotNull
    @Override
    public EnchantDistribution getDistribution() {
        return this.distribution;
    }

    @NotNull
    @Override
    public Charges getCharges() {
        return this.getComponent(EnchantComponent.CHARGES);
    }

    @NotNull
    public String getDisplayName() {
        return this.definition.getDisplayName();
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return this.definition.getDescription();
    }

    @Override
    @NotNull
    public List<String> getDescription(int level) {
        List<String> description = new ArrayList<>(this.getDescription());
        description.replaceAll(this.replacePlaceholders(level));
        return description;
    }

    @NotNull
    @Override
    public ItemSet getPrimaryItems() {
        return this.definition.getPrimaryItemSet();
    }

    @NotNull
    @Override
    public ItemSet getSupportedItems() {
        return this.definition.getSupportedItemSet();
    }

    @Override
    public boolean isHiddenFromList() {
        return this.hiddenFromList;
    }

    @Override
    public boolean hasVisualEffects() {
        return this.visualEffects;
    }

    @Override
    public boolean isCurse() {
        return this.curse;
    }

    @Override
    public boolean isChargeable() {
        return this.chargeable;
    }

    @Override
    public boolean isChargesFuel(@NotNull ItemStack item) {
        if (!this.isChargeable()) return false;

        ItemStack fuel = this.getFuel();

        if (Config.CHARGES_FUEL_IGNORE_META.get()) {
            return item.getType() == fuel.getType();
        }
        return item.isSimilar(fuel);
    }

    @Override
    @NotNull
    public ItemStack getFuel() {
        Charges charges = this.getCharges();
        return (charges.isCustomFuelEnabled() ? charges.getCustomFuelItem() : Config.CHARGES_FUEL_ITEM.get()).getItemStack();
    }

    @Override
    public int getMaxCharges(int level) {
        return this.getCharges().getMaxAmount(level);
    }

    @Override
    public boolean isOutOfCharges(@NotNull ItemStack item) {
        return this.isChargeable() && this.getCharges(item) == 0;
    }

    @Override
    public boolean isFullOfCharges(@NotNull ItemStack item) {
        if (!this.isChargeable()) return false;

        int level = EnchantsUtils.getLevel(item, this.getBukkitEnchantment());
        int max = this.getMaxCharges(level);

        return this.getCharges(item) == max;
    }

    @Override
    public int getCharges(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? 0 : this.getCharges(meta);
    }

    @Override
    public int getCharges(@NotNull ItemMeta meta) {
        return this.isChargeable() ? PDCUtil.getInt(meta, this.chargesKey).orElse(0) : -1;
    }

    @Override
    public void setCharges(@NotNull ItemStack item, int level, int amount) {
        if (!this.isChargeable()) return;

        int max = this.getMaxCharges(level);
        int set = Math.min(Math.abs(amount), max);
        PDCUtil.set(item, this.chargesKey, set);
    }

    @Override
    public void restoreCharges(@NotNull ItemStack item, int level) {
        this.setCharges(item, level, this.getMaxCharges(level));
    }

    @Override
    public void fuelCharges(@NotNull ItemStack item, int level) {
        if (!this.isChargeable()) return;

        int recharge = this.getCharges().getRechargeAmount();

        int has = this.getCharges(item);
        int set = has + recharge;

        this.setCharges(item, level, set);
    }

    @Override
    public void consumeCharges(@NotNull ItemStack item, int level) {
        if (!this.isChargeable()) return;

        int charges = this.getCharges(item);
        int consumeAmount = this.getCharges().getConsumeAmount();

        this.setCharges(item, level, charges < consumeAmount ? 0 : Math.max(0, charges - consumeAmount));
    }
}
