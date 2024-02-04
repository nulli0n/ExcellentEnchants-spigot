package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.EventListener;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.GenericEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

public class CurseOfBreakingEnchant extends ExcellentEnchant implements GenericEnchant, EventListener, Chanced {

    public static final String ID = "curse_of_breaking";
    public static final String PLACEHOLDER_DURABILITY_AMOUNT = "%enchantment_durability_amount%";

    private EnchantScaler durabilityAmount;
    private ChanceImplementation chanceImplementation;

    public CurseOfBreakingEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to consume extra " + PLACEHOLDER_DURABILITY_AMOUNT + " durability points.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0D);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "10.0 * " + Placeholders.ENCHANTMENT_LEVEL);
        this.durabilityAmount = EnchantScaler.read(this, "Settings.Durability_Amount",
            Placeholders.ENCHANTMENT_LEVEL,
            "Amount of durability points to be taken from the item.");

        this.addPlaceholder(PLACEHOLDER_DURABILITY_AMOUNT, level -> NumberUtil.format(this.getDurabilityAmount(level)));
    }

    @Override
    public boolean isCurse() {
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
        int level = EnchantUtils.getLevel(item, this.getBackend());

        if (level < 1) return;
        if (!this.checkTriggerChance(level)) return;

        int durabilityAmount = this.getDurabilityAmount(level);
        if (durabilityAmount <= 0) return;

        event.setDamage(event.getDamage() + durabilityAmount);
    }
}
