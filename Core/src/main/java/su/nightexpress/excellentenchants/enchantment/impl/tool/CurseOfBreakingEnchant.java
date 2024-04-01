package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.GenericEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.SimpeListener;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class CurseOfBreakingEnchant extends AbstractEnchantmentData implements GenericEnchant, SimpeListener, ChanceData {

    public static final String ID = "curse_of_breaking";

    private Modifier           durabilityAmount;
    private ChanceSettingsImpl chanceSettings;

    public CurseOfBreakingEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to consume extra " + GENERIC_AMOUNT + " durability points.");
        this.setMaxLevel(3);
        this.setRarity(Rarity.COMMON);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.multiply(10, 1, 1, 100));

        this.durabilityAmount = Modifier.read(config, "Settings.Durability_Amount",
            Modifier.add(0, 1, 1, 5),
            "Amount of durability points to be taken from the item.");

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.getDurabilityAmount(level)));
    }

    @Override
    public boolean isCurse() {
        return true;
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    public int getDurabilityAmount(int level) {
        return (int) this.durabilityAmount.getValue(level);
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.BREAKABLE;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDurability(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        if (!this.isAvailableToUse(player)) return;

        ItemStack item = event.getItem();
        int level = EnchantUtils.getLevel(item, this.getEnchantment());

        if (level < 1) return;
        if (!this.checkTriggerChance(level)) return;

        int durabilityAmount = this.getDurabilityAmount(level);
        if (durabilityAmount <= 0) return;

        event.setDamage(event.getDamage() + durabilityAmount);
    }
}
