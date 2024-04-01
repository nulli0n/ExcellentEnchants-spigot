package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class SwiperEnchant extends AbstractEnchantmentData implements CombatEnchant, ChanceData {

    public static final String ID = "swiper";

    public static final String PLACEHOLER_XP_AMOUNT = "%xp_amount%";

    private ChanceSettingsImpl chanceSettings;
    private Modifier           xpAmount;

    public SwiperEnchant(@NotNull EnchantsPlugin plugin, File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to steal " + PLACEHOLER_XP_AMOUNT + " XP from players.");
        this.setMaxLevel(3);
        this.setRarity(Rarity.RARE);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(5, 2.5, 1));

        this.xpAmount = Modifier.read(config, "Settings.XP_Amount",
            Modifier.add(0, 1, 1),
            "Amount of XP to be stolen on hit.");

        this.addPlaceholder(PLACEHOLER_XP_AMOUNT, level -> NumberUtil.format(this.getXPAmount(level)));
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.WEAPON;
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return this.chanceSettings;
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
        //int levelHas = player.getLevel();
        int xpHas = player.getTotalExperience();

        xpHas = Math.max(0, xpHas + amount);
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
