package su.nightexpress.excellentenchants.enchantment.impl.weapon;


import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EnchantDecapitator extends ExcellentEnchant implements Chanced, DeathEnchant {

    public static final String ID = "decapitator";

    private Set<String> ignoredEntityTypes;
    private String      headName;
    private Map<String, String> headTextures;
    private ChanceImplementation chanceImplementation;

    public EnchantDecapitator(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.chanceImplementation = ChanceImplementation.create(this);

        this.ignoredEntityTypes = JOption.create("Settings.Ignored_Entity_Types", new HashSet<>(),
            "List of entities, that won't drop heads.").read(cfg).stream().map(String::toUpperCase).collect(Collectors.toSet());

        this.headName = JOption.create("Settings.Head_Item.Name", "&c" + Placeholders.GENERIC_TYPE + "'s Head",
            "Head item display name. Use '" + Placeholders.GENERIC_TYPE + "' for entity name.").read(cfg);

        this.headTextures = new HashMap<>();
        for (String sType : cfg.getSection("Settings.Head_Item.Textures")) {
            this.headTextures.put(sType.toUpperCase(), cfg.getString("Settings.Head_Item.Textures." + sType, ""));
        }
        this.cfg.setComments("Settings.Head_Item.Textures",
            "Head texture values for each entity type.",
            "You can take some from http://minecraft-heads.com",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html");

        /*this.headTextures = new JOption<Map<String, String>>("Settings.Head_Item.Textures",
            (cfg, path, def) -> cfg.getSection(path).stream().collect(Collectors.toMap(String::toUpperCase, v -> cfg.getString(path + "." + v, ""))),
            HashMap::new,
            "Head texture values for each entity type.",
            "You can take some from http://minecraft-heads.com",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html").read(cfg);*/
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
    public boolean onDeath(@NotNull EntityDeathEvent e, @NotNull LivingEntity entity, int level) {
        return false;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent e, @NotNull LivingEntity entity, @NotNull Player killer, int level) {
        if (!this.isAvailableToUse(entity)) return false;

        EntityType entityType = entity.getType();
        if (this.ignoredEntityTypes.contains(entityType.name())) return false;
        if (!this.checkTriggerChance(level)) return false;

        ItemStack item;
        if (entityType == EntityType.WITHER_SKELETON || entityType == EntityType.WITHER) {
            item = new ItemStack(Material.WITHER_SKELETON_SKULL);
        }
        else if (entityType == EntityType.ZOMBIE || entityType == EntityType.GIANT) {
            item = new ItemStack(Material.ZOMBIE_HEAD);
        }
        else if (entityType == EntityType.SKELETON) {
            item = new ItemStack(Material.SKELETON_SKULL);
        }
        else if (entityType == EntityType.CREEPER) {
            item = new ItemStack(Material.CREEPER_HEAD);
        }
        else if (entityType == EntityType.ENDER_DRAGON) {
            item = new ItemStack(Material.DRAGON_HEAD);
        }
        else {
            item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta == null) return false;

            String entityName;
            if (entity instanceof Player player) {
                entityName = this.headName.replace(Placeholders.GENERIC_TYPE, entity.getName());
                meta.setOwningPlayer(player);
            }
            else {
                String texture = this.headTextures.get(entity.getType().name());
                if (texture == null) return false;

                entityName = this.headName.replace(Placeholders.GENERIC_TYPE, plugin.getLangManager().getEnum(entity.getType()));
                ItemUtil.setSkullTexture(item, texture);
                meta = (SkullMeta) item.getItemMeta();
            }

            meta.setDisplayName(entityName);
            item.setItemMeta(meta);
        }

        entity.getWorld().dropItemNaturally(entity.getLocation(), item);

        if (this.hasVisualEffects()) {
            EffectUtil.playEffect(entity.getEyeLocation(), Particle.BLOCK_CRACK, Material.REDSTONE_BLOCK.name(), 0.2f, 0.15f, 0.2f, 0.15f, 40);
        }
        return true;
    }
}
