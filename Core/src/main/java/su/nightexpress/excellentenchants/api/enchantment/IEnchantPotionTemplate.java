package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public abstract class IEnchantPotionTemplate extends IEnchantChanceTemplate {

    public static final String PLACEHOLDER_POTION_LEVEL    = "%enchantment_potion_level%";
    public static final String PLACEHOLDER_POTION_DURATION = "%enchantment_potion_duration%";
    public static final String PLACEHOLDER_POTION_TYPE     = "%enchantment_potion_type%";

    protected PotionEffectType potionEffectType;
    protected final boolean          potionParticles;
    protected       Scaler           potionDuration;
    protected       Scaler           potionLevel;

    public IEnchantPotionTemplate(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg,
                                  @NotNull EnchantPriority priority,
                                  @NotNull PotionEffectType potionEffectType) {
        super(plugin, cfg, priority);
        this.potionEffectType = potionEffectType;
        this.potionParticles = !(this instanceof PassiveEnchant);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.potionDuration = new EnchantScaler(this, "Settings.Potion_Effect.Duration");
        this.potionLevel = new EnchantScaler(this, "Settings.Potion_Effect.Level");
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_POTION_LEVEL, NumberUtil.toRoman(this.getEffectLevel(level)))
            .replace(PLACEHOLDER_POTION_DURATION, NumberUtil.format((double) this.getEffectDuration(level) / 20D))
            .replace(PLACEHOLDER_POTION_TYPE, LangManager.getPotionType(this.getEffectType()))
        );
    }

    @NotNull
    public final PotionEffectType getEffectType() {
        return this.potionEffectType;
    }

    public final int getEffectDuration(int level) {
        return (int) (this.potionDuration.getValue(level) * 20);
    }

    public final int getEffectLevel(int level) {
        return (int) this.potionLevel.getValue(level);
    }

    @NotNull
    public PotionEffect getEffect(int level) {
        int duration = this.getEffectDuration(level);
        int amplifier = Math.max(0, this.getEffectLevel(level) - 1);

        return new PotionEffect(this.potionEffectType, duration, amplifier, false, this.potionParticles);
    }

    public final boolean hasEffect(@NotNull LivingEntity entity) {
        return EnchantManager.hasEnchantmentEffect(entity, this);
    }

    public final boolean addEffect(@NotNull LivingEntity target, int level) {
        if (this instanceof PassiveEnchant) {
            this.plugin.getEnchantNMS().addEnchantmentEffect(target, this, this.getEffect(level));
        }
        else {
            target.addPotionEffect(this.getEffect(level));
        }
        return true;
    }
}