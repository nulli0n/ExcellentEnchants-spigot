package su.nightexpress.excellentenchants.tooltip;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.tooltip.TooltipController;
import su.nightexpress.excellentenchants.api.tooltip.TooltipHandler;
import su.nightexpress.excellentenchants.tooltip.format.ChargesFormat;
import su.nightexpress.excellentenchants.tooltip.handler.PacketTooltipHandler;
import su.nightexpress.excellentenchants.tooltip.handler.ProtocolTooltipHandler;
import su.nightexpress.excellentenchants.EnchantsUtils;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;

import java.util.*;

public class TooltipManager extends AbstractManager<EnchantsPlugin> implements TooltipController {

    private final TooltipSettings             settings;
    private final Map<String, TooltipFactory> factoryMap;
    private final Set<UUID>                   updateStopList;

    private TooltipHandler handler;

    public TooltipManager(@NotNull EnchantsPlugin plugin) {
        super(plugin);
        this.settings = new TooltipSettings();
        this.factoryMap = new LinkedHashMap<>();
        this.updateStopList = new HashSet<>();
    }

    @Override
    protected void onLoad() {
        this.settings.load(this.plugin.getConfig());
        this.loadFactories();
        this.loadHandler();

        this.addListener(new TooltipListener(this.plugin, this));
    }

    @Override
    protected void onShutdown() {
        if (this.handler != null) {
            this.handler.shutdown();
            this.handler = null;
        }
        this.factoryMap.clear();
        this.updateStopList.clear();
    }

    private void loadFactories() {
        this.factoryMap.put(TooltipPlugins.PACKET_EVENTS, PacketTooltipHandler::new);
        this.factoryMap.put(TooltipPlugins.PROTOCOL_LIB, provider -> new ProtocolTooltipHandler(this.plugin, provider));
    }

    private void loadHandler() {
        for (var entry : this.factoryMap.entrySet()) {
            if (!Plugins.isInstalled(entry.getKey())) continue;

            this.handler = entry.getValue().create(this);
            this.handler.setup();
            this.plugin.info("Using tooltip handler: " + entry.getKey());
            break;
        }

        if (this.handler == null) {
            this.plugin.error("No compatible enchantment tooltip provider found. Please install one of the following plugins for the feature to work: [%s]"
                .formatted(String.join(", ", this.factoryMap.keySet()))
            );
        }
    }

    @Override
    public void runInStopList(@NotNull Player player, @NotNull Runnable runnable) {
        this.addToUpdateStopList(player);
        runnable.run();
        this.removeFromUpdateStopList(player);
    }

    @Override
    public void addToUpdateStopList(@NotNull Player player) {
        this.updateStopList.add(player.getUniqueId());
    }

    @Override
    public void removeFromUpdateStopList(@NotNull Player player) {
        this.updateStopList.remove(player.getUniqueId());
    }

    @Override
    public boolean isReadyForTooltipUpdate(@NotNull Player player) {
        return !this.updateStopList.contains(player.getUniqueId()) && player.getGameMode() != GameMode.CREATIVE;
    }

    @Override
    public boolean isEnchantTooltipAllowed(@NotNull ItemStack item) {
        if (this.settings.isForBooksOnly()) {
            return EnchantsUtils.isEnchantedBook(item);
        }
        return true;
    }

    @NotNull
    private String getDescription(@NotNull CustomEnchantment enchantment, int level, int charges) {
        String format = enchantment.isChargeable() ? this.settings.getTooltipFormatWithCharges() : this.settings.getTooltipFormat();

        PlaceholderContext context = PlaceholderContext.builder()
            .with(EnchantsPlaceholders.GENERIC_DESCRIPTION, () -> String.join("\n", enchantment.getDescription(level)))
            .with(EnchantsPlaceholders.GENERIC_NAME, enchantment::getDisplayName)
            .with(EnchantsPlaceholders.GENERIC_CHARGES, () -> {
                if (!enchantment.isChargeable() || charges < 0) return "";

                int maxCharges = enchantment.getMaxCharges(level);
                int percent = (int) Math.ceil((double) charges / (double) maxCharges * 100D);
                ChargesFormat chargesFormat = this.settings.getTooltipChargesFormat(percent);

                return chargesFormat == null ? "" : chargesFormat.getFormatted(charges);
            })
            .build();

        return context.apply(format);
    }

    @Override
    @NotNull
    public ItemStack addDescription(@NotNull ItemStack itemStack) {
        if (itemStack.getType().isAir() || !this.isEnchantTooltipAllowed(itemStack)) return itemStack;

        ItemStack copy = new ItemStack(itemStack);
        ItemMeta meta = copy.getItemMeta();
        if (meta == null || meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) return itemStack;

        Map<CustomEnchantment, Integer> enchants = EnchantsUtils.getCustomEnchantments(meta);
        if (enchants.isEmpty()) return itemStack;

        List<String> lore = ItemUtil.getLoreSerialized(meta);

        enchants.forEach((enchant, level) -> {
            int chargesAmount = enchant.getCharges(meta);
            lore.add(this.getDescription(enchant, level, chargesAmount));
        });

        ItemUtil.setLore(meta, lore);
        copy.setItemMeta(meta);
        return copy;
    }
}
