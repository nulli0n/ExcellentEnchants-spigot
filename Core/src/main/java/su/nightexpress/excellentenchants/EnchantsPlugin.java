package su.nightexpress.excellentenchants;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.item.ItemSetRegistry;
import su.nightexpress.excellentenchants.bridge.spigot.SpigotEnchantsBootstrap;
import su.nightexpress.excellentenchants.command.BaseCommands;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Keys;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.config.Perms;
import su.nightexpress.excellentenchants.enchantment.EnchantDataRegistry;
import su.nightexpress.excellentenchants.hook.HookPlugin;
import su.nightexpress.excellentenchants.hook.impl.PacketEventsHook;
import su.nightexpress.excellentenchants.hook.impl.PlaceholderHook;
import su.nightexpress.excellentenchants.hook.impl.ProtocolLibHook;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.nightcore.config.PluginDetails;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.Version;

public class EnchantsPlugin extends NightPlugin {

    private EnchantManager enchantManager;

    @Override
    @NotNull
    protected PluginDetails getDefaultDetails() {
        return PluginDetails.create("Enchants", new String[]{"eenchants", "excellentenchants"})
            .setConfigClass(Config.class)
            .setPermissionsClass(Perms.class);
    }

    @Override
    protected boolean disableCommandManager() {
        return true;
    }

    @Override
    protected void addRegistries() {
        super.addRegistries();
        this.registerLang(Lang.class);
    }

    @Override
    protected void onStartup() {
        super.onStartup();

        EnchantsAPI.load(this);
        Keys.loadKeys(this);

        if (Version.isSpigot()) {
            new SpigotEnchantsBootstrap().bootstrap(this);
        }
    }

    @Override
    public void enable() {
        this.enchantManager = new EnchantManager(this);
        this.enchantManager.setup();

        this.loadHooks();
        this.loadCommands();
    }

    @Override
    public void disable() {
        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.shutdown();
        }

        if (this.enchantManager != null) this.enchantManager.shutdown();
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();

        ItemSetRegistry.clear();
        EnchantDataRegistry.clear();
        Keys.clear();
        EnchantsAPI.clear();
    }

    private void loadCommands() {
        this.rootCommand = NightCommand.forPlugin(this, builder -> new BaseCommands(this).load(builder));
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
}
