package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;

public class CurseOfMisfortuneEnchant extends GameEnchantment implements ChanceMeta, BlockBreakEnchant, DeathEnchant {

    public static final String ID = "curse_of_misfortune";

    private boolean dropXP;

    public CurseOfMisfortuneEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.treasure());
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            Lists.newList(ENCHANTMENT_CHANCE + "% chance to have no drops from blocks or mobs."),
            EnchantRarity.RARE,
            3,
            ItemCategories.BREAKABLE,
            ItemCategories.ALL_RANGE_WEAPON,
            Lists.newSet(
                BukkitThing.toString(Enchantment.FORTUNE),
                BukkitThing.toString(Enchantment.LOOTING)
            )
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.multiply(7, 1, 1, 100)));

        this.dropXP = ConfigValue.create("Settings.Drop_XP",
            false,
            "Sets whether or not XP from blocks and mobs can be dropped when enchantment applies."
        ).read(config);
    }

    public boolean isDropXP() {
        return dropXP;
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
