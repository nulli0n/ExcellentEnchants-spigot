package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
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
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ArrowMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.ArrowEffects;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;

public class FlareEnchant extends GameEnchantment implements ChanceMeta, ArrowMeta, BowEnchant {

    public static final String ID = "flare";

    public FlareEnchant(@NotNull EnchantsPlugin plugin, File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.SNOW_COMMON));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            ENCHANTMENT_CHANCE + "% chance to create a torch where arrow lands.",
            EnchantRarity.LEGENDARY,
            1,
            ItemCategories.BOWS
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config));
        this.meta.setArrowEffects(ArrowEffects.create(config, UniParticle.of(Particle.ELECTRIC_SPARK)));
    }

    @NotNull
    @Override
    public EventPriority getHitPriority() {
        return EventPriority.HIGHEST;
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
