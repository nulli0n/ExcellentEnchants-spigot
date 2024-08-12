package su.nightexpress.excellentenchants.enchantment.impl.universal;

import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
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
import su.nightexpress.nightcore.util.wrapper.UniSound;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_AMOUNT;

public class RestoreEnchant extends GameEnchantment implements GenericEnchant, ChanceMeta, SimpeListener {

    public static final String ID = "restore";

    private Modifier durabilityRestore;

    public RestoreEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.SNOW_COMMON));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            ENCHANTMENT_CHANCE + "% chance to save item from breaking back to " + GENERIC_AMOUNT + "%",
            EnchantRarity.LEGENDARY,
            5,
            ItemCategories.BREAKABLE
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(20, 6, 1, 100)));

        this.durabilityRestore = Modifier.read(config, "Settings.Durability_Restoration",
            Modifier.add(25, 5, 1, 100),
            "Amount of durability (in percent of item max) to be restored.");

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.getDurabilityRestore(level)));
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

        int level = EnchantUtils.getLevel(item, this.getBukkitEnchantment());
        if (level <= 0) return;

        if (this.isOutOfCharges(item)) return;
        if (!this.checkTriggerChance(level)) return;

        event.setCancelled(true);
        this.consumeChargesNoUpdate(item, level);

        double restorePercent = 100D - this.getDurabilityRestore(level);
        int restored = (int) (maxDurability * (restorePercent / 100D));

        damageable.setDamage(restored);
        item.setItemMeta(damageable);
        EnchantUtils.remove(item, this.getBukkitEnchantment());

        if (this.hasVisualEffects()) {
            UniSound.of(Sound.ITEM_TOTEM_USE).play(event.getPlayer());
        }
    }
}
