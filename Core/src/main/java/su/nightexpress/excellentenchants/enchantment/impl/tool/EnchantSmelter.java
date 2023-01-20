package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantDropContainer;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;

import java.util.HashMap;
import java.util.Map;

public class EnchantSmelter extends ExcellentEnchant implements Chanced, BlockDropEnchant {

    public static final String ID = "smelter";

    private Sound                   sound;
    private Map<Material, Material> smeltingTable;
    private ChanceImplementation chanceImplementation;

    public EnchantSmelter(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.chanceImplementation = ChanceImplementation.create(this);

        this.sound = JOption.create("Settings.Sound", Sound.class, Sound.BLOCK_LAVA_EXTINGUISH,
            "Sound to play on smelting.",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html").read(cfg);

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
        this.cfg.setComments("Settings.Smelting_Table",
            "Table of Original -> Smelted items.",
            "Syntax: 'Material Source : Material Result'.",
            "Note: Material source is material name of the dropped item, not the broken block!",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html");
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
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
    public boolean onDrop(@NotNull BlockDropItemEvent e, @NotNull EnchantDropContainer dropContainer, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (e.getBlockState() instanceof Container) return false;
        if (!this.isAvailableToUse(player)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (e.getItems().stream().noneMatch(drop -> this.isSmeltable(drop.getItemStack().getType()))) return false;

        e.getItems().forEach(drop -> {
            Material material = this.smeltingTable.get(drop.getItemStack().getType());
            if (material != null) drop.getItemStack().setType(material);
        });

        Block block = e.getBlockState().getBlock();
        if (this.hasVisualEffects()) {
            Location location = LocationUtil.getCenter(block.getLocation(), true);
            MessageUtil.sound(location, this.sound);
            EffectUtil.playEffect(location, Particle.FLAME, "", 0.2f, 0.2f, 0.2f, 0.05f, 30);
        }
        return true;
    }

    public boolean isSmeltable(@NotNull Material material) {
        return this.smeltingTable.containsKey(material);
    }
}
