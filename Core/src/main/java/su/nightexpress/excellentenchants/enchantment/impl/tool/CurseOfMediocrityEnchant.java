package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.ItemCategory;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemUtil;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class CurseOfMediocrityEnchant extends AbstractEnchantmentData implements ChanceData, BlockDropEnchant, DeathEnchant {

    public static final String ID = "curse_of_mediocrity";

    private ChanceSettingsImpl chanceSettings;

    public CurseOfMediocrityEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to disenchant item drops.");
        this.setMaxLevel(3);
        this.setRarity(Rarity.UNCOMMON);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.multiply(15, 1, 1, 100));
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.BREAKABLE;
    }

    @Override
    @NotNull
    public ItemCategory[] getItemCategories() {
        return new ItemCategory[] {
            ItemCategory.SWORD, ItemCategory.BOW, ItemCategory.CROSSBOW, ItemCategory.TRIDENT, ItemCategory.TOOL
        };
    }

    @NotNull
    @Override
    public EventPriority getDropPriority() {
        return EventPriority.HIGHEST;
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return this.chanceSettings;
    }

    @Override
    public boolean isCurse() {
        return true;
    }

    @Override
    public boolean onDrop(@NotNull BlockDropItemEvent event, @NotNull LivingEntity player, @NotNull ItemStack item, int level) {
        if (!this.checkTriggerChance(level)) return false;

        event.getItems().forEach(drop -> {
            ItemStack stack = drop.getItemStack();
            EnchantUtils.removeAll(stack);
            drop.setItemStack(stack);
        });

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

    @Override
    public boolean onKill(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull Player killer, ItemStack weapon, int level) {
        if (!this.checkTriggerChance(level)) return false;

        event.getDrops().forEach(stack -> {
            ItemUtil.editMeta(stack, meta -> {
                meta.getEnchants().keySet().forEach(meta::removeEnchant);
            });
        });

        return true;
    }
}
