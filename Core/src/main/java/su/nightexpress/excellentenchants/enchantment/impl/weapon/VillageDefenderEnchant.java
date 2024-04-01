package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Illager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class VillageDefenderEnchant extends AbstractEnchantmentData implements CombatEnchant {

    public static final String ID = "village_defender";

    private boolean  damageMultiplier;
    private Modifier damageAmount;

    public VillageDefenderEnchant(@NotNull EnchantsPlugin plugin, File file) {
        super(plugin, file);
        this.setDescription("Inflicts " + GENERIC_AMOUNT + "❤ more damage to all pillagers.");
        this.setMaxLevel(5);
        this.setRarity(Rarity.COMMON);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.damageAmount = Modifier.read(config, "Settings.Damage.Amount",
            Modifier.add(0.5, 0.5, 1),
            "Amount of additional damage.");

        this.damageMultiplier = ConfigValue.create("Settings.Damage.As_Modifier",
            false,
            "When 'true' the 'Damage.Formula' will work as a multiplier to the original damage."
        ).read(config);

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.getDamageAddict(level)));
    }

    public double getDamageAddict(int level) {
        return this.damageAmount.getValue(level);
    }

    public boolean isDamageMultiplier() {
        return damageMultiplier;
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!(victim instanceof Illager)) return false;

        double damageAdd = this.getDamageAddict(level);
        double damageHas = event.getDamage();
        double damageFinal = this.isDamageMultiplier() ? (damageHas * damageAdd) : (damageHas + damageAdd);

        event.setDamage(damageFinal);

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.VILLAGER_ANGRY).play(victim.getEyeLocation(), 0.25, 0.1, 30);
        }
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
