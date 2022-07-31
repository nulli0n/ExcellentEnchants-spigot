package su.nightexpress.excellentenchants.manager.enchants.weapon;

import com.google.common.collect.Sets;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;

import java.util.Set;

public class EnchantCure extends IEnchantChanceTemplate implements CombatEnchant {

    private Sound sound;
    private String particleName;
    private String particleData;

    public static final String ID = "cure";

    private static final Set<EntityType> MOBS_TO_CURE = Sets.newHashSet(EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOMBIE_VILLAGER);

    public EnchantCure(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.sound = cfg.getEnum("Settings.Sound", Sound.class);
        this.particleName = cfg.getString("Settings.Particle.Name", Particle.CLOUD.name());
        this.particleData = cfg.getString("Settings.Particle.Data", "");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.remove("Settings.Particle_Effect");
        cfg.addMissing("Settings.Particle.Name", Particle.CLOUD.name());
        cfg.addMissing("Settings.Particle.Data", "");
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isEnchantmentAvailable(damager)) return false;
        if (!MOBS_TO_CURE.contains(victim.getType())) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(damager)) return false;

        e.setCancelled(true);

        EffectUtil.playEffect(victim.getLocation(), this.particleName, this.particleData, 0.25, 0.25, 0.25, 0.1f, 20);
        MessageUtil.sound(victim.getLocation(), this.sound);

        if (victim instanceof PigZombie pigZombie) {
            victim.getWorld().spawn(victim.getLocation(), Piglin.class);
            victim.remove();
        }
        else if (victim instanceof ZombieVillager zombieVillager) {
            zombieVillager.setConversionTime(1);
        }
        return true;
    }
}
