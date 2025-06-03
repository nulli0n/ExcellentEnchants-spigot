package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.Particle;
import org.bukkit.entity.Illager;
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
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

public class VillageDefenderEnchant extends GameEnchantment implements AttackEnchant {

    private boolean  damageMultiplier;
    private Modifier damageAmount;

    public VillageDefenderEnchant(@NotNull EnchantsPlugin plugin, File file, @NotNull EnchantData data) {
        super(plugin, file, data);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.damageAmount = Modifier.load(config, "VillageDefender.Damage",
            Modifier.addictive(0.5).perLevel(0.5).capacity(1000D),
            "Amount of additional damage.");

        this.damageMultiplier = ConfigValue.create("VillageDefender.Multiplier",
            false,
            "When 'true' the 'Damage.Formula' will work as a multiplier to the original damage."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getDamageAddict(level)));
    }

    public double getDamageAddict(int level) {
        return this.damageAmount.getValue(level);
    }

    public boolean isDamageMultiplier() {
        return damageMultiplier;
    }

    @Override
    @NotNull
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.LOW;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!(victim instanceof Illager)) return false;

        double damageAdd = this.getDamageAddict(level);
        double damageHas = event.getDamage();
        double damageFinal = this.isDamageMultiplier() ? (damageHas * damageAdd) : (damageHas + damageAdd);

        event.setDamage(damageFinal);

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.ANGRY_VILLAGER).play(victim.getEyeLocation(), 0.25, 0.1, 30);
        }
        return true;
    }
}
