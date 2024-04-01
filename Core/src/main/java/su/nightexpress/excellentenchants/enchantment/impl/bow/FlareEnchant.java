package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ArrowData;
import su.nightexpress.excellentenchants.api.enchantment.data.ArrowSettings;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ArrowSettingsImpl;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class FlareEnchant extends AbstractEnchantmentData implements ChanceData, ArrowData, BowEnchant {

    public static final String ID = "flare";

    private ChanceSettingsImpl chanceSettings;
    private ArrowSettingsImpl  arrowSettings;

    public FlareEnchant(@NotNull EnchantsPlugin plugin, File file) {
        super(plugin, file);

        this.setDescription(ENCHANTMENT_CHANCE + "% chance to create a torch where arrow lands.");
        this.setMaxLevel(1);
        this.setRarity(Rarity.RARE);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config);
        this.arrowSettings = ArrowSettingsImpl.create(config, UniParticle.of(Particle.FIREWORKS_SPARK));
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.BOW;
    }

    @NotNull
    @Override
    public EventPriority getHitPriority() {
        return EventPriority.HIGHEST;
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return this.chanceSettings;
    }

    @NotNull
    @Override
    public ArrowSettings getArrowSettings() {
        return this.arrowSettings;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!(event.getProjectile() instanceof Arrow)) return false;

        return this.checkTriggerChance(level);
    }

    @Override
    public boolean onHit(@NotNull ProjectileHitEvent event, LivingEntity user, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        Block block = event.getHitBlock();
        if (block == null) return false;

        BlockFace face = event.getHitBlockFace();
        if (face == null || face == BlockFace.DOWN) return false;

        Block relative = block.getRelative(face);
        if (!relative.getType().isAir()) return false;

        if (projectile.getShooter() instanceof Player player) {
            BlockPlaceEvent placeEvent = new BlockPlaceEvent(relative, relative.getState(), block, new ItemStack(Material.TORCH),  player,true, EquipmentSlot.HAND);
            plugin.getPluginManager().callEvent(placeEvent);
            if (placeEvent.isCancelled() || !placeEvent.canBuild()) return false;
        }

        if (face == BlockFace.UP) {
            relative.setType(Material.TORCH);
        }
        else {
            relative.setType(Material.WALL_TORCH);

            Directional directional = (Directional) relative.getBlockData();
            directional.setFacing(face);
            relative.setBlockData(directional, true);
        }

        return false;
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull Projectile projectile, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
