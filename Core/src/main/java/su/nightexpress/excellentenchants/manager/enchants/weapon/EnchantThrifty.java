package su.nightexpress.excellentenchants.manager.enchants.weapon;

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
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;

import java.util.Set;
import java.util.stream.Collectors;

public class EnchantThrifty extends IEnchantChanceTemplate implements DeathEnchant {

    private Set<String> ignoredEntityTypes;
    private Set<String> ignoredSpawnReasons;
    private final NamespacedKey keyEntityIgnored;

    public static final String ID = "thrifty";

    public EnchantThrifty(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
        this.keyEntityIgnored = new NamespacedKey(plugin, ID + "_ignored");
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.ignoredEntityTypes = cfg.getStringSet("Settings.Ignored_Entity_Types").stream().map(String::toUpperCase).collect(Collectors.toSet());
        this.ignoredSpawnReasons = cfg.getStringSet("Settings.Ignored_Spawn_Reasons").stream().map(String::toUpperCase).collect(Collectors.toSet());
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean use(@NotNull EntityDeathEvent e, @NotNull LivingEntity dead, int level) {
        if (!this.isEnchantmentAvailable(dead)) return false;

        Player killer = dead.getKiller();
        if (killer == null) return false;

        if (this.ignoredEntityTypes.contains(dead.getType().name())) return false;
        if (PDCUtil.getBooleanData(dead, this.keyEntityIgnored)) return false;
        if (!this.checkTriggerChance(level)) return false;

        Material material = Material.getMaterial(dead.getType().name() + "_SPAWN_EGG");
        if (material == null) {
            if (dead.getType() == EntityType.MUSHROOM_COW) {
                material = Material.MOOSHROOM_SPAWN_EGG;
            }
            else return false;
        }
        if (!this.takeCostItem(killer)) return false;

        ItemStack egg = new ItemStack(material);
        e.getDrops().add(egg);
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSettingCreatureSpawnReason(CreatureSpawnEvent e) {
        if (!this.ignoredSpawnReasons.contains(e.getSpawnReason().name())) return;

        PDCUtil.setData(e.getEntity(), this.keyEntityIgnored, true);
    }
}
