package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;
import java.util.Set;

import static su.nightexpress.excellentenchants.Placeholders.GENERIC_DAMAGE;

public class BaneOfNetherspawnEnchant extends GameEnchantment implements CombatEnchant {

    public static final String ID = "bane_of_netherspawn";

    private static final Set<EntityType> ENTITY_TYPES = Lists.newSet(
        EntityType.BLAZE, EntityType.MAGMA_CUBE,
        EntityType.WITHER_SKELETON, EntityType.GHAST, EntityType.WITHER,
        EntityType.PIGLIN, EntityType.PIGLIN_BRUTE,
        EntityType.ZOGLIN, EntityType.HOGLIN,
        EntityType.STRIDER, EntityType.ZOMBIFIED_PIGLIN
    );

    private boolean  damageModifier;
    private Modifier damageAmount;

    public BaneOfNetherspawnEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.JUNGLE_COMMON));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            "Inflicts " + GENERIC_DAMAGE + "❤ more damage to nether mobs.",
            EnchantRarity.COMMON,
            5,
            ItemCategories.WEAPON
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.damageModifier = ConfigValue.create("Settings.Damage.As_Modifier",
            false,
            "When 'true' multiplies the damage. When 'false' sums plain values."
        ).read(config);

        this.damageAmount = Modifier.read(config, "Settings.Damage.Amount",
            Modifier.add(0.75, 0.25, 1, 10000),
            "Amount of additional damage."
        );

        this.addPlaceholder(GENERIC_DAMAGE, level -> NumberUtil.format(this.getDamageAmount(level)));
    }

    public double getDamageAmount(int level) {
        return this.damageAmount.getValue(level);
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!ENTITY_TYPES.contains(victim.getType())) return false;

        double damageEvent = event.getDamage();
        double damageAdd = this.getDamageAmount(level);
        event.setDamage(this.damageModifier ? damageEvent * damageAdd : damageEvent + damageAdd);

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.SMOKE).play(victim.getEyeLocation(), 0.25, 0.1, 20);
            UniParticle.of(Particle.LAVA).play(victim.getEyeLocation(), 0.25, 0.1, 5);
        }

        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
