package su.nightexpress.excellentenchants;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantDefaults;
import su.nightexpress.excellentenchants.api.EnchantRegistry;
import su.nightexpress.excellentenchants.api.config.ConfigBridge;
import su.nightexpress.excellentenchants.api.item.ItemSetRegistry;
import su.nightexpress.excellentenchants.command.BaseCommands;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Keys;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.config.Perms;
import su.nightexpress.excellentenchants.hook.HookPlugin;
import su.nightexpress.excellentenchants.hook.impl.PacketEventsHook;
import su.nightexpress.excellentenchants.hook.impl.PlaceholderHook;
import su.nightexpress.excellentenchants.hook.impl.ProtocolLibHook;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.excellentenchants.manager.EnchantProviders;
import su.nightexpress.excellentenchants.nms.RegistryHack;
import su.nightexpress.excellentenchants.nms.mc_1_21_7.RegistryHack_1_21_7;
import su.nightexpress.excellentenchants.nms.v1_21_4.RegistryHack_1_21_4;
import su.nightexpress.excellentenchants.nms.v1_21_5.RegistryHack_1_21_5;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.command.experimental.ImprovedCommands;
import su.nightexpress.nightcore.config.PluginDetails;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.Version;

import java.io.File;

public class EnchantsPlugin extends NightPlugin implements ImprovedCommands {

    private EnchantManager enchantManager;
    private RegistryHack   registryHack;

    @Override
    @NotNull
    protected PluginDetails getDefaultDetails() {
        return PluginDetails.create("Enchants", new String[]{"eenchants", "excellentenchants"})
            .setConfigClass(Config.class)
            .setLangClass(Lang.class)
            .setPermissionsClass(Perms.class);
    }

    @Override
    public void enable() {
        this.loadAPI();

        if (Version.isSpigot()) {
            this.loadInternals();
            if (this.registryHack == null) {
                this.error("Unsupported server version!");
                this.getPluginManager().disablePlugin(this);
                return;
            }

            File dataDir = this.getDataFolder();

            ConfigBridge.load(dataDir, false); // Load distribution config, assign isPaper field.
            ItemSetRegistry.load(dataDir); // Load default item types, uses ConfigBridge.isPaper() to determine which items source to use.
            EnchantDefaults.load(dataDir); // Load defaults and read from the config files Definition and Distribution settings for enchants.
        }

        if (!EnchantRegistry.isLocked()) {
            EnchantProviders.load(this);
        }

        this.loadCommands();

        this.enchantManager = new EnchantManager(this);
        this.enchantManager.setup();

        this.loadHooks();
    }

    @Override
    public void disable() {
        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.shutdown();
        }

        if (this.enchantManager != null) this.enchantManager.shutdown();

        Keys.clear();
    }

    private void loadInternals() {
        //boolean isSpigot = Version.isSpigot();

        switch (Version.getCurrent()) {
            case MC_1_21_4 -> this.registryHack = new RegistryHack_1_21_4(this);
            case MC_1_21_5 -> this.registryHack = new RegistryHack_1_21_5(this);
            case MC_1_21_7 -> this.registryHack = new RegistryHack_1_21_7(this);
        }
    }

    private void loadAPI() {
        EnchantsAPI.load(this);
        Keys.loadKeys(this);
    }

    private void loadCommands() {
        BaseCommands.load(this);
    }

    private void loadHooks() {
        if (Config.isDescriptionEnabled()) {
            if (Plugins.isInstalled(HookPlugin.PACKET_EVENTS)) {
                PacketEventsHook.setup(this);
            }
            else if (Plugins.isInstalled(HookPlugin.PROTOCOL_LIB)) {
                ProtocolLibHook.setup(this);
            }
            else {
                this.warn("You need to install " + HookPlugin.PACKET_EVENTS + " or " + HookPlugin.PROTOCOL_LIB + " for enchantment description to work.");
            }
        }

        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.setup(this);
        }
    }

    @NotNull
    public EnchantManager getEnchantManager() {
        return this.enchantManager;
    }

//    @NotNull
//    public EnchantNMS getEnchantNMS() {
//        return this.enchantNMS;
//    }

    public RegistryHack getRegistryHack() {
        return this.registryHack;
    }

//    public boolean hasInternals() {
//        return this.enchantNMS != null;
//    }
}
