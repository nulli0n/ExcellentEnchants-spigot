package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

public class SniperEnchant extends GameEnchantment implements BowEnchant {

    private Modifier speedModifier;

    public SniperEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.oneHundred());
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.speedModifier = Modifier.load(config, "Sniper.Speed_Modifier",
            Modifier.addictive(1).perLevel(0.2).capacity(3D),
            "Projectile's speed modifier.");

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getSpeedModifier(level) * 100D));
    }

    public double getSpeedModifier(int level) {
        return this.speedModifier.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getShootPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        double modifier = this.getSpeedModifier(level);

        Entity entity = event.getProjectile();
        Vector vector = entity.getVelocity();
        this.plugin.runTask(entity, () -> entity.setVelocity(vector.multiply(modifier)));

        return true;
    }
}
