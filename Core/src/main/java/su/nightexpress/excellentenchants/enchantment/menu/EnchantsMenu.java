package su.nightexpress.excellentenchants.enchantment.menu;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.core.CoreLang;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.AutoFill;
import su.nightexpress.nightcore.menu.api.AutoFilled;
import su.nightexpress.nightcore.menu.impl.ConfigMenu;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static su.nightexpress.excellentenchants.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class EnchantsMenu extends ConfigMenu<EnchantsPlugin> implements AutoFilled<CustomEnchantment> {

    private static final String FILE_NAME = "enchants.yml";

    private static final String CONFLICTS = "%conflicts%";
    private static final String CHARGES   = "%charges%";

    private final NamespacedKey keyLevel;
    private final Map<String, Map<Integer, ItemStack>> iconCache;

    private ItemStack    enchantIcon;
    private String       enchantName;
    private List<String> enchantLoreMain;
    private List<String> enchantLoreConflicts;
    private List<String> enchantLoreCharges;
    private int[]        enchantSlots;

    public EnchantsMenu(@NotNull EnchantsPlugin plugin) {
        super(plugin, FileConfig.loadOrExtract(plugin, Config.DIR_MENU, FILE_NAME));
        this.keyLevel = new NamespacedKey(plugin, "list_display_level");
        this.iconCache = new HashMap<>();

        this.load();
    }

    public void clear() {
        super.clear();
        this.iconCache.clear();
    }

    @Override
    protected void loadAdditional() {
        this.enchantIcon = ConfigValue.create("Enchantment.Icon", new ItemStack(Material.ENCHANTED_BOOK)).read(this.cfg);

        this.enchantName = ConfigValue.create("Enchantment.Name",
            LIGHT_YELLOW.enclose(BOLD.enclose(ENCHANTMENT_NAME + " " + ENCHANTMENT_LEVEL))
        ).read(this.cfg);

        this.enchantLoreMain = ConfigValue.create("Enchantment.Lore.Main",
            Lists.newList(
                ENCHANTMENT_RARITY,
                "",
                ENCHANTMENT_DESCRIPTION_REPLACED,
                DARK_GRAY.enclose("(click to switch levels)"),
                "",
                LIGHT_YELLOW.enclose(BOLD.enclose("Info:")),
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Applies to: ") + ENCHANTMENT_FIT_ITEM_TYPES),
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Levels: ") + ENCHANTMENT_LEVEL_MIN + LIGHT_GRAY.enclose(" - ") + ENCHANTMENT_LEVEL_MAX),
                CHARGES,
                CONFLICTS
            )).read(this.cfg);

        this.enchantLoreConflicts = ConfigValue.create("Enchantment.Lore.Conflicts",
            Lists.newList(
                "",
                LIGHT_RED.enclose(BOLD.enclose("Conflicts:")),
                LIGHT_RED.enclose("✘ ") + LIGHT_GRAY.enclose(GENERIC_NAME)
            )).read(this.cfg);

        this.enchantLoreCharges = ConfigValue.create("Enchantment.Lore.Charges",
            Lists.newList(
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Charges: ") + GENERIC_AMOUNT + "⚡" + LIGHT_GRAY.enclose(" (" + WHITE.enclose(GENERIC_ITEM) + ")")))
        ).read(this.cfg);

        this.enchantSlots = ConfigValue.create("Enchantment.Slots", IntStream.range(0, 27).toArray()).read(this.cfg);
    }

    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Custom Enchantments"), MenuSize.CHEST_36);
    }

    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack nextPageStack = ItemUtil.getSkinHead(SKIN_ARROW_RIGHT);
        ItemUtil.editMeta(nextPageStack, meta -> meta.setDisplayName(CoreLang.EDITOR_ITEM_NEXT_PAGE.getLocalizedName()));

        ItemStack prevPageStack = ItemUtil.getSkinHead(SKIN_ARROW_LEFT);
        ItemUtil.editMeta(prevPageStack, meta -> meta.setDisplayName(CoreLang.EDITOR_ITEM_PREVIOUS_PAGE.getLocalizedName()));

        list.add(new MenuItem(nextPageStack).setSlots(35).setHandler(ItemHandler.forNextPage(this)).setPriority(5));
        list.add(new MenuItem(prevPageStack).setSlots(27).setHandler(ItemHandler.forPreviousPage(this)).setPriority(5));

        return list;
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<CustomEnchantment> autoFill) {
        autoFill.setSlots(this.enchantSlots);
        autoFill.setItems(EnchantRegistry.getRegistered().stream()
            .filter(Predicate.not(CustomEnchantment::isHiddenFromList))
            .sorted(Comparator.comparing(data -> NightMessage.stripAll(data.getDisplayName())))
            .toList()
        );
        autoFill.setItemCreator(enchantmentData -> this.getEnchantIcon(enchantmentData, 1));
        autoFill.setClickAction(enchantmentData -> (viewer1, event) -> {
            if (!event.isLeftClick()) return;

            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null) return;

            int levelHas = PDCUtil.getInt(currentItem, this.keyLevel).orElse(1);
            if (++levelHas > enchantmentData.getDefinition().getMaxLevel()) {
                levelHas = 1;
            }
            currentItem = this.getEnchantIcon(enchantmentData, levelHas);
            PDCUtil.set(currentItem, this.keyLevel, levelHas);
            event.setCurrentItem(currentItem);
        });
    }

    private ItemStack getEnchantIcon(@NotNull CustomEnchantment enchant, int level) {
        return this.iconCache.computeIfAbsent(enchant.getId(), k -> new HashMap<>()).computeIfAbsent(level, k -> this.buildEnchantIcon(enchant, level));
    }

    @NotNull
    private ItemStack buildEnchantIcon(@NotNull CustomEnchantment enchant, int level) {
        ItemStack icon = new ItemStack(this.enchantIcon);

        List<String> conflicts = new ArrayList<>();
        if (enchant.getDefinition().hasConflicts()) {
            for (String line : this.enchantLoreConflicts) {
                if (line.contains(GENERIC_NAME)) {
                    enchant.getDefinition().getConflicts().stream()
                        .map(EnchantUtils::getLocalized).filter(Objects::nonNull)
                        .forEach(conf -> conflicts.add(line.replace(GENERIC_NAME, conf)));
                    continue;
                }
                conflicts.add(line);
            }
        }

        List<String> charges = new ArrayList<>();
        if (enchant.hasCharges()) {
            for (String line : this.enchantLoreCharges) {
                charges.add(line
                    .replace(GENERIC_AMOUNT, NumberUtil.format(enchant.getCharges().getMaxAmount(level)))
                    .replace(GENERIC_ITEM, ItemUtil.getItemName(enchant.getCharges().getFuel()))
                );
            }
        }

        ItemReplacer.create(icon).hideFlags().trimmed()
            .setDisplayName(this.enchantName)
            .setLore(this.enchantLoreMain)
            .replace(CHARGES, charges)
            .replace(CONFLICTS, conflicts)
            .replace(enchant.getPlaceholders(level))
            .writeMeta();
        return icon;
    }
}

