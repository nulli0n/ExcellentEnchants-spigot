package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.manager.EventListener;
import su.nexmedia.engine.utils.EngineUtils;
import su.nexmedia.engine.utils.PDCUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.hook.HookId;
import su.nightexpress.excellentenchants.hook.impl.MythicMobsHook;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EnchantThrifty extends ExcellentEnchant implements Chanced, DeathEnchant, EventListener {

    public static final String ID = "thrifty";

    private final NamespacedKey                keyEntityIgnored;

    private Set<EntityType>                    ignoredEntityTypes;
    private Set<CreatureSpawnEvent.SpawnReason> ignoredSpawnReasons;
    private boolean ignoreMythicMobs;

    private ChanceImplementation chanceImplementation;

    public EnchantThrifty(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to obtain mob spawn egg on kill.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.75);

        this.keyEntityIgnored = new NamespacedKey(plugin, ID + "_ignored");
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "5.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 3");

        this.ignoredEntityTypes = JOption.create("Settings.Ignored_Entity_Types",
            Set.of(EntityType.WITHER.name(), EntityType.ENDER_DRAGON.name()),
            "List of entity types, that will not drop spawn eggs.",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html"
        ).read(cfg).stream().map(e -> StringUtil.getEnum(e, EntityType.class).orElse(null))
            .filter(Objects::nonNull).collect(Collectors.toSet());

        this.ignoredSpawnReasons = JOption.create("Settings.Ignored_Spawn_Reasons",
            Set.of(CreatureSpawnEvent.SpawnReason.SPAWNER_EGG.name(),
                CreatureSpawnEvent.SpawnReason.SPAWNER.name(),
                CreatureSpawnEvent.SpawnReason.DISPENSE_EGG.name()),
            "Entities will not drop spawn eggs if they were spawned by one of the reasons below.",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html"
        ).read(cfg).stream().map(e -> StringUtil.getEnum(e, CreatureSpawnEvent.SpawnReason.class).orElse(null))
            .filter(Objects::nonNull).collect(Collectors.toSet());

        this.ignoreMythicMobs = JOption.create("Settings.Ignore_MythicMobs", true,
            "Sets whether or not MythicMobs should be immune to enchantment effect."
        ).read(cfg);
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull Player killer, ItemStack weapon, int level) {
        if (this.ignoredEntityTypes.contains(entity.getType())) return false;
        if (PDCUtil.getBoolean(entity, this.keyEntityIgnored).orElse(false)) return false;
        if (this.ignoreMythicMobs && EngineUtils.hasPlugin(HookId.MYTHIC_MOBS) && MythicMobsHook.isMythicMob(entity)) return false;
        if (!this.checkTriggerChance(level)) return false;

        ItemStack eggItem = plugin.getEnchantNMS().getSpawnEgg(entity);
        if (eggItem == null) return false;

        event.getDrops().add(eggItem);
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!this.ignoredSpawnReasons.contains(event.getSpawnReason())) return;

        PDCUtil.set(event.getEntity(), this.keyEntityIgnored, true);
    }
}
