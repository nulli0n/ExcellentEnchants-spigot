package su.nightexpress.excellentenchants.enchantment.universal;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.DurabilityEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.sound.VanillaSound;

import java.nio.file.Path;

public class RestoreEnchant extends GameEnchantment implements DurabilityEnchant {

    private Modifier amount;

    public RestoreEnchant(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager, @NotNull Path file, @NotNull EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(20, 5));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.amount = Modifier.load(config, "Restore.Amount",
            Modifier.addictive(15).perLevel(5).capacity(100),
            "Amount of durability (in percent of item max) to be restored.");

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getAmount(level)));
    }

    public double getAmount(int level) {
        return this.amount.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getItemDamagePriority() {
        return EnchantPriority.MONITOR;
    }

    @Override
    public boolean onItemDamage(@NotNull PlayerItemDamageEvent event, @NotNull Player player, @NotNull ItemStack itemStack, int level) {
        if (!(itemStack.getItemMeta() instanceof Damageable damageable)) return false;

        int damage = event.getDamage();
        int maxDurability = itemStack.getType().getMaxDurability();
        if (damageable.getDamage() + damage < maxDurability) return false;

        event.setCancelled(true);

        double damagePercent = 100D - this.getAmount(level);
        int restoredDamage = (int) (maxDurability * (damagePercent / 100D));

        damageable.setDamage(restoredDamage);
        damageable.removeEnchant(this.getBukkitEnchantment());
        itemStack.setItemMeta(damageable);

        if (this.hasVisualEffects()) {
            VanillaSound.of(Sound.ITEM_TOTEM_USE).play(event.getPlayer());
        }
        return true;
    }
}
