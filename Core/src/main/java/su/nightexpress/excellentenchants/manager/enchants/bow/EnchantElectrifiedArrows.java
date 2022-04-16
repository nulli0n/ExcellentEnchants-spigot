package su.nightexpress.excellentenchants.manager.enchants.bow;

import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.LocationUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantBowTemplate;
import su.nightexpress.excellentenchants.manager.EnchantRegister;

public class EnchantElectrifiedArrows extends IEnchantBowTemplate {

    public static final String ID = "electrified_arrows";

    public EnchantElectrifiedArrows(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(EnchantRegister.CONFUSING_ARROWS);
        this.addConflict(EnchantRegister.POISONED_ARROWS);
        this.addConflict(EnchantRegister.EXPLOSIVE_ARROWS);
        this.addConflict(EnchantRegister.WITHERED_ARROWS);
        this.addConflict(EnchantRegister.DRAGONFIRE_ARROWS);
    }

    @Override
    public boolean use(@NotNull ProjectileHitEvent e, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        if (!super.use(e, projectile, bow, level)) return false;

        Entity entity = e.getHitEntity();
        Block block = e.getHitBlock();

        if (entity instanceof LivingEntity victim) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                victim.setNoDamageTicks(0);
                victim.getWorld().strikeLightning(victim.getLocation());
            });
        }
        else if (block != null) {
            block.getWorld().strikeLightning(block.getLocation());
            EffectUtil.playEffect(LocationUtil.getCenter(block.getLocation()), Particle.BLOCK_CRACK, block.getType().name(), 1D, 1D, 1D, 0.05, 150);
            EffectUtil.playEffect(LocationUtil.getCenter(block.getLocation()), Particle.FIREWORKS_SPARK, "", 1D, 1D, 1D, 0.05, 150);
        }
        else return false;

        return true;
    }
}
