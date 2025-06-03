package su.nightexpress.excellentenchants.enchantment.universal;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.KillEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemUtil;

import java.io.File;

public class CurseOfMediocrityEnchant extends GameEnchantment implements BlockDropEnchant, KillEnchant {

    public CurseOfMediocrityEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(0, 15));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @Override
    @NotNull
    public EnchantPriority getDropPriority() {
        return EnchantPriority.HIGHEST;
    }

    @Override
    @NotNull
    public EnchantPriority getKillPriority() {
        return EnchantPriority.HIGHEST;
    }

    @Override
    public boolean onDrop(@NotNull BlockDropItemEvent event, @NotNull LivingEntity player, @NotNull ItemStack item, int level) {
        event.getItems().forEach(drop -> {
            ItemStack stack = drop.getItemStack();
            EnchantUtils.removeAll(stack);
            drop.setItemStack(stack);
        });

        return true;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull Player killer, @NotNull ItemStack weapon, int level) {
        event.getDrops().forEach(stack -> {
            ItemUtil.editMeta(stack, meta -> {
                meta.getEnchants().keySet().forEach(meta::removeEnchant);
            });
        });

        return true;
    }
}
