package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.AttackEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

public class SwiperEnchant extends GameEnchantment implements AttackEnchant {

    private Modifier xpAmount;

    public SwiperEnchant(@NotNull EnchantsPlugin plugin, File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(6, 2));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.xpAmount = Modifier.load(config, "Swiper.XP_Amount",
            Modifier.addictive(0).perLevel(1).capacity(10),
            "Amount of XP to be stolen on hit.");

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getXPAmount(level)));
    }

    public int getXPAmount(int level) {
        return (int) this.xpAmount.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!(damager instanceof Player attacker)) return false;
        if (!(victim instanceof Player defender)) return false;
        if (defender.getTotalExperience() == 0) return false;

        int amount = this.getXPAmount(level);
        if (defender.getTotalExperience() < amount) amount = defender.getTotalExperience();

        defender.giveExp(-amount);
        attacker.giveExp(amount);
        return true;
    }
}
