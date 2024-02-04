package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.FishHook;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.values.UniParticle;
import su.nexmedia.engine.utils.values.UniSound;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

public class CurseOfDrownedEnchant extends ExcellentEnchant implements FishingEnchant, Chanced {

    public static final String ID = "curse_of_drowned";

    private ChanceImplementation chanceImplementation;

    public CurseOfDrownedEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to fish up a Drowned Zombie.");
        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(0D);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "5.0 + " + Placeholders.ENCHANTMENT_LEVEL);
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.FISHING_ROD;
    }

    @Override
    public boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return false;
        if (!this.checkTriggerChance(level)) return false;

        FishHook hook = event.getHook();
        Drowned drowned = hook.getWorld().spawn(hook.getLocation(), Drowned.class);
        hook.setHookedEntity(drowned);
        hook.pullHookedEntity();

        event.setCancelled(true);

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.WATER_SPLASH).play(hook.getLocation(), 0.5, 0.1, 50);
            UniSound.of(Sound.ENTITY_DROWNED_AMBIENT).play(event.getPlayer());
        }
        return true;
    }
}
