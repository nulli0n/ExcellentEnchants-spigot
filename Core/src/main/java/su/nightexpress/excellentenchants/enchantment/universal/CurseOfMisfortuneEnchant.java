package su.nightexpress.excellentenchants.enchantment.universal;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.KillEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.MiningEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;

public class CurseOfMisfortuneEnchant extends GameEnchantment implements MiningEnchant, KillEnchant {

    private boolean dropXP;

    public CurseOfMisfortuneEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(0, 7));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.dropXP = ConfigValue.create("CurseOfMisfortune.Drop_XP",
            false,
            "Controls whether XP should drop."
        ).read(config);
    }

    public boolean isDropXP() {
        return this.dropXP;
    }

    @Override
    @NotNull
    public EnchantPriority getBreakPriority() {
        return EnchantPriority.HIGHEST;
    }

    @NotNull
    @Override
    public EnchantPriority getKillPriority() {
        return EnchantPriority.HIGHEST;
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent event, @NotNull LivingEntity player, @NotNull ItemStack item, int level) {
        event.setDropItems(false);
        if (!this.dropXP) event.setExpToDrop(0);
        return true;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull Player killer, @NotNull ItemStack weapon, int level) {
        event.getDrops().clear();
        if (!this.dropXP) event.setDroppedExp(0);
        return true;
    }
}
