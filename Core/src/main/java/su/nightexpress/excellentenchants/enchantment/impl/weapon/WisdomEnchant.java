package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class WisdomEnchant extends AbstractEnchantmentData implements DeathEnchant {

    public static final String ID = "exp_hunter";

    private Modifier xpModifier;

    public WisdomEnchant(@NotNull EnchantsPlugin plugin, File file) {
        super(plugin, file);
        this.setDescription("Increases XP dropped from mobs by " + GENERIC_AMOUNT + "%.");
        this.setMaxLevel(5);
        this.setRarity(Rarity.UNCOMMON);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.xpModifier = Modifier.read(config, "Settings.XP_Modifier",
            Modifier.add(1, 0.5, 1, 5D),
            "Exp modifier value. The original exp amount will be multiplied on this value.");

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.getXPModifier(level) * 100D - 100D));
    }

    public final double getXPModifier(int level) {
        return this.xpModifier.getValue(level);
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull Player killer, ItemStack weapon, int level) {
        double expModifier = this.getXPModifier(level);
        double expFinal = Math.ceil((double) event.getDroppedExp() * expModifier);

        event.setDroppedExp((int) expFinal);
        return true;
    }

    @Override
    public boolean onDeath(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, ItemStack item, int level) {
        return false;
    }

    @Override
    public boolean onResurrect(@NotNull EntityResurrectEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        return false;
    }
}
