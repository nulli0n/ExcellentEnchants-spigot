package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EnchantThrifty extends ExcellentEnchant implements Chanced, DeathEnchant {

    public static final String ID = "thrifty";

    private Set<EntityType>                    ignoredEntityTypes;
    private Set<CreatureSpawnEvent.SpawnReason> ignoredSpawnReasons;
    private final NamespacedKey                keyEntityIgnored;

    private ChanceImplementation chanceImplementation;

    public EnchantThrifty(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.keyEntityIgnored = new NamespacedKey(plugin, ID + "_ignored");
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.chanceImplementation = ChanceImplementation.create(this);

        this.ignoredEntityTypes = JOption.create("Settings.Ignored_Entity_Types",
            Set.of(EntityType.WITHER.name(), EntityType.ENDER_DRAGON.name()),
            "List of entity types, that will not drop spawn eggs.",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html")
            .read(cfg).stream().map(e -> CollectionsUtil.getEnum(e, EntityType.class))
            .filter(Objects::nonNull).collect(Collectors.toSet());

        this.ignoredSpawnReasons = JOption.create("Settings.Ignored_Spawn_Reasons",
            Set.of(CreatureSpawnEvent.SpawnReason.SPAWNER_EGG.name(),
                CreatureSpawnEvent.SpawnReason.SPAWNER.name(),
                CreatureSpawnEvent.SpawnReason.DISPENSE_EGG.name()),
            "Entities will not drop spawn eggs if they were spawned by one of the reasons below.",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html")
            .read(cfg).stream().map(e -> CollectionsUtil.getEnum(e, CreatureSpawnEvent.SpawnReason.class))
            .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent e, @NotNull LivingEntity entity, @NotNull Player killer, int level) {
        if (!this.isAvailableToUse(entity)) return false;

        if (this.ignoredEntityTypes.contains(entity.getType())) return false;
        if (PDCUtil.getBoolean(entity, this.keyEntityIgnored).orElse(false)) return false;
        if (!this.checkTriggerChance(level)) return false;

        Material material = Material.getMaterial(entity.getType().name() + "_SPAWN_EGG");
        if (material == null) {
            if (entity.getType() == EntityType.MUSHROOM_COW) {
                material = Material.MOOSHROOM_SPAWN_EGG;
            }
            else return false;
        }

        ItemStack egg = new ItemStack(material);
        e.getDrops().add(egg);
        return true;
    }

    @Override
    public boolean onDeath(@NotNull EntityDeathEvent e, @NotNull LivingEntity entity, int level) {
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!this.ignoredSpawnReasons.contains(e.getSpawnReason())) return;

        PDCUtil.set(e.getEntity(), this.keyEntityIgnored, true);
    }
}
