package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.lang.LangMessage;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantDropContainer;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CustomDropEnchant;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class EnchantTelekinesis extends IEnchantChanceTemplate implements CustomDropEnchant {

    private LangMessage messageDropReceived;
    private String      messageItemName;
    private String      messageItemSeparator;

    public static final String ID = "telekinesis";

    public EnchantTelekinesis(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.LOWEST);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.messageDropReceived = new LangMessage(plugin, cfg.getString("Settings.Message.Drop_Received", ""));
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
    public void handleDrop(@NotNull EnchantDropContainer e, @NotNull Player player, @NotNull ItemStack item, int level) {
        BlockDropItemEvent parent = e.getParent();
        Block block = parent.getBlockState().getBlock();

        if (!this.isEnchantmentAvailable(player)) return;
        //if (block.getState() instanceof Container) return;
        if (!this.checkTriggerChance(level)) return;

        List<ItemStack> drops = new ArrayList<>();
        drops.addAll(parent.getItems().stream().map(Item::getItemStack).toList());
        drops.addAll(e.getDrop());
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

        e.getDrop().clear();
        parent.getItems().clear();
    }
}
