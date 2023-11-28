package su.nightexpress.excellentenchants.enchantment.menu;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemReplacer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static su.nexmedia.engine.utils.Colors2.*;
import static su.nightexpress.excellentenchants.Placeholders.*;

public class EnchantmentsListMenu extends ConfigMenu<ExcellentEnchants> implements AutoPaged<ExcellentEnchant> {

    private static final String FILE = "enchants.yml";

    private static final String PLACEHOLDER_CONFLICTS = "%conflicts%";
    private static final String PLACEHOLDER_CHARGES   = "%charges%";
    private static final String PLACEHOLDER_OBTAINING = "%obtaining%";

    private final NamespacedKey                        keyLevel;
    private final Map<String, Map<Integer, ItemStack>> iconCache;

    private String       enchantName;
    private List<String> enchantLoreMain;
    private List<String> enchantLoreConflicts;
    private List<String> enchantLoreCharges;
    private List<String> enchantLoreObtaining;
    private int[]        enchantSlots;

    public EnchantmentsListMenu(@NotNull ExcellentEnchants plugin) {
        super(plugin, new JYML(plugin.getDataFolder() + Config.DIR_MENU, FILE));
        this.keyLevel = new NamespacedKey(plugin, "list_display_level");
        this.iconCache = new HashMap<>();

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, ClickHandler.forClose(this))
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this));

        this.load();
    }

    @Override
    public void clear() {
        super.clear();
        this.iconCache.clear();
    }

    // ----------

    @Override
    public boolean isCodeCreation() {
        return true;
    }

    @Override
    protected void loadAdditional() {
        this.enchantName = JOption.create("Enchantment.Name", ENCHANTMENT_NAME_FORMATTED).read(cfg);

        this.enchantLoreMain = JOption.create("Enchantment.Lore.Main",
            Arrays.asList(
                ENCHANTMENT_DESCRIPTION,
                DARK_GRAY + "(click to switch level)",
                "",
                LIGHT_YELLOW + BOLD + "Info:",
                LIGHT_YELLOW + "▪ " + LIGHT_GRAY + "Tier: " + LIGHT_YELLOW + ENCHANTMENT_TIER,
                LIGHT_YELLOW + "▪ " + LIGHT_GRAY + "Applies to: " + LIGHT_YELLOW + ENCHANTMENT_FIT_ITEM_TYPES,
                LIGHT_YELLOW + "▪ " + LIGHT_GRAY + "Levels: " + LIGHT_YELLOW + ENCHANTMENT_LEVEL_MIN + GRAY + " - " + LIGHT_YELLOW + ENCHANTMENT_LEVEL_MAX,
                PLACEHOLDER_CHARGES,
                PLACEHOLDER_CONFLICTS,
                PLACEHOLDER_OBTAINING
            )).read(cfg);

        this.enchantLoreConflicts = JOption.create("Enchantment.Lore.Conflicts",
            Arrays.asList(
                "",
                LIGHT_RED + BOLD + "Conflicts:",
                LIGHT_RED + "✘ " + LIGHT_GRAY + GENERIC_NAME
            )).read(cfg);

        this.enchantLoreCharges = JOption.create("Enchantment.Lore.Charges",
            Arrays.asList(
                LIGHT_YELLOW + "▪ " + LIGHT_GRAY + "Charges: " + LIGHT_YELLOW + ENCHANTMENT_CHARGES_MAX_AMOUNT + "⚡" + LIGHT_GRAY + " (" + WHITE + ENCHANTMENT_CHARGES_FUEL_ITEM + LIGHT_GRAY + ")"
            )).read(cfg);

        this.enchantLoreObtaining = JOption.create("Enchantment.Lore.Obtaining",
            Arrays.asList(
                "",
                LIGHT_GREEN + BOLD + "Obtaining:",
                LIGHT_GREEN + "✔ " + LIGHT_GRAY + GENERIC_TYPE
            )).read(cfg);

        this.enchantSlots = new JOption<int[]>("Enchantment.Slots",
            (cfg, path, def) -> cfg.getIntArray(path),
            () -> IntStream.range(0, 27).toArray()
        ).setWriter(JYML::setIntArray).read(cfg);
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(DARK_GRAY + BOLD + "Custom Enchants", 36, InventoryType.CHEST);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack nextPageStack = ItemUtil.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMyY2E2NjA1NmI3Mjg2M2U5OGY3ZjMyYmQ3ZDk0YzdhMGQ3OTZhZjY5MWM5YWMzYTkxMzYzMzEzNTIyODhmOSJ9fX0=");
        ItemUtil.mapMeta(nextPageStack, meta -> {
            meta.setDisplayName(WHITE + "Next Page" + LIGHT_GRAY + " (→)");
        });

        ItemStack prevPageStack = ItemUtil.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY5NzFkZDg4MWRiYWY0ZmQ2YmNhYTkzNjE0NDkzYzYxMmY4Njk2NDFlZDU5ZDFjOTM2M2EzNjY2YTVmYTYifX19");
        ItemUtil.mapMeta(prevPageStack, meta -> {
            meta.setDisplayName(LIGHT_GRAY + "(←) " + WHITE + "Previous Page");
        });

        list.add(new MenuItem(nextPageStack).setSlots(35).setType(MenuItemType.PAGE_NEXT).setPriority(5));
        list.add(new MenuItem(prevPageStack).setSlots(27).setType(MenuItemType.PAGE_PREVIOUS).setPriority(5));

        return list;
    }

    // -----------

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
        ItemStack icon = new ItemStack(Material.ENCHANTED_BOOK);

        List<String> conflicts = new ArrayList<>();
        if (enchant.hasConflicts()) {
            for (String line : this.enchantLoreConflicts) {
                if (line.contains(GENERIC_NAME)) {
                    enchant.getConflicts().stream().map(EnchantUtils::getLocalized).filter(Objects::nonNull).forEach(conf -> {
                        conflicts.add(line.replace(GENERIC_NAME, conf));
                    });
                }
                else conflicts.add(line);
            }
        }

        List<String> obtaining = new ArrayList<>();
        for (String line : this.enchantLoreObtaining) {
            if (line.contains(GENERIC_TYPE)) {
                for (ObtainType obtainType : ObtainType.values()) {
                    if (enchant.isObtainable(obtainType)) {
                        obtaining.add(line.replace(GENERIC_TYPE, plugin.getLangManager().getEnum(obtainType)));
                    }
                }
            }
            else obtaining.add(line);
        }

        ItemReplacer.create(icon).hideFlags().trimmed()
            .setDisplayName(this.enchantName)
            .setLore(this.enchantLoreMain)
            .replaceLoreExact(PLACEHOLDER_CHARGES, enchant.isChargesEnabled() ? new ArrayList<>(this.enchantLoreCharges) : Collections.emptyList())
            .replaceLoreExact(PLACEHOLDER_CONFLICTS, conflicts)
            .replaceLoreExact(PLACEHOLDER_OBTAINING, obtaining)
            .replaceLoreExact(ENCHANTMENT_DESCRIPTION, enchant.formatDescription())
            .replace(enchant.getPlaceholders(level))
            .replace(Colorizer::apply)
            .writeMeta();

        return icon;
    }
}
