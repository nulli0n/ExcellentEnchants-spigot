package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;

public class SwiperEnchant extends GameEnchantment implements CombatEnchant, ChanceMeta {

    public static final String ID = "swiper";

    public static final String PLACEHOLER_XP_AMOUNT = "%xp_amount%";

    private Modifier xpAmount;

    public SwiperEnchant(@NotNull EnchantsPlugin plugin, File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.JUNGLE_SPECIAL));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            ENCHANTMENT_CHANCE + "% chance to steal " + PLACEHOLER_XP_AMOUNT + " XP from players.",
            EnchantRarity.LEGENDARY,
            3,
            ItemCategories.WEAPON
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(5, 2.5, 1)));

        this.xpAmount = Modifier.read(config, "Settings.XP_Amount",
            Modifier.add(0, 1, 1),
            "Amount of XP to be stolen on hit.");

        this.addPlaceholder(PLACEHOLER_XP_AMOUNT, level -> NumberUtil.format(this.getXPAmount(level)));
    }

    public int getXPAmount(int level) {
        return (int) this.xpAmount.getValue(level);
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!(damager instanceof Player attacker)) return false;
        if (!(victim instanceof Player defender)) return false;
        if (defender.getTotalExperience() == 0) return false;
        if (!this.checkTriggerChance(level)) return false;

        int amount = this.getXPAmount(level);
        if (defender.getTotalExperience() < amount) amount = defender.getTotalExperience();

        defender.giveExp(-amount);
        attacker.giveExp(amount);
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
