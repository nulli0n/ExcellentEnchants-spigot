package su.nightexpress.excellentenchants.enchantment.impl.universal;

import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
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
import su.nightexpress.nightcore.util.wrapper.UniSound;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class RestoreEnchant extends AbstractEnchantmentData implements GenericEnchant, ChanceData, SimpeListener {

    public static final String ID = "restore";

    private ChanceSettingsImpl chanceSettings;
    private Modifier           durabilityRestore;

    public RestoreEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to save item from breaking back to " + GENERIC_AMOUNT + "%");
        this.setMaxLevel(5);
        this.setRarity(Rarity.RARE);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(20, 6, 1, 100));

        this.durabilityRestore = Modifier.read(config, "Settings.Durability_Restoration",
            Modifier.add(25, 5, 1, 100),
            "Amount of durability (in percent of item max) to be restored.");

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.getDurabilityRestore(level)));
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
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

        int level = EnchantUtils.getLevel(item, this.getEnchantment());
        if (level <= 0) return;

        if (this.isOutOfCharges(item)) return;
        if (!this.checkTriggerChance(level)) return;

        event.setCancelled(true);
        this.consumeChargesNoUpdate(item, level);

        double restorePercent = 100D - this.getDurabilityRestore(level);
        int restored = (int) (maxDurability * (restorePercent / 100D));

        damageable.setDamage(restored);
        item.setItemMeta(damageable);
        EnchantUtils.remove(item, this.getEnchantment());

        if (this.hasVisualEffects()) {
            UniSound.of(Sound.ITEM_TOTEM_USE).play(event.getPlayer());
        }
    }
}
