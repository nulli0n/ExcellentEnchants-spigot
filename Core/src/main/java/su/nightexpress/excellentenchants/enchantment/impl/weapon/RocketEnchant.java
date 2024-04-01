package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.*;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.wrapper.UniSound;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class RocketEnchant extends AbstractEnchantmentData implements ChanceData, CombatEnchant {

    public static final String ID = "rocket";

    private Modifier           fireworkPower;
    private ChanceSettingsImpl chanceSettings;

    public RocketEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to launch your enemy into the space.");
        this.setMaxLevel(3);
        this.setRarity(Rarity.RARE);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.multiply(2, 1, 1, 100));

        this.fireworkPower = Modifier.read(config, "Settings.Firework_Power",
            Modifier.multiply(1, 0.25, 1, 3),
            "Firework power. The more power = the higher fly distance.");
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    public final double getFireworkPower(int level) {
        return this.fireworkPower.getValue(level);
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.WEAPON;
    }

    @NotNull
    @Override
    public EventPriority getAttackPriority() {
        return EventPriority.HIGHEST;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.checkTriggerChance(level)) return false;

        if (victim.isInsideVehicle()) {
            victim.leaveVehicle();
        }

        Firework firework = this.createRocket(victim.getWorld(), victim.getLocation(), level);
        firework.addPassenger(victim);

        UniSound.of(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH).play(victim.getLocation());
        return true;
    }

    @NotNull
    private Firework createRocket(@NotNull World world, @NotNull Location location, int level) {
        Firework firework = (Firework) world.spawnEntity(location, EntityType.FIREWORK);
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

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
