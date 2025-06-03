package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.KillEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Players;

import java.io.File;

public class NimbleEnchant extends GameEnchantment implements KillEnchant {

    private boolean ignorePlayers;

    public NimbleEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.oneHundred());
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.ignorePlayers = ConfigValue.create("Nimble.Ignore_Players",
            false,
            "Sets whether or not to ignore drops from players."
        ).read(config);
    }

    @NotNull
    @Override
    public EnchantPriority getKillPriority() {
        return EnchantPriority.MONITOR;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull Player killer, @NotNull ItemStack weapon, int level) {
        if (this.ignorePlayers && entity instanceof Player) return false;

        event.getDrops().forEach(item -> Players.addItem(killer, item));
        event.getDrops().clear();
        return true;
    }
}
