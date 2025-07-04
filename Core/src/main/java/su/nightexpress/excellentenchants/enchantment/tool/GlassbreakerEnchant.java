package su.nightexpress.excellentenchants.enchantment.tool;

import org.bukkit.Material;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.InteractEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.bukkit.NightSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class GlassbreakerEnchant extends GameEnchantment implements InteractEnchant {

    private static final Set<Material> GLASS = new HashSet<>();

    static {
        BukkitThing.getMaterials().stream().filter(GlassbreakerEnchant::isGlass).forEach(GLASS::add);
    }

    private static boolean isGlass(@NotNull Material material) {
        if (!material.isBlock()) return false;

        String name = BukkitThing.getValue(material);

        return name.endsWith("glass") || name.endsWith("glass_pane") || name.endsWith("stained_glass");
    }

    public GlassbreakerEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @Override
    @NotNull
    public EnchantPriority getInteractPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onInteract(@NotNull PlayerInteractEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        if (event.useItemInHand() == Event.Result.DENY) return false;
        if (event.useInteractedBlock() == Event.Result.DENY) return false;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return false;
        if (!(entity instanceof Player player)) return false;

        Block block = event.getClickedBlock();
        if (block == null) return false;

        Material material = block.getType();
        BlockData blockData = block.getBlockData();
        SoundGroup soundGroup = blockData.getSoundGroup();

        if (!GLASS.contains(material)) return false;
        if (!player.breakBlock(block)) return false;

        UniParticle.blockCrack(material).play(LocationUtil.setCenter3D(block.getLocation()), 0.5, 0.1, 10);
        NightSound.of(soundGroup.getBreakSound()).play(player);
        return true;
    }
}
