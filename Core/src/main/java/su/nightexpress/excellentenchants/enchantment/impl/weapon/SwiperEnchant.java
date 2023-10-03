package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

public class SwiperEnchant extends ExcellentEnchant implements CombatEnchant, Chanced {

    public static final String ID = "swiper";

    public static final String PLACEHOLER_XP_AMOUNT = "%xp_amount%";

    private ChanceImplementation chanceImplementation;
    private EnchantScaler xpAmount;

    public SwiperEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to steal " + PLACEHOLER_XP_AMOUNT + " XP from players.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.7);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

        this.chanceImplementation = ChanceImplementation.create(this,
            "5.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 2.5");

        this.xpAmount = EnchantScaler.read(this, "Settings.XP_Amount",
            Placeholders.ENCHANTMENT_LEVEL,
            "Amount of XP to be stolen on hit.");

        this.addPlaceholder(PLACEHOLER_XP_AMOUNT, level -> NumberUtil.format(this.getXPAmount(level)));
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @NotNull
    @Override
    public Chanced getChanceImplementation() {
        return this.chanceImplementation;
    }

    public int getXPAmount(int level) {
        return (int) this.xpAmount.getValue(level);
    }

    private int getExpRequired(int level) {
        if (level <= 15) return 2 * level + 7;
        if (level <= 30) return 5 * level - 38;
        return 9 * level - 158;
    }

    private void addXP(@NotNull Player player, int amount) {
        int levelHas = player.getLevel();
        int xpHas = player.getTotalExperience();

        xpHas = Math.max(0, xpHas - amount);
        player.setExp(0F);
        player.setTotalExperience(0);
        player.setLevel(0);
        player.giveExp(xpHas);
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!(damager instanceof Player attacker)) return false;
        if (!(victim instanceof Player defender)) return false;
        if (defender.getTotalExperience() == 0) return false;
        if (!this.checkTriggerChance(level)) return false;

        int amount = this.getXPAmount(level);
        if (defender.getTotalExperience() < amount) amount = defender.getTotalExperience();

        this.addXP(defender, -amount);
        this.addXP(attacker, amount);
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
