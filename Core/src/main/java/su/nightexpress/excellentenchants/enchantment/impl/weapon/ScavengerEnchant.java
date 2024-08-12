package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.random.Rnd;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;

public class ScavengerEnchant extends GameEnchantment implements ChanceMeta, DeathEnchant {

    public static final String ID = "scavenger";

    private final Set<CreatureSpawnEvent.SpawnReason> ignoredSpawnReasons;
    private final Set<EntityType> ignoredEntities;
    private final Set<LootTables> lootTables;

    public ScavengerEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.treasure(TradeType.JUNGLE_COMMON));

        this.ignoredSpawnReasons = new HashSet<>();
        this.ignoredEntities = new HashSet<>();
        this.lootTables = new HashSet<>();
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            ENCHANTMENT_CHANCE + "% chance to get additional loot from mobs.",
            EnchantRarity.LEGENDARY,
            4,
            ItemCategories.WEAPON
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(1, 0.5, 1)));

        this.ignoredSpawnReasons.addAll(ConfigValue.forSet("Settings.Ignored_SpawnReasons",
            id -> StringUtil.getEnum(id, CreatureSpawnEvent.SpawnReason.class).orElse(null),
            (cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()),
            Set.of(
                CreatureSpawnEvent.SpawnReason.SPAWNER,
                CreatureSpawnEvent.SpawnReason.SPAWNER_EGG,
                CreatureSpawnEvent.SpawnReason.DISPENSE_EGG
            ),
            "Mobs spawned by specified souces will be 'immune' to this enchantment's effect.",
            "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html"
        ).read(config));

        this.ignoredEntities.addAll(ConfigValue.forSet("Settings.Ignored_Entities",
            id -> StringUtil.getEnum(id, EntityType.class).orElse(null),
            (cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()),
            Set.of(EntityType.COD, EntityType.PUFFERFISH, EntityType.SQUID, EntityType.ARMOR_STAND, EntityType.SALMON),
            "A list of entity types excluded from this enchantment's effect.",
            "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html"
        ).read(config));

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
                LootTables.END_CITY_TREASURE,
                LootTables.PILLAGER_OUTPOST,
                LootTables.NETHER_BRIDGE,
                LootTables.BASTION_BRIDGE
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

    @Override
    public boolean onKill(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull Player killer, @NotNull ItemStack weapon, int level) {
        if (this.lootTables.isEmpty()) return false;
        if (this.ignoredEntities.contains(entity.getType())) return false;

        CreatureSpawnEvent.SpawnReason spawnReason = EnchantUtils.getSpawnReason(entity);
        if (spawnReason != null && this.ignoredSpawnReasons.contains(spawnReason)) return false;

        if (!this.checkTriggerChance(level)) return false;

        LootTable lootTable = Rnd.get(this.lootTables).getLootTable();
        LootContext.Builder builder = new LootContext.Builder(entity.getLocation());
        builder.killer(killer);
        builder.lootedEntity(entity);
        LootContext context = builder.build();

        Collection<ItemStack> items = lootTable.populateLoot(Rnd.RANDOM, context);

        event.getDrops().addAll(items);
        return true;
    }

    @Override
    public boolean onDeath(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, ItemStack item, int level) {
        return false;
    }

    @Override
    public boolean onResurrect(@NotNull EntityResurrectEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        return false;
    }
}
