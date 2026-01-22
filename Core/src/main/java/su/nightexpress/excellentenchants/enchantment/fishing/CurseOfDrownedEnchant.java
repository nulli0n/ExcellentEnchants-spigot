package su.nightexpress.excellentenchants.enchantment.fishing;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.FishHook;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.sound.VanillaSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

public class CurseOfDrownedEnchant extends GameEnchantment implements FishingEnchant {

    public CurseOfDrownedEnchant(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager, @NotNull Path file, @NotNull EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(5, 5));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @Override
    @NotNull
    public EnchantPriority getFishingPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return false;

        FishHook hook = event.getHook();
        Drowned drowned = hook.getWorld().spawn(hook.getLocation(), Drowned.class);
        hook.setHookedEntity(drowned);
        hook.pullHookedEntity();

        event.setCancelled(true);

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.UNDERWATER).play(hook.getLocation(), 0.75, 0.1, 50);
            VanillaSound.of(Sound.ENTITY_DROWNED_AMBIENT).play(event.getPlayer());
        }
        return true;
    }
}
