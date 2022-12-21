package su.nightexpress.excellentenchants;

import org.bukkit.enchantments.EnchantmentTarget;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.Version;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.command.list.ReloadSubCommand;
import su.nightexpress.excellentenchants.command.BookCommand;
import su.nightexpress.excellentenchants.command.EnchantCommand;
import su.nightexpress.excellentenchants.command.ListCommand;
import su.nightexpress.excellentenchants.command.TierbookCommand;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.excellentenchants.manager.type.FitItemType;
import su.nightexpress.excellentenchants.nms.EnchantNMS;
import su.nightexpress.excellentenchants.nms.v1_17_R1.V1_17_R1;
import su.nightexpress.excellentenchants.nms.v1_18_R2.V1_18_R2;
import su.nightexpress.excellentenchants.nms.v1_19_R1.V1_19_R1;
import su.nightexpress.excellentenchants.nms.v1_19_R2.V1_19_R2;

public class ExcellentEnchants extends NexPlugin<ExcellentEnchants> {

    public static boolean isLoaded = false;

    private EnchantNMS     enchantNMS;
    private EnchantManager enchantManager;

    @Override
    @NotNull
    protected ExcellentEnchants getSelf() {
        return this;
    }

    @Override
    public void enable() {
        if (!this.setNMS()) {
            this.error("Could not setup internal NMS handler!");
            this.getPluginManager().disablePlugin(this);
            return;
        }

        this.enchantManager = new EnchantManager(this);
        this.enchantManager.setup();
    }

    @Override
    public void disable() {
        if (this.enchantManager != null) {
            this.enchantManager.shutdown();
            this.enchantManager = null;
        }
    }

    private boolean setNMS() {
        this.enchantNMS = switch (Version.CURRENT) {
            case V1_17_R1 -> new V1_17_R1();
            case V1_18_R2 -> new V1_18_R2();
            case V1_19_R1 -> new V1_19_R1();
            case V1_19_R2 -> new V1_19_R2();
        };
        return true;
    }

    @Override
    public void loadConfig() {
        Config.load(this);
    }

    @Override
    public void loadLang() {
        this.getLangManager().loadMissing(Lang.class);
        this.getLangManager().setupEnum(EnchantmentTarget.class);
        this.getLangManager().setupEnum(FitItemType.class);
        this.getLang().saveChanges();
    }

    @Override
    public void registerCommands(@NotNull GeneralCommand<ExcellentEnchants> mainCommand) {
        mainCommand.addChildren(new BookCommand(this));
        mainCommand.addChildren(new EnchantCommand(this));
        mainCommand.addChildren(new ListCommand(this));
        mainCommand.addChildren(new TierbookCommand(this));
        mainCommand.addChildren(new ReloadSubCommand<>(this, Perms.PREFIX + "admin"));
    }

    @Override
    public void registerHooks() {

    }

    @Override
    public void registerPermissions() {
        // TODO
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
