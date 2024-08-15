package su.nightexpress.excellentenchants.enchantment.impl.armor;


import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.type.GenericEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.SimpeListener;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;

import java.io.File;

public class ReboundEnchant extends GameEnchantment implements GenericEnchant, SimpeListener {

    private Modifier modifier;
    private Modifier capacity;

    public ReboundEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.treasure(TradeType.SWAMP_COMMON));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            Lists.newList("Effect of landing on a slime block."),
            EnchantRarity.LEGENDARY,
            3,
            ItemCategories.BOOTS,
            null,
            Lists.newSet(BukkitThing.toString(Enchantment.FEATHER_FALLING))
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.modifier = Modifier.read(config, "Settings.Modifier.Step",
            Modifier.add(0, 0.1, 0),
            "Sets bounce power modifier based on fall distance.",
            "Greater value = greater rebound."
        );

        this.capacity = Modifier.read(config, "Settings.Modifier.Capacity",
            Modifier.add(0.75, 0.15, 0),
            "Sets maximal bounce power modifier value.",
            "Greater value = greater rebound."
        );
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFall(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity instanceof Player player && player.isSneaking()) return;
        if (!this.isAvailableToUse(entity)) return;

        ItemStack boots = EnchantUtils.getEquipped(entity, EquipmentSlot.FEET);
        if (boots == null) return;

        int level = EnchantUtils.getLevel(boots, this.getBukkitEnchantment());
        if (level <= 0) return;
        if (this.isOutOfCharges(boots)) return;

        event.setCancelled(true);

        this.bounceUp(entity, event.getDamage(), level);
        this.consumeCharges(boots, level);
    }

    private void bounceUp(@NotNull LivingEntity entity, double power, int level) {
        double limit = this.capacity.getValue(level);
        double step = this.modifier.getValue(level);
        double modifier = Math.min(limit, power * step);

        Vector velocity = entity.getVelocity();
        if (velocity.getY() < 0D) {
            entity.setVelocity(velocity.setY(modifier));
        }
    }
}
