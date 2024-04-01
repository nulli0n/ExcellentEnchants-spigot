package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
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
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class CurseOfMisfortuneEnchant extends AbstractEnchantmentData implements ChanceData, BlockBreakEnchant, DeathEnchant {

    public static final String ID = "curse_of_misfortune";

    private boolean            dropXP;
    private ChanceSettingsImpl chanceImplementation;

    public CurseOfMisfortuneEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to have no drops from blocks or mobs.");
        this.setMaxLevel(3);
        this.setRarity(Rarity.UNCOMMON);
        this.setConflicts(
            Enchantment.LOOT_BONUS_BLOCKS.getKey().getKey(),
            Enchantment.LOOT_BONUS_MOBS.getKey().getKey()
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceImplementation = ChanceSettingsImpl.create(config, Modifier.multiply(7, 1, 1, 100));

        this.dropXP = ConfigValue.create("Settings.Drop_XP",
            false,
            "Sets whether or not XP from blocks and mobs can be dropped when enchantment applies."
        ).read(config);
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceImplementation;
    }

    public boolean isDropXP() {
        return dropXP;
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
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.BREAKABLE;
    }

    @NotNull
    @Override
    public EventPriority getBreakPriority() {
        return EventPriority.HIGHEST;
    }

    @NotNull
    @Override
    public EventPriority getKillPriority() {
        return EventPriority.HIGH;
    }

    @Override
    public boolean isCurse() {
        return true;
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent event, @NotNull LivingEntity player, @NotNull ItemStack item, int level) {
        if (!this.checkTriggerChance(level)) return false;

        event.setDropItems(false);
        if (!this.isDropXP()) event.setExpToDrop(0);
        return true;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull Player killer, ItemStack weapon, int level) {
        if (!this.checkTriggerChance(level)) return false;

        event.getDrops().clear();
        if (!this.isDropXP()) event.setDroppedExp(0);
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
}
