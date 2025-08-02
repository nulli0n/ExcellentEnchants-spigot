package su.nightexpress.excellentenchants.manager.menu;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantRegistry;
import su.nightexpress.excellentenchants.api.config.ConfigBridge;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.config.Keys;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.NormalMenu;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.Replacer;
import su.nightexpress.nightcore.util.text.night.NightMessage;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static su.nightexpress.excellentenchants.api.EnchantsPlaceholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

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
        super(plugin, MenuType.GENERIC_9X4, BLACK.wrap("Custom Enchantments"));

        this.load(FileConfig.loadOrExtract(plugin, ConfigBridge.DIR_MENU, FILE_NAME));
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
            .sorted(Comparator.comparing(data -> NightMessage.stripTags(data.getDisplayName())))
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
        List<String> conflicts = new ArrayList<>();
        if (enchant.getDefinition().hasConflicts()) {
            for (String line : this.enchantLoreConflicts) {
                if (line.contains(GENERIC_NAME)) {
                    enchant.getDefinition().getExclusiveSet().stream()
                        .map(BukkitThing::getEnchantment).filter(Objects::nonNull).map(LangUtil::getSerializedName)
                        .forEach(conf -> conflicts.add(line.replace(GENERIC_NAME, conf)));
                    continue;
                }
                conflicts.add(line);
            }
        }

        List<String> charges = Replacer.create()
            .replace(GENERIC_AMOUNT, () -> NumberUtil.format(enchant.getCharges().getMaxAmount(level)))
            .replace(GENERIC_ITEM, () -> ItemUtil.getNameSerialized(enchant.getFuel()))
            .apply(enchant.isChargeable() ? this.enchantLoreCharges : Collections.emptyList());

        return this.enchantIcon.copy().hideAllComponents()
            .setDisplayName(this.enchantName)
            .setLore(this.enchantLoreMain)
            .replacement(replacer -> replacer
                .replace(CHARGES, charges)
                .replace(CONFLICTS, conflicts)
                .replace(enchant.replacePlaceholders(level))
            );
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.enchantIcon = ConfigValue.create("Enchantment.Icon", new NightItem(Material.ENCHANTED_BOOK)).read(config);

        this.enchantName = ConfigValue.create("Enchantment.Name",
            LIGHT_YELLOW.wrap(BOLD.wrap(ENCHANTMENT_NAME + " " + ENCHANTMENT_LEVEL))
        ).read(config);

        this.enchantLoreMain = ConfigValue.create("Enchantment.Lore.Main",
            Lists.newList(
                ENCHANTMENT_DESCRIPTION_REPLACED,
                DARK_GRAY.wrap("(click to switch levels)"),
                EMPTY_IF_ABOVE,
                LIGHT_YELLOW.wrap(BOLD.wrap("Info:")),
                LIGHT_YELLOW.wrap("▪ " + LIGHT_GRAY.wrap("Applies to: ") + ENCHANTMENT_FIT_ITEM_TYPES),
                LIGHT_YELLOW.wrap("▪ " + LIGHT_GRAY.wrap("Levels: ") + ENCHANTMENT_LEVEL_MIN + LIGHT_GRAY.wrap(" - ") + ENCHANTMENT_LEVEL_MAX),
                EMPTY_IF_BELOW,
                CHARGES,
                EMPTY_IF_BELOW,
                CONFLICTS
            )).read(config);

        this.enchantLoreConflicts = ConfigValue.create("Enchantment.Lore.Conflicts",
            Lists.newList(
                LIGHT_RED.wrap(BOLD.wrap("Conflicts:")),
                LIGHT_RED.wrap("✘ ") + LIGHT_GRAY.wrap(GENERIC_NAME)
            )).read(config);

        this.enchantLoreCharges = ConfigValue.create("Enchantment.Lore.Charges",
            Lists.newList(
                LIGHT_YELLOW.wrap("▪ " + LIGHT_GRAY.wrap("Charges: ") + GENERIC_AMOUNT + "⚡" + LIGHT_GRAY.wrap(" (" + WHITE.wrap(GENERIC_ITEM) + ")"))
            )).read(config);

        this.enchantSlots = ConfigValue.create("Enchantment.Slots", IntStream.range(0, 27).toArray()).read(config);


        loader.addDefaultItem(MenuItem.buildNextPage(this, 35));
        loader.addDefaultItem(MenuItem.buildPreviousPage(this, 27));
    }
}

