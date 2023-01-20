package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantCurseOfBreaking extends ExcellentEnchant implements Chanced {

    public static final String ID = "curse_of_breaking";
    public static final String PLACEHOLDER_DURABILITY_AMOUNT = "%enchantment_durability_amount%";

    private EnchantScaler durabilityAmount;
    private ChanceImplementation chanceImplementation;

    public EnchantCurseOfBreaking(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.chanceImplementation = ChanceImplementation.create(this);
        this.durabilityAmount = EnchantScaler.read(this, "Settings.Durability_Amount", Placeholders.ENCHANTMENT_LEVEL,
            "Amount of durability points to be taken from the item.");
    }

    @Override
    public boolean isCursed() {
        return true;
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    public int getDurabilityAmount(int level) {
        return (int) this.durabilityAmount.getValue(level);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str)
            .replace(PLACEHOLDER_DURABILITY_AMOUNT, NumberUtil.format(this.getDurabilityAmount(level)));
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDurability(PlayerItemDamageEvent e) {
        Player player = e.getPlayer();
        if (!this.isAvailableToUse(player)) return;

        ItemStack item = e.getItem();
        int level = EnchantManager.getEnchantmentLevel(item, this);

        if (level < 1) return;
        if (!this.checkTriggerChance(level)) return;

        int durabilityAmount = this.getDurabilityAmount(level);
        if (durabilityAmount <= 0) return;

        e.setDamage(e.getDamage() + durabilityAmount);
    }
}
