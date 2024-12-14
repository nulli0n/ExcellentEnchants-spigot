package su.nightexpress.excellentenchants.enchantment.menu;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Keys;
import su.nightexpress.excellentenchants.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.NormalMenu;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.Replacer;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static su.nightexpress.excellentenchants.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

@SuppressWarnings("UnstableApiUsage")
public class EnchantsMenu extends NormalMenu<EnchantsPlugin> implements ConfigBased, Filled<CustomEnchantment> {

    private static final String FILE_NAME = "enchants.yml";

    private static final String CONFLICTS = "%conflicts%";
    private static final String CHARGES   = "%charges%";

    private NightItem    enchantIcon;
    private String       enchantName;
    private List<String> enchantLoreMain;
    private List<String> enchantLoreConflicts;
    private List<String> enchantLoreCharges;
    private int[]        enchantSlots;

    public EnchantsMenu(@NotNull EnchantsPlugin plugin) {
        super(plugin, MenuType.GENERIC_9X4, BLACK.enclose("Custom Enchantments"));

        this.load(FileConfig.loadOrExtract(plugin, Config.DIR_MENU, FILE_NAME));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    @NotNull
    public MenuFiller<CustomEnchantment> createFiller(@NotNull MenuViewer viewer) {
        var autoFill = MenuFiller.builder(this);

        autoFill.setSlots(this.enchantSlots);
        autoFill.setItems(EnchantRegistry.getRegistered().stream()
            .filter(Predicate.not(CustomEnchantment::isHiddenFromList))
            .sorted(Comparator.comparing(data -> NightMessage.stripAll(data.getDisplayName())))
            .toList()
        );
        autoFill.setItemCreator(enchantmentData -> this.buildEnchantIcon(enchantmentData, 1));
        autoFill.setItemClick(enchantmentData -> (viewer1, event) -> {
            if (!event.isLeftClick()) return;

            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null) return;

            int levelHas = PDCUtil.getInt(currentItem, Keys.keyLevel).orElse(1);
            if (++levelHas > enchantmentData.getDefinition().getMaxLevel()) {
                levelHas = 1;
            }

            ItemStack item = this.buildEnchantIcon(enchantmentData, levelHas).getItemStack();
            PDCUtil.set(item, Keys.keyLevel, levelHas);
            event.setCurrentItem(item);
        });

        return autoFill.build();
    }

    @NotNull
    private NightItem buildEnchantIcon(@NotNull CustomEnchantment enchant, int level) {
        NightItem icon = this.enchantIcon.copy();

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
                charges.add(Replacer.create()
                    .replace(GENERIC_AMOUNT, NumberUtil.format(enchant.getCharges().getMaxAmount(level)))
                    .replace(GENERIC_ITEM, ItemUtil.getItemName(enchant.getCharges().getFuel()))
                    .apply(line)
                );
            }
        }

        icon.setHideComponents(true)
            .setDisplayName(this.enchantName)
            .setLore(this.enchantLoreMain)
            .replacement(replacer -> replacer
                .replace(CHARGES, charges)
                .replace(CONFLICTS, conflicts)
                .replace(enchant.replacePlaceholders(level))
            );
        return icon;
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.enchantIcon = ConfigValue.create("Enchantment.Icon", new NightItem(Material.ENCHANTED_BOOK)).read(config);

        this.enchantName = ConfigValue.create("Enchantment.Name",
            LIGHT_YELLOW.enclose(BOLD.enclose(ENCHANTMENT_NAME + " " + ENCHANTMENT_LEVEL))
        ).read(config);

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
            )).read(config);

        this.enchantLoreConflicts = ConfigValue.create("Enchantment.Lore.Conflicts",
            Lists.newList(
                "",
                LIGHT_RED.enclose(BOLD.enclose("Conflicts:")),
                LIGHT_RED.enclose("✘ ") + LIGHT_GRAY.enclose(GENERIC_NAME)
            )).read(config);

        this.enchantLoreCharges = ConfigValue.create("Enchantment.Lore.Charges",
            Lists.newList(
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Charges: ") + GENERIC_AMOUNT + "⚡" + LIGHT_GRAY.enclose(" (" + WHITE.enclose(GENERIC_ITEM) + ")")))
        ).read(config);

        this.enchantSlots = ConfigValue.create("Enchantment.Slots", IntStream.range(0, 27).toArray()).read(config);


        loader.addDefaultItem(MenuItem.buildNextPage(this, 35));
        loader.addDefaultItem(MenuItem.buildPreviousPage(this, 27));
    }
}

