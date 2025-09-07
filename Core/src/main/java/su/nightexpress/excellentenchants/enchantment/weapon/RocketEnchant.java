package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.AttackEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.bukkit.NightSound;
import su.nightexpress.nightcore.util.random.Rnd;

import java.io.File;

public class RocketEnchant extends GameEnchantment implements AttackEnchant {

    private Modifier fireworkPower;

    public RocketEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(0, 2));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.fireworkPower = Modifier.load(config, "Rocket.Firework_Power",
            Modifier.addictive(1).perLevel(0.25).capacity(3D),
            "Firework power. The more power = the higher fly distance.");
    }

    public final double getFireworkPower(int level) {
        return this.fireworkPower.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (victim.isInsideVehicle()) {
            victim.leaveVehicle();
        }

        Location location = victim.getLocation();
        this.plugin.runTask(location, () -> {
            Firework firework = this.createRocket(victim.getWorld(), location, level);
            this.plugin.runTask(victim, () -> firework.addPassenger(victim));
        });

        NightSound.of(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH).play(victim.getLocation());
        return true;
    }

    @NotNull
    private Firework createRocket(@NotNull World world, @NotNull Location location, int level) {
        Firework firework = (Firework) world.spawnEntity(location, EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect.Type type = Rnd.get(FireworkEffect.Type.values());
        Color color = Color.fromBGR(Rnd.nextInt(255), Rnd.nextInt(255), Rnd.nextInt(255));
        Color fade = Color.fromBGR(Rnd.nextInt(255), Rnd.nextInt(255), Rnd.nextInt(255));
        FireworkEffect effect = FireworkEffect.builder().flicker(Rnd.nextBoolean()).withColor(color).withFade(fade).with(type).trail(Rnd.nextBoolean()).build();
        meta.addEffect(effect);
        meta.setPower((int) this.getFireworkPower(level));
        firework.setFireworkMeta(meta);
        return firework;
    }
}
