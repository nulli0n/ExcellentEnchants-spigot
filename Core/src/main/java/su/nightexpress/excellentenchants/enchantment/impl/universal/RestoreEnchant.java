package su.nightexpress.excellentenchants.enchantment.impl.universal;

import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.EventListener;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.values.UniSound;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.GenericEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

public class RestoreEnchant extends ExcellentEnchant implements GenericEnchant, Chanced, EventListener {

    public static final String ID                             = "restore";
    public static final String PLACEHOLDER_DURABILITY_RESTORE = "%durability_restore%";

    private ChanceImplementation chanceImplementation;
    private EnchantScaler durabilityRestore;

    public RestoreEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to save item from breaking back to " + PLACEHOLDER_DURABILITY_RESTORE + "%");
        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(0.6);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

        this.chanceImplementation = ChanceImplementation.create(this,
            "35.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 8");

        this.durabilityRestore = EnchantScaler.read(this, "Settings.Durability_Restoration",
            "25.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 5",
            "Amount of durability (in percent of item max) to be restored.");

        this.addPlaceholder(PLACEHOLDER_DURABILITY_RESTORE, level -> NumberUtil.format(this.getDurabilityRestore(level)));
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }

    public double getDurabilityRestore(int level) {
        return this.durabilityRestore.getValue(level);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (!(item.getItemMeta() instanceof Damageable damageable)) return;

        int damage = event.getDamage();
        int maxDurability = item.getType().getMaxDurability();
        if (damageable.getDamage() + damage < maxDurability) return;

        int level = EnchantUtils.getLevel(item, this);
        if (level <= 0) return;

        if (this.isOutOfCharges(item)) return;
        if (!this.checkTriggerChance(level)) return;

        event.setCancelled(true);
        this.consumeChargesNoUpdate(item, level);

        double restorePercent = 100D - this.getDurabilityRestore(level);
        int restored = (int) (maxDurability * (restorePercent / 100D));

        damageable.setDamage(restored);
        item.setItemMeta(damageable);
        EnchantUtils.remove(item, this);

        if (this.hasVisualEffects()) {
            UniSound.of(Sound.ITEM_TOTEM_USE).play(event.getPlayer());
        }
    }
}
