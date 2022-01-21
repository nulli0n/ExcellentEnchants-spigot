package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.CustomDropEnchant;
import su.nightexpress.excellentenchants.manager.EnchantRegister;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantSmelter extends IEnchantChanceTemplate implements BlockBreakEnchant, CustomDropEnchant {

    private final String                  sound;
    private final String                  particleName;
    private final String                  particleData;
    private final Map<Material, Material> smeltingTable;

    public static final String ID = "smelter";

    public EnchantSmelter(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);

        this.sound = cfg.getString("Settings.Sound", "");
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
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(Enchantment.SILK_TOUCH);
        this.addConflict(EnchantRegister.DIVINE_TOUCH);
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
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    @NotNull
    public List<ItemStack> getCustomDrops(@NotNull Player player, @NotNull ItemStack item, @NotNull Block block, int level) {
        if (block.getState() instanceof Container) return Collections.emptyList();

        List<ItemStack> drops = plugin.getNMS().getBlockDrops(block, player, item);
        return this.smelt(drops);
    }

    public boolean isSmeltable(@NotNull Material material) {
        return this.smeltingTable.containsKey(material);
    }

    @NotNull
    public List<ItemStack> smelt(@NotNull List<ItemStack> drops) {
        return drops.stream().peek(drop -> {
            Material material = this.smeltingTable.get(drop.getType());
            if (material != null) drop.setType(material);
        }).toList();
    }

    public void playEffect(@NotNull Block block) {
        Location location = LocationUtil.getCenter(block.getLocation(), true);
        MessageUtil.sound(location, this.sound);
        EffectUtil.playEffect(location, this.particleName, this.particleData, 0.2f, 0.2f, 0.2f, 0.05f, 30);
    }

    @Override
    public boolean isEventMustHaveDrops() {
        return true;
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        Block block = e.getBlock();
        if (!this.isEnchantmentAvailable(player)) return false;
        if (EnchantTelekinesis.isDropHandled(block)) return false;
        if (this.isEventMustHaveDrops() && !e.isDropItems()) return false;
        if (!this.checkTriggerChance(level)) return false;

        List<ItemStack> defaults = plugin.getNMS().getBlockDrops(block, player, item);
        List<ItemStack> custom = this.getCustomDrops(player, item, block, level);
        if (custom.isEmpty() || custom.containsAll(defaults)) return false;
        if (!this.takeCostItem(player)) return false;

        e.setDropItems(false);

        World world = block.getWorld();
        Location location = LocationUtil.getCenter(block.getLocation(), true);

        custom.forEach(itemSmelt -> world.dropItem(location, itemSmelt));
        this.playEffect(block);
        return true;
    }
}
