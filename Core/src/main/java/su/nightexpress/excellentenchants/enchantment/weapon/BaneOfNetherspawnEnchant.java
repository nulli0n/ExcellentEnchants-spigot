package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.AttackEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;
import java.util.Set;

public class BaneOfNetherspawnEnchant extends GameEnchantment implements AttackEnchant {

    private static final Set<EntityType> ENTITY_TYPES = Lists.newSet(
        EntityType.BLAZE, EntityType.MAGMA_CUBE,
        EntityType.WITHER_SKELETON, EntityType.GHAST, EntityType.WITHER,
        EntityType.PIGLIN, EntityType.PIGLIN_BRUTE,
        EntityType.ZOGLIN, EntityType.HOGLIN,
        EntityType.STRIDER, EntityType.ZOMBIFIED_PIGLIN
    );

    private boolean  multiplier;
    private Modifier damageMod;

    public BaneOfNetherspawnEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.multiplier = ConfigValue.create("BaneOfNetherspawn.Damage.Multiplier",
            false,
            "When 'true' multiplies the damage. When 'false' sums plain values."
        ).read(config);

        this.damageMod = Modifier.load(config, "BaneOfNetherspawn.Damage.Amount",
            Modifier.addictive(0.75).perLevel(0.25).capacity(1000D),
            "Amount of additional damage."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_DAMAGE, level -> NumberUtil.format(this.getDamageAmount(level)));
    }

    public double getDamageAmount(int level) {
        return this.damageMod.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.LOW;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!ENTITY_TYPES.contains(victim.getType())) return false;

        double damageEvent = event.getDamage();
        double damageAdd = this.getDamageAmount(level);
        event.setDamage(this.multiplier ? damageEvent * damageAdd : damageEvent + damageAdd);

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.SMOKE).play(victim.getEyeLocation(), 0.25, 0.1, 20);
            UniParticle.of(Particle.LAVA).play(victim.getEyeLocation(), 0.25, 0.1, 5);
        }

        return true;
    }
}
