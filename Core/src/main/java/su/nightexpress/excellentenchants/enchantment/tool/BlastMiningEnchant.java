package su.nightexpress.excellentenchants.enchantment.tool;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.MiningEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;
import java.util.List;

public class BlastMiningEnchant extends GameEnchantment implements MiningEnchant {

    private Modifier explosionPower;
    private double minBlockStrength;

    public BlastMiningEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(0, 10));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.explosionPower = Modifier.load(config, "BlastMining.Explosion_Power",
            Modifier.addictive(3).perLevel(0.75).capacity(8),
            "Explosion power. The more power = the more blocks (area) to explode.");

        this.minBlockStrength = ConfigValue.create("BlastMining.Min_Block_Strength",
            1.3D,
            "Minimal block strength value for the enchantment to have effect.",
            "Block strength value is how long it takes to break the block by a hand.",
            "For example, a Stone has 3.0 strength."
            ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_RADIUS, level -> NumberUtil.format(this.getExplosionPower(level)));
    }

    public double getExplosionPower(int level) {
        return this.explosionPower.getValue(level);
    }

    private boolean isHardEnough(@NotNull Block block) {
        float strength = block.getType().getHardness();
        return (strength >= this.minBlockStrength);
    }

    @Override
    @NotNull
    public EnchantPriority getBreakPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        if (!(entity instanceof Player player)) return false;
        if (EnchantUtils.isBusy()) return false;

        Block block = event.getBlock();
        if (!this.isHardEnough(block)) return false;

        float power = (float) this.getExplosionPower(level);

        return this.plugin.getEnchantManager().createExplosion(player, block.getLocation(), power, false, true, explosion -> {
            explosion.setOnDamage(damageEvent -> damageEvent.setCancelled(true));
            explosion.setOnExplode(explodeEvent -> {
                List<Block> blockList = explodeEvent.blockList();
                blockList.forEach(explodedBlock -> {
                    if (explodedBlock.getLocation().equals(block.getLocation())) return;

                    EnchantUtils.safeBusyBreak(player, explodedBlock);
                });
                blockList.clear();
            });
        });
    }
}
