package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.config.LangMessage;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.CustomDropEnchant;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.excellentenchants.manager.EnchantRegister;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class EnchantTelekinesis extends IEnchantChanceTemplate implements BlockBreakEnchant {

    public static final String META_BLOCK_DROP_HANDLER = "telekinesis_drop_handler";

    private final LangMessage messageDropReceived;
    private final String      messageItemName;
    private final String messageItemSeparator;

    public static final String ID = "telekinesis";

    public EnchantTelekinesis(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.HIGHEST);

        this.messageDropReceived = new LangMessage(plugin.lang(), cfg.getString("Settings.Message.Drop_Received", ""));
        this.messageItemName = StringUtil.color(cfg.getString("Settings.Message.Item_Name", "&7x%item_amount% &f%item_name%"));
        this.messageItemSeparator = StringUtil.color(cfg.getString("Settings.Message.Item_Separator", "&7, "));
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.remove("Settings.Radius");
        cfg.remove("Settings.Power");

        cfg.addMissing("Settings.Message.Drop_Received", "{message: ~type: ACTION_BAR; ~prefix: false;}%items%");
        cfg.addMissing("Settings.Message.Item_Name", "&7x%item_amount% &f%item_name%");
        cfg.addMissing("Settings.Message.Item_Separator", "&7, ");
    }

    public static boolean isDropHandled(@NotNull Block block) {
        return block.hasMetadata(META_BLOCK_DROP_HANDLER);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return super.replacePlaceholders(level);
    }

    @Override
    @NotNull
    public FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.TOOL};
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.isEnchantmentAvailable(player)) return false;
        if (e.getBlock().getState() instanceof Container) return false;
        if (!e.isDropItems()) return false;
        if (!this.checkTriggerChance(level)) return false;

        EnchantCurseOfMisfortune curseOfMisfortune = EnchantRegister.CURSE_OF_MISFORTUNE;
        if (curseOfMisfortune != null && item.containsEnchantment(curseOfMisfortune)) {
            if (curseOfMisfortune.use(e, player, item, level)) {
                return false;
            }
        }

        Block block = e.getBlock();
        List<ItemStack> drops = new ArrayList<>(plugin.getNMS().getBlockDrops(block, player, item));

        // Check inventory space.
        if (drops.stream().anyMatch(itemDrop -> PlayerUtil.countItemSpace(player, itemDrop) == 0)) return false;

        // Tell other enchantments that block drops are handled by Telekinesis.
        block.setMetadata(META_BLOCK_DROP_HANDLER, new FixedMetadataValue(plugin, true));

        for (Map.Entry<CustomDropEnchant, Integer> entry : EnchantManager.getItemCustomEnchants(item, CustomDropEnchant.class).entrySet()) {
            CustomDropEnchant dropEnchant = entry.getKey();
            int dropLevel = entry.getValue();
            if (dropEnchant.isEventMustHaveDrops() && !e.isDropItems()) continue;

            if (dropEnchant instanceof IEnchantChanceTemplate chanceEnchant) {
                if (!chanceEnchant.checkTriggerChance(dropLevel)) continue;
            }
            if (dropEnchant instanceof EnchantSilkChest && block.getState() instanceof Chest) {
                drops.removeIf(drop -> drop.getType() == block.getType());
            }
            if (dropEnchant instanceof EnchantSmelter smelter) {
                boolean isSmelted = drops.stream().anyMatch(drop -> smelter.isSmeltable(drop.getType()));
                smelter.smelt(drops);
                if (isSmelted) smelter.playEffect(block);
                continue; // Do not add smelted items twice, only replace current ones.
            }
            if (dropEnchant instanceof EnchantTreasures treasures) {
                if (treasures.getTreasure(block) != null) {
                    treasures.playEffect(block);
                }
            }

            drops.addAll(dropEnchant.getCustomDrops(player, item, block, dropLevel));
        }
        drops.removeIf(Objects::isNull);

        StringBuilder builder = new StringBuilder();
        drops.forEach(drop -> {
            PlayerUtil.addItem(player, drop);

            if (!builder.isEmpty()) builder.append(this.messageItemSeparator);
            builder.append(this.messageItemName
                    .replace("%item_name%", ItemUtil.getItemName(drop))
                    .replace("%item_amount%", String.valueOf(drop.getAmount())));
        });
        this.messageDropReceived.replace("%items%", builder.toString()).send(player);

        e.setDropItems(false);
        plugin.getServer().getScheduler().runTask(plugin, c -> {
            block.removeMetadata(META_BLOCK_DROP_HANDLER, plugin);
        });
        return true;
    }
}
