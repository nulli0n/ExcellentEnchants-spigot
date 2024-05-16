package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.GenericEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.SimpeListener;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.random.Rnd;

import java.io.File;
import java.util.*;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class TreasureHunterEnchant extends AbstractEnchantmentData implements ChanceData, GenericEnchant, SimpeListener {

    public static final String ID = "treasure_hunter";

    private ChanceSettingsImpl chanceSettings;

    private final Set<LootTables> lootTables;

    public TreasureHunterEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to get more items in loot chests.");
        this.setMaxLevel(4);
        this.setRarity(Rarity.RARE);

        this.lootTables = new HashSet<>();
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(2.5, 2.5, 1, 100));

        boolean isWhitelist = ConfigValue.create("Settings.LootTables.Whitelist",
            false,
            "When 'true', uses only loot tables listed below.",
            "When 'false', uses ALL loot tables except ones listed below.",
            "[Default is false]"
        ).read(config);

        Set<LootTables> tables = ConfigValue.forSet("Settings.LootTables.List",
            id -> StringUtil.getEnum(id, LootTables.class).orElse(null),
            (cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()),
            Set.of(
                LootTables.ENDER_DRAGON,
                LootTables.END_CITY_TREASURE
            ),
            "List of loot tables that are added or excluded (depends on Whitelist setting) for this enchantment.",
            "Available loot table names: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/loot/LootTables.html"
        ).read(config);

        if (isWhitelist) {
            this.lootTables.addAll(tables);
        }
        else {
            this.lootTables.addAll(Arrays.asList(LootTables.values()));
            this.lootTables.removeAll(tables);
        }
    }

    @Override
    public void clear() {
        this.lootTables.clear();
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.ARMOR_HEAD;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLootExplore(LootGenerateEvent event) {
        if (this.lootTables.isEmpty()) return;

        if (!(event.getEntity() instanceof Player player)) return;
        if (!this.isAvailableToUse(player)) return;

        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null || helmet.getType().isAir()) return;

        int level = EnchantUtils.getLevel(helmet, this.getEnchantment());
        if (level < 1) return;

        if (!this.checkTriggerChance(level)) return;

        InventoryHolder holder = event.getInventoryHolder();
        if (holder == null) return;

        Inventory inventory = holder.getInventory();

        LootTable lootTable = Rnd.get(this.lootTables).getLootTable();

        Collection<ItemStack> items = lootTable.populateLoot(Rnd.RANDOM, event.getLootContext());
        items.forEach(inventory::addItem);

        this.consumeCharges(helmet, level);
    }
}
