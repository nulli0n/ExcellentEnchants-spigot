package su.nightexpress.excellentenchants.manager.enchants.weapon;


import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EnchantDecapitator extends IEnchantChanceTemplate implements DeathEnchant {

    private final String      particleName;
    private final String      particleData;
    private final Set<String> ignoredEntityTypes;
    private final String      headName;
    private final Map<String, String> headTextures;

    public static final String ID = "decapitator";

    public EnchantDecapitator(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);

        this.particleName = cfg.getString("Settings.Particle.Name", Particle.BLOCK_CRACK.name());
        this.particleData = cfg.getString("Settings.Particle.Data", Material.REDSTONE_BLOCK.name());
        this.ignoredEntityTypes = cfg.getStringSet("Settings.Ignored_Entity_Types").stream().map(String::toUpperCase).collect(Collectors.toSet());
        this.headName = StringUtil.color(cfg.getString("Settings.Head_Item.Name", "&c%entity%'s Head"));
        this.headTextures = new HashMap<>();
        for (String sType : cfg.getSection("Settings.Head_Item.Textures")) {
            String texture = cfg.getString("Settings.Head_Item.Textures." + sType);
            this.headTextures.put(sType.toUpperCase(), texture);
        }
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        this.cfg.remove("Settings.Particle_Effect");
        this.cfg.addMissing("Settings.Particle.Name", Particle.BLOCK_CRACK.name());
        this.cfg.addMissing("Settings.Particle.Data", Material.REDSTONE_BLOCK.name());
        this.cfg.addMissing("Settings.Ignored_Entity_Types", Arrays.asList("ENDER_DRAGON", "WITHER_SKELETON", "WITHER"));
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean use(@NotNull EntityDeathEvent e, @NotNull LivingEntity victim, int level) {
        if (!this.isEnchantmentAvailable(victim)) return false;

        EntityType entityType = victim.getType();
        if (this.ignoredEntityTypes.contains(entityType.name())) return false;
        if (!this.checkTriggerChance(level)) return false;

        Player killer = victim.getKiller();
        if (killer == null) return false;
        if (!this.takeCostItem(killer)) return false;

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
            if (victim instanceof Player player) {
                entityName = this.headName.replace("%entity%", victim.getName());
                meta.setOwningPlayer(player);
            }
            else {
                String texture = this.headTextures.get(victim.getType().name());
                if (texture == null) return false;

                entityName = this.headName.replace("%entity%", plugin.getLangManager().getEnum(victim.getType()));
                ItemUtil.setSkullTexture(item, texture);
                meta = (SkullMeta) item.getItemMeta();
            }

            meta.setDisplayName(entityName);
            item.setItemMeta(meta);
        }

        victim.getWorld().dropItemNaturally(victim.getLocation(), item);
        EffectUtil.playEffect(victim.getEyeLocation(), this.particleName, this.particleData, 0.2f, 0.15f, 0.2f, 0.15f, 40);
        return true;
    }
}
