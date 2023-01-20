package su.nightexpress.excellentenchants.nms.v1_17_R1;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Reflex;
import su.nightexpress.excellentenchants.nms.EnchantNMS;

public class CustomEffectInstance extends MobEffectInstance {

    private final Enchantment enchantment;

    public CustomEffectInstance(MobEffect effect, int amplifier, @NotNull Enchantment enchantment) {
        super(effect, EnchantNMS.EFFECT_DURATION_MAX, amplifier);
        this.enchantment = enchantment;
    }

    @NotNull
    public Enchantment getEnchantment() {
        return enchantment;
    }

    @Override
    public boolean update(MobEffectInstance effect) {
        /*if (effect instanceof CustomEffectInstance custom) {
            return false;
        }
        if (effect.getAmplifier() > this.getAmplifier()) {

        }*/
        return false;
    }

    public boolean tick(LivingEntity entity, Runnable runnable) {
        if (EnchantNMS.getEquippedEnchantLevel((org.bukkit.entity.LivingEntity) entity.getBukkitEntity(), this.getEnchantment()) <= 0) {
            return false;
        }
        if (super.tick(entity, runnable)) {
            if (this.getDuration() <= EnchantNMS.EFFECT_DURATION_MIN) {
                Reflex.setFieldValue(this, "c", EnchantNMS.EFFECT_DURATION_MAX);
            }
            return true;
        }
        return false;
    }
}
