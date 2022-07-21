package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.manager.player.blocktracker.PlayerBlockTracker;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantDropContainer;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CustomDropEnchant;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class EnchantTreasures extends IEnchantChanceTemplate implements CustomDropEnchant, ICleanable {

    private final String particleName;
    private final String particleData;
    private final Sound sound;
    private final Map<Material, Map<Material, Double>> treasures;
    private final Predicate<Block>                     blockTracker;

    public static final String ID = "treasures";

    public EnchantTreasures(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);

        this.particleName = cfg.getString("Settings.Particle.Name", Particle.REDSTONE.name());
        this.particleData = cfg.getString("Settings.Particle.Data", "200,180,0");
        this.sound = cfg.getEnum("Settings.Sound", Sound.class, Sound.BLOCK_NOTE_BLOCK_BELL);
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

        PlayerBlockTracker.initialize();
        PlayerBlockTracker.BLOCK_FILTERS.add(this.blockTracker = (block) -> {
           return this.getTreasure(block.getType()) != null;
        });
    }

    @Override
    public void clear() {
        PlayerBlockTracker.BLOCK_FILTERS.remove(this.blockTracker);
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
    public void handleDrop(@NotNull EnchantDropContainer e, @NotNull Player player, @NotNull ItemStack item, int level) {
        BlockDropItemEvent parent = e.getParent();
        Block block = parent.getBlockState().getBlock();
        if (!this.isEnchantmentAvailable(player)) return;
        if (PlayerBlockTracker.isTracked(block)) return;
        if (!this.checkTriggerChance(level)) return;
        if (!this.takeCostItem(player)) return;

        ItemStack treasure = this.getTreasure(parent.getBlockState().getType());
        if (treasure == null) return;

        e.getDrop().add(treasure);
        this.playEffect(block);
    }

    @Nullable
    public final ItemStack getTreasure(@NotNull Material type) {
        Map<Material, Double> treasures = this.treasures.get(type);
        if (treasures == null) return null;

        Material mat = Rnd.get(treasures);
        return mat != null && !mat.isAir() ? new ItemStack(mat) : null;
    }

    public void playEffect(@NotNull Block block) {
        Location location = LocationUtil.getCenter(block.getLocation());
        MessageUtil.sound(location, this.sound);
        EffectUtil.playEffect(location, this.particleName, this.particleData, 0.2f, 0.2f, 0.2f, 0.12f, 20);
    }
}
