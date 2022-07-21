package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.manager.EnchantRegister;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

import java.util.HashMap;
import java.util.Map;

public class EnchantSmelter extends IEnchantChanceTemplate implements BlockDropEnchant {

    public static final String ID = "smelter";

    private final Sound                   sound;
    private final String                  particleName;
    private final String                  particleData;
    private final Map<Material, Material> smeltingTable;

    public EnchantSmelter(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);

        this.sound = cfg.getEnum("Settings.Sound", Sound.class);
        this.particleName = cfg.getString("Settings.Particle.Name", Particle.FLAME.name());
        this.particleData = cfg.getString("Settings.Particle.Data", "");
        this.smeltingTable = new HashMap<>();
        for (String sFrom : cfg.getSection("Settings.Smelting_Table")) {
            Material mFrom = Material.getMaterial(sFrom.toUpperCase());
            if (mFrom == null) {
                plugin.error("[Smelter] Invalid source material '" + sFrom + "' !");
                continue;
            }
            String sTo = cfg.getString("Settings.Smelting_Table." + sFrom, "");
            Material mTo = Material.getMaterial(sTo.toUpperCase());
            if (mTo == null) {
                plugin.error("[Smelter] Invalid result material '" + sTo + "' !");
                continue;
            }
            this.smeltingTable.put(mFrom, mTo);
        }
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.remove("Settings.Particle_Effect");
        cfg.addMissing("Settings.Sound", Sound.BLOCK_LAVA_EXTINGUISH.name());
        cfg.addMissing("Settings.Particle.Name", Particle.FLAME.name());
        cfg.addMissing("Settings.Particle.Data", "");
    }

    @Override
    @NotNull
    public FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.PICKAXE, FitItemType.AXE, FitItemType.SHOVEL};
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(Enchantment.SILK_TOUCH);
        this.addConflict(EnchantRegister.DIVINE_TOUCH);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean use(@NotNull BlockDropItemEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.isEnchantmentAvailable(player)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (e.getItems().stream().noneMatch(drop -> this.isSmeltable(drop.getItemStack().getType()))) return false;
        if (!this.takeCostItem(player)) return false;

        e.getItems().forEach(drop -> {
            Material material = this.smeltingTable.get(drop.getItemStack().getType());
            if (material != null) drop.getItemStack().setType(material);
        });

        Block block = e.getBlockState().getBlock();
        this.playEffect(block);
        return true;
    }

    public boolean isSmeltable(@NotNull Material material) {
        return this.smeltingTable.containsKey(material);
    }

    public void playEffect(@NotNull Block block) {
        Location location = LocationUtil.getCenter(block.getLocation(), true);
        MessageUtil.sound(location, this.sound);
        EffectUtil.playEffect(location, this.particleName, this.particleData, 0.2f, 0.2f, 0.2f, 0.05f, 30);
    }
}
