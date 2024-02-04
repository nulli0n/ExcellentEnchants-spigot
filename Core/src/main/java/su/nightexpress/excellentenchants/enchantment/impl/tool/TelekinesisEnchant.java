package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.api.enchantment.ItemCategory;

public class TelekinesisEnchant extends ExcellentEnchant implements Chanced, BlockDropEnchant {

    public static final String ID = "telekinesis";

    private ChanceImplementation chanceImplementation;

    public TelekinesisEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription("Moves all blocks loot directly to your inventory.");
        this.getDefaults().setLevelMax(1);
        this.getDefaults().setTier(0.75);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this, "100");
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    @NotNull
    public ItemCategory[] getFitItemTypes() {
        return new ItemCategory[]{ItemCategory.TOOL};
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.TOOL;
    }

    @NotNull
    @Override
    public EventPriority getDropPriority() {
        return EventPriority.HIGHEST;
    }

    @Override
    public boolean onDrop(@NotNull BlockDropItemEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        if (!(entity instanceof Player player)) return false;
        if (!this.checkTriggerChance(level)) return false;

        event.getItems().forEach(drop -> {
            PlayerUtil.addItem(player, drop.getItemStack());
        });
        event.getItems().clear();
        return true;
    }
}
