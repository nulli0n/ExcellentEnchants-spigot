package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.manager.player.listener.PlayerBlockPlacedListener;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.CustomDropEnchant;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class EnchantTreasures extends IEnchantChanceTemplate implements BlockBreakEnchant, CustomDropEnchant, ICleanable {

    private final String particleName;
    private final String particleData;
    private final String sound;
    private final Map<Material, Map<Material, Double>> treasures;
    private final Predicate<Block>                     userBlockFilter;

    public static final String ID = "treasures";

    public EnchantTreasures(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.LOWEST);

        this.particleName = cfg.getString("Settings.Particle.Name", Particle.REDSTONE.name());
        this.particleData = cfg.getString("Settings.Particle.Data", "200,180,0");
        this.sound = cfg.getString("Settings.Sound", Sound.BLOCK_NOTE_BLOCK_BELL.name());
        this.treasures = new HashMap<>();
        for (String sFromArray : cfg.getSection("Settings.Treasures")) {
            for (String sFrom : sFromArray.split(",")) {
                Material mFrom = Material.getMaterial(sFrom.toUpperCase());
                if (mFrom == null) {
                    plugin.error("[Treasures] Invalid source material '" + sFrom + "' !");
                    continue;
                }
                Map<Material, Double> treasuresList = new HashMap<>();

                for (String sTo : cfg.getSection("Settings.Treasures." + sFromArray)) {
                    Material mTo = Material.getMaterial(sTo.toUpperCase());
                    if (mTo == null) {
                        plugin.error("[Treasures] Invalid result material '" + sTo + "' for '" + sFromArray + "' !");
                        continue;
                    }

                    double tChance = cfg.getDouble("Settings.Treasures." + sFromArray + "." + sTo);
                    treasuresList.put(mTo, tChance);
                }
                this.treasures.put(mFrom, treasuresList);
            }
        }

        NexEngine.get().getPlayerManager().enableUserBlockListening();
        PlayerBlockPlacedListener.BLOCK_FILTERS.add(this.userBlockFilter = (block) -> {
           return this.getTreasure(block) != null;
        });
    }

    @Override
    public void clear() {
        PlayerBlockPlacedListener.BLOCK_FILTERS.remove(this.userBlockFilter);
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();
        cfg.remove("Settings.Particle_Effect");
        cfg.addMissing("Settings.Particle.Name", Particle.REDSTONE.name());
        cfg.addMissing("Settings.Particle.Data", "200,180,0");
        cfg.addMissing("Settings.Sound", Sound.BLOCK_NOTE_BLOCK_BELL.name());
    }

    @Override
    @NotNull
    public FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.PICKAXE, FitItemType.AXE, FitItemType.SHOVEL};
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    @NotNull
    public List<ItemStack> getCustomDrops(@NotNull Player player, @NotNull ItemStack item, @NotNull Block block, int level) {
        ItemStack drop = this.getTreasure(block);
        if (PlayerBlockPlacedListener.isUserPlaced(block) || drop == null) return Collections.emptyList();
        return Collections.singletonList(drop);
    }

    @Override
    public boolean isEventMustHaveDrops() {
        return false;
    }

    @Nullable
    public final ItemStack getTreasure(@NotNull Block block) {
        Map<Material, Double> treasures = this.treasures.get(block.getType());
        if (treasures == null) return null;

        Material mat = Rnd.get(treasures);
        return mat != null && !mat.isAir() ? new ItemStack(mat) : null;
    }

    public void playEffect(@NotNull Block block) {
        Location location = LocationUtil.getCenter(block.getLocation());
        MessageUtil.sound(location, this.sound);
        EffectUtil.playEffect(location, this.particleName, this.particleData, 0.2f, 0.2f, 0.2f, 0.12f, 20);
    }


    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        Block block = e.getBlock();
        if (!this.isEnchantmentAvailable(player)) return false;
        if (EnchantTelekinesis.isDropHandled(block)) return false;
        if (this.isEventMustHaveDrops() && !e.isDropItems()) return false;
        if (PlayerBlockPlacedListener.isUserPlaced(block)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(player)) return false;

        Location location = LocationUtil.getCenter(block.getLocation());
        List<ItemStack> drops = this.getCustomDrops(player, item, block, level);
        if (drops.isEmpty()) return false;

        drops.forEach(itemDrop -> block.getWorld().dropItem(location, itemDrop));
        this.playEffect(block);
        return true;
    }
}
