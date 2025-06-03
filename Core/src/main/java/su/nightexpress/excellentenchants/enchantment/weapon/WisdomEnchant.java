package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.KillEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

public class WisdomEnchant extends GameEnchantment implements KillEnchant {

    private Modifier xpModifier;

    public WisdomEnchant(@NotNull EnchantsPlugin plugin, File file, @NotNull EnchantData data) {
        super(plugin, file, data);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.xpModifier = Modifier.load(config, "Wisdom.XP_Modifier",
            Modifier.addictive(1).perLevel(0.5).capacity(3D),
            "Exp modifier value. The original exp amount will be multiplied on this value.");

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getXPModifier(level) * 100D - 100D));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_MODIFIER, level -> NumberUtil.format(this.getXPModifier(level)));
    }

    public final double getXPModifier(int level) {
        return this.xpModifier.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getKillPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull Player killer, @NotNull ItemStack weapon, int level) {
        double xpModifier = this.getXPModifier(level);
        double xpFinal = Math.ceil((double) event.getDroppedExp() * xpModifier);

        event.setDroppedExp((int) xpFinal);
        return true;
    }
}
