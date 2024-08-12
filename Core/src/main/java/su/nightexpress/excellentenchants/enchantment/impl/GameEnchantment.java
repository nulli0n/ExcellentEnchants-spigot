package su.nightexpress.excellentenchants.enchantment.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.EnchantMeta;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.util.EnchantPlaceholders;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.AbstractFileData;
import su.nightexpress.nightcore.manager.SimpeListener;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public abstract class GameEnchantment extends AbstractFileData<EnchantsPlugin> implements CustomEnchantment {

    protected final EnchantMeta         meta;
    protected final EnchantDefinition   definition;
    protected final EnchantDistribution distribution;
    protected final EnchantCharges      charges;
    private final   NamespacedKey       chargesKey;
    private final   EnchantPlaceholders placeholders;
    private final   String              logPrefix;

    private Enchantment enchantment;
    private boolean hiddenFromList;
    private boolean visualEffects;

    public GameEnchantment(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantDefinition definition) {
        this(plugin, file, definition, EnchantDistribution.defaults());
    }

    public GameEnchantment(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantDefinition definition, @NotNull EnchantDistribution distribution) {
        super(plugin, file);

        this.meta = new EnchantMeta();
        this.definition = definition;
        this.distribution = distribution;
        this.charges = new EnchantCharges();
        this.chargesKey = new NamespacedKey(plugin, this.getId() + "_charges");
        this.placeholders = Placeholders.forEnchant(this);
        this.logPrefix = "[" + this.getId() + "] ";
    }

    public void registerListeners() {
        if (this instanceof SimpeListener listener) {
            this.plugin.getPluginManager().registerEvents(listener, plugin);
        }
    }

    @Override
    public boolean checkServerRequirements() {
        return true;
    }

    @Override
    protected boolean onLoad(@NotNull FileConfig config) {
        this.loadMain(config);
        this.loadSettings(config);
        this.loadAdditional(config);

        this.registerListeners();
        return true;
    }

    private void loadMain(@NotNull FileConfig config) {
        this.definition.load(this, config);
        this.distribution.load(config);

        if (Config.isChargesEnabled() && !this.isCurse()) {
            this.charges.load(config);
        }
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
    }

    protected abstract void loadAdditional(@NotNull FileConfig config);

    @Override
    protected void onSave(@NotNull FileConfig config) {

    }

    public void onRegister(@NotNull Enchantment enchantment) {
        if (this.enchantment != null) return;

        this.enchantment = enchantment;
    }

    protected void info(@NotNull String text) {
        this.plugin.info(this.logPrefix + text);
    }

    protected void warn(@NotNull String text) {
        this.plugin.warn(this.logPrefix + text);
    }

    protected void error(@NotNull String text) {
        this.plugin.error(this.logPrefix + text);
    }

    @NotNull
    public PlaceholderMap getPlaceholders(int level) {
        return this.placeholders.toMap(level);
    }

    public void addPlaceholder(@NotNull String key, @NotNull Function<Integer, String> replacer) {
        this.placeholders.add(key, replacer);
    }

    @Override
    @NotNull
    public Enchantment getBukkitEnchantment() {
        return this.enchantment;
    }

    @Override
    @NotNull
    public EnchantMeta getMeta() {
        return this.meta;
    }

    @NotNull
    @Override
    public EnchantDefinition getDefinition() {
        return definition;
    }

    @NotNull
    @Override
    public EnchantDistribution getDistribution() {
        return distribution;
    }

    @NotNull
    @Override
    public EnchantCharges getCharges() {
        return this.charges;
    }

    @Override
    public boolean isAvailableToUse(@NotNull World world) {
        Set<String> disabled = Config.ENCHANTMENTS_DISABLED_IN_WORLDS.get().getOrDefault(world.getName().toLowerCase(), Collections.emptySet());
        return disabled.isEmpty() || (!disabled.contains(this.getId()) && !disabled.contains(Placeholders.WILDCARD));
    }

    @Override
    public boolean isAvailableToUse(@NotNull LivingEntity entity) {
        return this.isAvailableToUse(entity.getWorld());
    }

    @NotNull
    public String getDisplayName() {
        return this.definition.getDisplayName();
    }

    @Override
    @NotNull
    public String getFormattedName() {
        return this.isCurse() ? this.getDisplayName() : this.getPlaceholders(1).replacer().apply(this.definition.getRarity().getNameFormat());
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return this.definition.getDescription();
    }

    @Override
    @NotNull
    public List<String> getDescription(int level) {
        return this.getDescription(level, 0);
    }

    @Override
    @NotNull
    public List<String> getDescription(int level, int charges) {
        List<String> description = new ArrayList<>(this.getDescription());

        String lineFormat = Config.ENCHANTMENTS_DISPLAY_DESCRIPTION_FORMAT.get();

        description.replaceAll(line -> {
            line = lineFormat.replace(Placeholders.GENERIC_DESCRIPTION, line);
            line = EnchantUtils.replaceComponents(this, line, level, charges);
            line = this.getPlaceholders(level).replacer().apply(line);
            return line;
        });
        return description;
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
        return false;
    }

    @Override
    public boolean hasCharges() {
        return this.charges != null && this.charges.isEnabled();
    }

    @Override
    public boolean isChargesFuel(@NotNull ItemStack item) {
        if (!this.hasCharges()) return false;

        if (Config.ENCHANTMENTS_CHARGES_COMPARE_TYPE_ONLY.get()) {
            return item.getType() == this.charges.getFuel().getType();
        }
        return item.isSimilar(this.charges.getFuel());
    }

    @Override
    public boolean isOutOfCharges(@NotNull ItemStack item) {
        return this.hasCharges() && this.getCharges(item) == 0;
    }

    @Override
    public boolean isFullOfCharges(@NotNull ItemStack item) {
        if (!this.hasCharges()) return false;

        int level = EnchantUtils.getLevel(item, this.getBukkitEnchantment());
        int max = this.charges.getMaxAmount(level);

        return this.getCharges(item) == max;
    }

    @Override
    public int getCharges(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? 0 : this.getCharges(meta);
    }

    @Override
    public int getCharges(@NotNull ItemMeta meta) {
        return this.hasCharges() ? PDCUtil.getInt(meta, this.chargesKey).orElse(0) : -1;
    }

    @Override
    public void setCharges(@NotNull ItemStack item, int level, int amount) {
        if (!this.hasCharges()) return;

        int max = this.charges.getMaxAmount(level);
        int set = Math.min(Math.abs(amount), max);
        PDCUtil.set(item, this.chargesKey, set);
    }

    @Override
    public void restoreCharges(@NotNull ItemStack item, int level) {
        this.setCharges(item, level, Integer.MAX_VALUE);
    }

    @Override
    public void fuelCharges(@NotNull ItemStack item, int level) {
        if (!this.hasCharges()) return;

        int recharge = this.charges.getRechargeAmount(level);

        int has = this.getCharges(item);
        int set = has + recharge;

        this.setCharges(item, level, set);
    }

    @Override
    public void consumeChargesNoUpdate(@NotNull ItemStack item, int level) {
        if (!this.hasCharges()) return;

        int charges = this.getCharges(item);
        int consumeAmount = this.charges.getConsumeAmount(level);

        this.setCharges(item, level, charges < consumeAmount ? 0 : Math.max(0, charges - consumeAmount));
    }

    @Override
    public void consumeCharges(@NotNull ItemStack item, int level) {
        if (!this.hasCharges()) return;

        this.consumeChargesNoUpdate(item, level);
    }
}
