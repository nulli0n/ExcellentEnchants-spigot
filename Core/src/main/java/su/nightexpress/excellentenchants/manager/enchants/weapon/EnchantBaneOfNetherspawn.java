package su.nightexpress.excellentenchants.manager.enchants.weapon;

import com.google.common.collect.Sets;
import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.Scaler;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.Set;
import java.util.function.UnaryOperator;

public class EnchantBaneOfNetherspawn extends IEnchantChanceTemplate implements CombatEnchant {

    private       String          particleName;
    private       String          particleData;
    private       boolean         damageModifier;
    private       Scaler          damageFormula;
    private final Set<EntityType> entityTypes;

    public static final String ID = "bane_of_netherspawn";

    private static final String PLACEHOLDER_DAMAGE = "%enchantment_damage%";

    public EnchantBaneOfNetherspawn(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);

        this.entityTypes = Sets.newHashSet(EntityType.BLAZE, EntityType.MAGMA_CUBE,
            EntityType.WITHER_SKELETON, EntityType.GHAST, EntityType.WITHER);

        this.entityTypes.add(EntityType.PIGLIN);
        this.entityTypes.add(EntityType.PIGLIN_BRUTE);
        this.entityTypes.add(EntityType.ZOGLIN);
        this.entityTypes.add(EntityType.HOGLIN);
        this.entityTypes.add(EntityType.STRIDER);
        this.entityTypes.add(EntityType.ZOMBIFIED_PIGLIN);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.particleName = cfg.getString("Settings.Particle.Name", "");
        this.particleData = cfg.getString("Settings.Particle.Data", "");
        this.damageModifier = cfg.getBoolean("Settings.Damage.As_Modifier");
        this.damageFormula = new EnchantScaler(this, "Settings.Damage.Formula");
    }

    public double getDamageModifier(int level) {
        return this.damageFormula.getValue(level);
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.remove("Settings.Particle_Effect");
        cfg.addMissing("Settings.Particle.Name", Particle.SMOKE_NORMAL.name());
        cfg.addMissing("Settings.Particle.Data", "");
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_DAMAGE, NumberUtil.format(this.getDamageModifier(level)))
        );
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isEnchantmentAvailable(damager)) return false;
        if (!this.entityTypes.contains(victim.getType())) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(damager)) return false;

        double damageEvent = e.getDamage();
        double damageAdd = this.getDamageModifier(level);
        e.setDamage(this.damageModifier ? damageEvent * damageAdd : damageEvent + damageAdd);
        EffectUtil.playEffect(victim.getEyeLocation(), this.particleName, this.particleData, 0.25, 0.25, 0.25, 0.1f, 30);
        return true;
    }
}
