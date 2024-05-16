package su.nightexpress.excellentenchants;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.ItemCategory;
import su.nightexpress.excellentenchants.api.DistributionWay;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.command.*;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Keys;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.config.Perms;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.enchantment.EnchantPopulator;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.hook.HookPlugin;
import su.nightexpress.excellentenchants.hook.impl.PlaceholderHook;
import su.nightexpress.excellentenchants.hook.impl.ProtocolHook;
import su.nightexpress.excellentenchants.nms.EnchantNMS;
import su.nightexpress.excellentenchants.nms.v1_19_R3.V1_19_R3;
import su.nightexpress.excellentenchants.nms.v1_20_R1.V1_20_R1;
import su.nightexpress.excellentenchants.nms.v1_20_R2.V1_20_R2;
import su.nightexpress.excellentenchants.nms.v1_20_R3.V1_20_R3;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.command.api.NightPluginCommand;
import su.nightexpress.nightcore.command.base.ReloadSubCommand;
import su.nightexpress.nightcore.config.PluginDetails;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.Version;

public class EnchantsPlugin extends NightPlugin {

    private EnchantRegistry registry;
    private EnchantManager  enchantManager;
    private EnchantNMS      enchantNMS;

    @Override
    public void onLoad() {
        super.onLoad();
        this.registry = new EnchantRegistry(this);
    }

    @Override
    @NotNull
    protected PluginDetails getDefaultDetails() {
        return PluginDetails.create("Enchants", new String[]{"excellentenchants", "eenchants"})
            .setConfigClass(Config.class)
            .setLangClass(Lang.class)
            .setPermissionsClass(Perms.class);
    }

    @Override
    public void enable() {
        if (!this.setNMS()) {
            this.error("Unsupported server version!");
            this.getPluginManager().disablePlugin(this);
            return;
        }

        this.getLangManager().loadEnum(ItemCategory.class);
        this.getLangManager().loadEnum(EnchantmentTarget.class);
        this.getLangManager().loadEnum(DistributionWay.class);
        this.getLangManager().loadEnum(Rarity.class);

        Keys.loadKeys(this);
        Config.loadRarityWeights(this.getConfig());

        this.registerCommands();

        this.registry.setup();

        this.enchantManager = new EnchantManager(this);
        this.enchantManager.setup();

        if (Config.ENCHANTMENTS_DISPLAY_MODE.get() == 2) {
            if (Plugins.isInstalled(HookPlugin.PROTOCOL_LIB)) {
                ProtocolHook.setup(this);
            }
            else {
                this.warn(HookPlugin.PROTOCOL_LIB + " is not installed. Display mode is set to Plain lore.");
                Config.ENCHANTMENTS_DISPLAY_MODE.set(1);
            }
        }

        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.setup(this);
        }
    }

    @Override
    public void disable() {
        if (this.enchantManager != null) {
            this.enchantManager.shutdown();
            this.enchantManager = null;
        }

        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.shutdown();
        }

        this.registry.shutdown();
    }

    private boolean setNMS() {
        this.enchantNMS = switch (Version.getCurrent()) {
            case V1_19_R3 -> new V1_19_R3();
            case V1_20_R1 -> new V1_20_R1();
            case V1_20_R2 -> new V1_20_R2();
            case V1_20_R3 -> new V1_20_R3();
            case MC_1_20_6 -> new Internal1_20_6();
            default -> null;
        };
        return this.enchantNMS != null;
    }

    private void registerCommands() {
        NightPluginCommand mainCommand = this.getBaseCommand();
        mainCommand.addChildren(new BookCommand(this));
        mainCommand.addChildren(new EnchantCommand(this));
        mainCommand.addChildren(new ListCommand(this));
        mainCommand.addChildren(new RarityBookCommand(this));
        mainCommand.addChildren(new ReloadSubCommand(this, Perms.COMMAND_RELOAD));
        if (Config.ENCHANTMENTS_CHARGES_ENABLED.get()) {
            mainCommand.addChildren(new GetFuelCommand(this));
        }
    }

    @NotNull
    public EnchantPopulator createPopulator(@NotNull ItemStack item, @NotNull DistributionWay distributionWay) {
        return new EnchantPopulator(this, item, distributionWay);
    }

    public void populateResource(@NotNull BlockDropItemEvent event, @NotNull ItemStack itemStack) {
        /*if (Plugins.isSpigot()) {
            this.warn("Adding items to BlockDropItemEvent is not supported in Spigot, please use Paper or its forks for this feature.");
            return;
        }*/
        Item item = this.getEnchantNMS().popResource(event.getBlock(), itemStack);
        event.getItems().add(item);
    }

    @NotNull
    public EnchantRegistry getRegistry() {
        return registry;
    }

    @NotNull
    public EnchantManager getEnchantManager() {
        return this.enchantManager;
    }

    @NotNull
    public EnchantNMS getEnchantNMS() {
        return enchantNMS;
    }
}
