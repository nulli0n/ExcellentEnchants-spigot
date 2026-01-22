package su.nightexpress.excellentenchants.enchantment.tool;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.MiningEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.nio.file.Path;

public class LuckyMinerEnchant extends GameEnchantment implements MiningEnchant {

    private Modifier xpModifier;

    public LuckyMinerEnchant(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager, @NotNull Path file, @NotNull EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(20, 10));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.xpModifier = Modifier.load(config, "LuckyMiner.XP_Modifier",
            Modifier.addictive(1).perLevel(0.5).capacity(3D),
            "XP Modifier. Amount of dropped XP will be multiplied on this value."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getXPModifier(level) * 100D - 100D));
    }

    public double getXPModifier(int level) {
        return this.xpModifier.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getBreakPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent event, @NotNull LivingEntity player, @NotNull ItemStack item, int level) {
        double expMod = this.getXPModifier(level);
        event.setExpToDrop((int) ((double) event.getExpToDrop() * expMod));
        return true;
    }
}
