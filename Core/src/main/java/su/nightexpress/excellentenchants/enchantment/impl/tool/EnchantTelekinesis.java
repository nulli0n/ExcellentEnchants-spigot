package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantDropContainer;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnchantTelekinesis extends ExcellentEnchant implements BlockDropEnchant {

    public static final String ID = "telekinesis";

    public EnchantTelekinesis(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.LOWEST);
    }

    @Override
    @NotNull
    public FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.TOOL};
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean onDrop(@NotNull BlockDropItemEvent e, @NotNull EnchantDropContainer dropContainer, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.isAvailableToUse(player)) return false;

        List<ItemStack> drops = new ArrayList<>();
        drops.addAll(e.getItems().stream().map(Item::getItemStack).toList());
        drops.addAll(dropContainer.getDrop());
        drops.removeIf(Objects::isNull);
        drops.forEach(drop -> PlayerUtil.addItem(player, drop));

        dropContainer.getDrop().clear();
        e.getItems().clear();

        return true;
    }
}
