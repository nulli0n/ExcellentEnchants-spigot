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
import su.nexmedia.engine.utils.random.Rnd;
import su.nexmedia.engine.utils.values.UniSound;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

public class EnchantRocket extends ExcellentEnchant implements Chanced, CombatEnchant {

    public static final String ID = "rocket";

    private EnchantScaler fireworkPower;
    private ChanceImplementation chanceImplementation;

    public EnchantRocket(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to launch your enemy into the space.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.5);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "4.0 + " + Placeholders.ENCHANTMENT_LEVEL);
        this.fireworkPower = EnchantScaler.read(this, "Settings.Firework_Power",
            Placeholders.ENCHANTMENT_LEVEL + " * 0.25",
            "Firework power. The more power = the higher fly distance.");
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
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

        Firework firework = this.createRocket(victim.getLocation(), level);
        firework.addPassenger(victim);

        UniSound.of(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH).play(victim.getLocation());
        return true;
    }

    @NotNull
    private Firework createRocket(@NotNull Location location, int level) {
        World world = location.getWorld();
        if (world == null) {
            throw new IllegalStateException("World is null!");
        }

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
