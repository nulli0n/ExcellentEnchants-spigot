package su.nightexpress.excellentenchants;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.Version;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.command.list.ReloadSubCommand;
import su.nexmedia.engine.utils.EngineUtils;
import su.nightexpress.excellentenchants.command.BookCommand;
import su.nightexpress.excellentenchants.command.EnchantCommand;
import su.nightexpress.excellentenchants.command.ListCommand;
import su.nightexpress.excellentenchants.command.TierbookCommand;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.enchantment.EnchantPopulator;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;
import su.nightexpress.excellentenchants.hook.HookId;
import su.nightexpress.excellentenchants.hook.impl.PlaceholderHook;
import su.nightexpress.excellentenchants.hook.impl.ProtocolHook;
import su.nightexpress.excellentenchants.nms.EnchantNMS;
import su.nightexpress.excellentenchants.nms.V1_20_R2;
import su.nightexpress.excellentenchants.nms.v1_18_R2.V1_18_R2;
import su.nightexpress.excellentenchants.nms.v1_19_R3.V1_19_R3;
import su.nightexpress.excellentenchants.nms.v1_20_R1.V1_20_R1;
import su.nightexpress.excellentenchants.tier.TierManager;

public class ExcellentEnchants extends NexPlugin<ExcellentEnchants> {

    private EnchantRegistry registry;
    private EnchantManager  enchantManager;
    private EnchantNMS      enchantNMS;
    private TierManager     tierManager;

    @Override
    @NotNull
    protected ExcellentEnchants getSelf() {
        return this;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.registry = new EnchantRegistry(this);
    }

    @Override
    public void enable() {
        if (!this.setNMS()) {
            this.error("Unsupported server version!");
            this.getPluginManager().disablePlugin(this);
            return;
        }

        this.tierManager = new TierManager(this);
        this.tierManager.setup();

        this.registry.setup();

        this.enchantManager = new EnchantManager(this);
        this.enchantManager.setup();

        if (Config.ENCHANTMENTS_DISPLAY_MODE.get() == 2) {
            if (EngineUtils.hasPlugin(HookId.PROTOCOL_LIB)) {
                ProtocolHook.setup();
            }
            else {
                this.warn(HookId.PROTOCOL_LIB + " is not installed. Set display mode to Plain lore.");
                Config.ENCHANTMENTS_DISPLAY_MODE.set(1);
            }
        }
    }

    @Override
    public void disable() {
        if (this.enchantManager != null) {
            this.enchantManager.shutdown();
            this.enchantManager = null;
        }
        if (this.tierManager != null) {
            this.tierManager.shutdown();
            this.tierManager = null;
        }
        PlaceholderHook.shutdown();
        this.registry.shutdown();
    }

    private boolean setNMS() {
        this.enchantNMS = switch (Version.getCurrent()) {
            case V1_18_R2 -> new V1_18_R2();
            case V1_19_R3 -> new V1_19_R3();
            case V1_20_R1 -> new V1_20_R1();
            case V1_20_R2 -> new V1_20_R2();
            default -> null;
        };
        return this.enchantNMS != null;
    }

    @Override
    public void loadConfig() {
        this.getConfig().initializeOptions(Config.class);
    }

    @Override
    public void loadLang() {
        this.getLangManager().loadMissing(Lang.class);
        this.getLangManager().loadEnum(FitItemType.class);
        this.getLang().saveChanges();
    }

    @Override
    public void registerCommands(@NotNull GeneralCommand<ExcellentEnchants> mainCommand) {
        mainCommand.addChildren(new BookCommand(this));
        mainCommand.addChildren(new EnchantCommand(this));
        mainCommand.addChildren(new ListCommand(this));
        mainCommand.addChildren(new TierbookCommand(this));
        mainCommand.addChildren(new ReloadSubCommand<>(this, Perms.COMMAND_RELOAD));
    }

    @Override
    public void registerHooks() {
        if (EngineUtils.hasPlaceholderAPI()) {
            PlaceholderHook.setup();
        }
    }

    @Override
    public void registerPermissions() {
        this.registerPermissions(Perms.class);
    }

    @NotNull
    public EnchantPopulator createPopulator(@NotNull ItemStack item, @NotNull ObtainType obtainType) {
        return new EnchantPopulator(this, item, obtainType);
    }

    @NotNull
    public TierManager getTierManager() {
        return tierManager;
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
