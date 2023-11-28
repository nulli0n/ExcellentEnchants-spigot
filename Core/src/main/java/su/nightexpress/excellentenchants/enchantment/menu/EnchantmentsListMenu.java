package su.nightexpress.excellentenchants.enchantment.menu;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

import java.util.*;
import java.util.function.Predicate;

public class EnchantmentsListMenu extends ConfigMenu<ExcellentEnchants> implements AutoPaged<ExcellentEnchant> {

    private static final String PATH = "/menu/enchants_list.yml";

    private static final String PLACEHOLDER_CONFLICTS = "%conflicts%";
    private static final String PLACEHOLDER_CHARGES   = "%charges%";
    private static final String PLACEHOLDER_OBTAINING = "%obtaining%";

    private final ItemStack    enchantIcon;
    private final List<String> enchantLoreConflicts;
    private final List<String> enchantLoreCharges;
    private final List<String> enchantLoreObtaining;
    private final int[]        enchantSlots;

    private final NamespacedKey                        keyLevel;
    private final Map<String, Map<Integer, ItemStack>> iconCache;

    public EnchantmentsListMenu(@NotNull ExcellentEnchants plugin) {
        super(plugin, JYML.loadOrExtract(plugin, PATH));
        this.keyLevel = new NamespacedKey(plugin, "list_display_level");
        this.iconCache = new HashMap<>();

        this.enchantIcon = cfg.getItem("Enchantments.Icon");
        this.enchantLoreConflicts = Colorizer.apply(cfg.getStringList("Enchantments.Lore.Conflicts"));
        this.enchantLoreCharges = Colorizer.apply(cfg.getStringList("Enchantments.Lore.Charges"));
        this.enchantLoreObtaining = Colorizer.apply(cfg.getStringList("Enchantments.Lore.Obtaining"));
        this.enchantSlots = cfg.getIntArray("Enchantments.Slots");

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, (viewer, event) -> plugin.runTask(task -> viewer.getPlayer().closeInventory()))
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this));

        this.load();
    }

    @Override
    public void clear() {
        super.clear();
        this.iconCache.clear();
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);
        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    @Override
    public int[] getObjectSlots() {
        return this.enchantSlots;
    }

    @Override
    @NotNull
    public List<ExcellentEnchant> getObjects(@NotNull Player player) {
        return new ArrayList<>(EnchantRegistry.getRegistered().stream()
            .filter(Predicate.not(enchant -> enchant.getDefaults().isHiddenFromList()))
            .sorted(Comparator.comparing(e -> Colorizer.restrip(e.getDisplayName()))).toList());
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull ExcellentEnchant enchant) {
        return this.getEnchantIcon(enchant, 1);
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull ExcellentEnchant enchant) {
        return (viewer, event) -> {
            if (!event.isLeftClick()) return;

            ItemStack itemClick = event.getCurrentItem();
            if (itemClick == null) return;

            int levelHas = PDCUtil.getInt(itemClick, this.keyLevel).orElse(0);
            if (levelHas == 0) levelHas = enchant.getStartLevel();

            if (++levelHas > enchant.getMaxLevel()) levelHas = enchant.getStartLevel();
            itemClick = this.getEnchantIcon(enchant, levelHas);
            PDCUtil.set(itemClick, this.keyLevel, levelHas);

            event.setCurrentItem(itemClick);
        };
    }

    private ItemStack getEnchantIcon(@NotNull ExcellentEnchant enchant, int level) {
        return this.iconCache.computeIfAbsent(enchant.getId(), k -> new HashMap<>()).computeIfAbsent(level, k -> this.buildEnchantIcon(enchant, level));
    }

    @NotNull
    private ItemStack buildEnchantIcon(@NotNull ExcellentEnchant enchant, int level) {
        ItemStack icon = new ItemStack(this.enchantIcon);
        ItemUtil.mapMeta(icon, meta -> {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();

            List<String> conflicts = enchant.getConflicts().isEmpty() ? Collections.emptyList() : new ArrayList<>(this.enchantLoreConflicts);
            List<String> conflictNames = enchant.getConflicts().stream().map(EnchantUtils::getLocalized).filter(Objects::nonNull).toList();
            conflicts = StringUtil.replace(conflicts, Placeholders.ENCHANTMENT_NAME, true, conflictNames);

            List<String> charges = enchant.isChargesEnabled() ? new ArrayList<>(this.enchantLoreCharges) : Collections.emptyList();
            List<String> obtaining = new ArrayList<>(this.enchantLoreObtaining);

            lore = StringUtil.replaceInList(lore, PLACEHOLDER_CONFLICTS, conflicts);
            lore = StringUtil.replaceInList(lore, PLACEHOLDER_CHARGES, charges);
            lore = StringUtil.replaceInList(lore, PLACEHOLDER_OBTAINING, obtaining);
            lore = StringUtil.replace(lore, Placeholders.ENCHANTMENT_DESCRIPTION, true, enchant.getDescription());

            meta.setLore(lore);
            ItemUtil.replace(meta, enchant.getPlaceholders(level).replacer());
        });

        return icon;
    }
}
