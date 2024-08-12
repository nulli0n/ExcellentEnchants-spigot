package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.type.GenericEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.SimpeListener;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_AMOUNT;

public class CurseOfBreakingEnchant extends GameEnchantment implements GenericEnchant, SimpeListener, ChanceMeta {

    public static final String ID = "curse_of_breaking";

    private Modifier durabilityAmount;

    public CurseOfBreakingEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.treasure());
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            ENCHANTMENT_CHANCE + "% chance to consume extra " + GENERIC_AMOUNT + " durability points.",
            EnchantRarity.COMMON,
            3,
            ItemCategories.BREAKABLE
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.multiply(10, 1, 1, 100)));

        this.durabilityAmount = Modifier.read(config, "Settings.Durability_Amount",
            Modifier.add(0, 1, 1, 5),
            "Amount of durability points to be taken from the item.");

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.getDurabilityAmount(level)));
    }

    @Override
    public boolean isCurse() {
        return true;
    }

    public int getDurabilityAmount(int level) {
        return (int) this.durabilityAmount.getValue(level);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDurability(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        if (!this.isAvailableToUse(player)) return;

        ItemStack item = event.getItem();
        int level = EnchantUtils.getLevel(item, this.getBukkitEnchantment());

        if (level < 1) return;
        if (!this.checkTriggerChance(level)) return;

        int durabilityAmount = this.getDurabilityAmount(level);
        if (durabilityAmount <= 0) return;

        event.setDamage(event.getDamage() + durabilityAmount);
    }
}
