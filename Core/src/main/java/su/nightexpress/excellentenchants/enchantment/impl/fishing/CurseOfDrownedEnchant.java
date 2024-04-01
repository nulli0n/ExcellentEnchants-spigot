package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.FishHook;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniParticle;
import su.nightexpress.nightcore.util.wrapper.UniSound;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class CurseOfDrownedEnchant extends AbstractEnchantmentData implements FishingEnchant, ChanceData {

    public static final String ID = "curse_of_drowned";

    private ChanceSettingsImpl chanceSettings;

    public CurseOfDrownedEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to fish up a Drowned Zombie.");
        this.setMaxLevel(5);
        this.setRarity(Rarity.UNCOMMON);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(0, 5, 1, 100));
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.FISHING_ROD;
    }

    @Override
    public boolean isCurse() {
        return true;
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
            UniParticle.of(Particle.WATER_SPLASH).play(hook.getLocation(), 0.75, 0.1, 50);
            UniSound.of(Sound.ENTITY_DROWNED_AMBIENT).play(event.getPlayer());
        }
        return true;
    }
}
