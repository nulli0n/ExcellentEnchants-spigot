package su.nightexpress.excellentenchants.enchantment.tool;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.EnchantsUtils;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.InteractEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Randomizer;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@NullMarked
public class LumberjackEnchant extends GameEnchantment implements BlockDropEnchant, InteractEnchant {

    private boolean disableOnCrouch;
    private Modifier stripDropChance;

    public LumberjackEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.disableOnCrouch = ConfigValue.create("Lumberjack.Disable_On_Crouch",
            true,
            "Sets whether or not enchantment will have no effect when crouching."
        ).read(config);

        this.stripDropChance = Modifier.load(config, "Lumberjack.Strip_Drop_Chance",
            Modifier.addictive(0.10).perLevel(0.05).capacity(0.30),
            "Chance (between 0.0 and 1.0) to drop the original log when stripping."
        );
    }

    private static boolean isLog(Material material) {
        return Tag.LOGS.isTagged(material);
    }

    private static boolean isStrippedLog(Material material) {
        return material.name().startsWith("STRIPPED_");
    }

    @Override
    public EnchantPriority getDropPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public EnchantPriority getInteractPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onDrop(BlockDropItemEvent event, LivingEntity entity, ItemStack item, int level) {
        if (this.disableOnCrouch && entity instanceof Player player && player.isSneaking()) return false;

        BlockState state = event.getBlockState();
        if (!isLog(state.getType())) return false;

        List<ItemStack> extraDrops = new ArrayList<>();
        for (Item drop : event.getItems()) {
            ItemStack stack = drop.getItemStack();
            if (isLog(stack.getType())) {
                int fortuneAmount = this.getFortuneAmount(level);
                if (fortuneAmount > 0) {
                    ItemStack extra = stack.clone();
                    extra.setAmount(fortuneAmount * stack.getAmount());
                    extraDrops.add(extra);
                }
            }
        }

        if (extraDrops.isEmpty()) return false;

        extraDrops.forEach(extra -> EnchantsUtils.populateResource(event, extra));
        return true;
    }

    private int getFortuneAmount(int level) {
        int random = Randomizer.nextInt(level + 2);
        if (random > 1) {
            return random - 1;
        }
        return 0;
    }

    @Override
    public boolean onInteract(PlayerInteractEvent event, LivingEntity entity, ItemStack item, int level) {
        if (!(entity instanceof Player)) return false;
        if (event.getHand() != EquipmentSlot.HAND) return false;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return false;

        Block block = event.getClickedBlock();
        if (block == null) return false;

        Material originalMaterial = block.getType();
        if (!isLog(originalMaterial) || isStrippedLog(originalMaterial)) return false;

        Location location = block.getLocation();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Block currentBlock = location.getBlock();
            if (isStrippedLog(currentBlock.getType())) {
                double chance = this.stripDropChance.getValue(level);
                if (Math.random() < chance) {
                    ItemStack drop = new ItemStack(originalMaterial, 1);
                    currentBlock.getWorld().dropItemNaturally(currentBlock.getLocation().add(0.5, 0.5, 0.5), drop);
                    
                    if (this.hasVisualEffects()) {
                        UniParticle.of(Particle.HAPPY_VILLAGER).play(
                            currentBlock.getLocation().add(0.5, 1.0, 0.5),
                            0.2, 0.2, 5
                        );
                    }
                }
            }
        }, 1L);

        return true;
    }
}
