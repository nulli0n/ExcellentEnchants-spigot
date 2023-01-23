package su.nightexpress.excellentenchants.enchantment.menu;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantRegister;

import java.util.*;

public class EnchantmentsListMenu extends AbstractMenuAuto<ExcellentEnchants, ExcellentEnchant> {

    private static final String PATH = "/menu/enchants_list.yml";

    private static final String PLACEHOLDER_CONFLICTS = "%conflicts%";
    private static final String PLACEHOLDER_CHARGES = "%charges%";
    private static final String PLACEHOLDER_OBTAINING = "%obtaining%";

    private final ItemStack enchantIcon;
    private final List<String> enchantLoreConflicts;
    private final List<String> enchantLoreCharges;
    private final List<String> enchantLoreObtaining;
    private final int[]     enchantSlots;

    private final NamespacedKey                        keyLevel;
    private final Map<String, Map<Integer, ItemStack>> iconCache;

    public EnchantmentsListMenu(@NotNull ExcellentEnchants plugin) {
        super(plugin, JYML.loadOrExtract(plugin, PATH), "");
        this.keyLevel = new NamespacedKey(plugin, "list_display_level");
        this.iconCache = new HashMap<>();

        this.enchantIcon = cfg.getItem("Enchantments.Icon");
        this.enchantLoreConflicts = StringUtil.color(cfg.getStringList("Enchantments.Lore.Conflicts"));
        this.enchantLoreCharges = StringUtil.color(cfg.getStringList("Enchantments.Lore.Charges"));
        this.enchantLoreObtaining = StringUtil.color(cfg.getStringList("Enchantments.Lore.Obtaining"));
        this.enchantSlots = cfg.getIntArray("Enchantments.Slots");

        MenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + sId);

            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    public void clear() {
        super.clear();
        this.iconCache.clear();
    }

    @Override
    protected int[] getObjectSlots() {
        return this.enchantSlots;
    }

    @Override
    @NotNull
    protected List<ExcellentEnchant> getObjects(@NotNull Player player) {
        return new ArrayList<>(EnchantRegister.ENCHANT_REGISTRY.values().stream()
            .sorted(Comparator.comparing(ExcellentEnchant::getName)).toList());
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull ExcellentEnchant enchant) {
        return this.getEnchantIcon(enchant, 1);
    }

    @Override
    @NotNull
    protected MenuClick getObjectClick(@NotNull Player player, @NotNull ExcellentEnchant enchant) {
        return (player1, type, e) -> {
            if (!e.isLeftClick()) return;

            ItemStack itemClick = e.getCurrentItem();
            if (itemClick == null) return;

            int levelHas = PDCUtil.getIntData(itemClick, this.keyLevel);
            if (levelHas == 0) levelHas = enchant.getStartLevel();

            if (++levelHas > enchant.getMaxLevel()) levelHas = enchant.getStartLevel();
            itemClick = this.getEnchantIcon(enchant, levelHas);
            PDCUtil.setData(itemClick, this.keyLevel, levelHas);

            e.setCurrentItem(itemClick);
        };
    }

    private ItemStack getEnchantIcon(@NotNull ExcellentEnchant enchant, int level) {
        return this.iconCache.computeIfAbsent(enchant.getId(), k -> new HashMap<>()).computeIfAbsent(level, k -> this.buildEnchantIcon(enchant, level));
    }

    @NotNull
    private ItemStack buildEnchantIcon(@NotNull ExcellentEnchant enchant, int level) {
        ItemStack icon = new ItemStack(this.enchantIcon);
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) return icon;

        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();

        List<String> conflicts = enchant.getConflicts().isEmpty() ? Collections.emptyList() : new ArrayList<>(this.enchantLoreConflicts);
        List<String> conflictNames = enchant.getConflicts().stream().map(key -> Enchantment.getByKey(NamespacedKey.minecraft(key)))
            .filter(Objects::nonNull).map(LangManager::getEnchantment).toList();
        conflicts = StringUtil.replace(conflicts, Placeholders.ENCHANTMENT_NAME, true, conflictNames);

        List<String> charges = enchant.isChargesEnabled() ? new ArrayList<>(this.enchantLoreCharges) : Collections.emptyList();
        List<String> obtaining = new ArrayList<>(this.enchantLoreObtaining);

        lore = StringUtil.replace(lore, PLACEHOLDER_CONFLICTS, false, conflicts);
        lore = StringUtil.replace(lore, PLACEHOLDER_CHARGES, false, charges);
        lore = StringUtil.replace(lore, PLACEHOLDER_OBTAINING, false, obtaining);

        meta.setLore(lore);
        icon.setItemMeta(meta);

        ItemUtil.replace(icon, enchant.replacePlaceholders(level));
        return icon;
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
