package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantCurseOfBreaking extends IEnchantChanceTemplate {

    private Scaler durabilityAmount;

    public static final String ID = "curse_of_breaking";
    public static final String PLACEHOLDER_DURABILITY_AMOUNT = "%enchantment_durability_amount%";

    public EnchantCurseOfBreaking(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.durabilityAmount = new EnchantScaler(this, "Settings.Durability_Amount");
    }

    @Override
    public boolean isCursed() {
        return true;
    }

    public int getDurabilityAmount(int level) {
        return (int) this.durabilityAmount.getValue(level);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
        .replace(PLACEHOLDER_DURABILITY_AMOUNT, NumberUtil.format(this.getDurabilityAmount(level)))
        );
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDurability(PlayerItemDamageEvent e) {
        Player player = e.getPlayer();
        if (!this.isEnchantmentAvailable(player)) return;

        ItemStack item = e.getItem();
        int level = EnchantManager.getItemEnchantLevel(item, this);

        if (level < 1) return;
        if (!this.checkTriggerChance(level)) return;
        if (!this.takeCostItem(player)) return;

        int durabilityAmount = this.getDurabilityAmount(level);
        if (durabilityAmount <= 0) return;

        e.setDamage(e.getDamage() + durabilityAmount);
    }
}
