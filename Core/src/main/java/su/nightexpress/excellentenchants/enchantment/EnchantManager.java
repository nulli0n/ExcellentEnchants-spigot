/*
 * Decompiled with CFR 0.151.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  su.nexmedia.engine.NexPlugin
 *  su.nexmedia.engine.api.manager.AbstractManager
 *  su.nexmedia.engine.api.manager.EventListener
 */
package su.nightexpress.excellentenchants.enchantment;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.enchantment.listener.EnchantAnvilListener;
import su.nightexpress.excellentenchants.enchantment.listener.EnchantGenericListener;
import su.nightexpress.excellentenchants.enchantment.menu.EnchantmentsListMenu;
import su.nightexpress.excellentenchants.enchantment.task.ArrowTrailsTask;
import su.nightexpress.excellentenchants.enchantment.task.PassiveEnchantsTask;

public class EnchantManager
extends AbstractManager<ExcellentEnchants> {

    public static final String DIR_ENCHANTS = "/enchants/";
    private EnchantmentsListMenu enchantmentsListMenu;
    private ArrowTrailsTask arrowTrailsTask;
    private PassiveEnchantsTask passiveEnchantsTask;

    public EnchantManager(@NotNull ExcellentEnchants plugin) {
        super(plugin);
    }

    protected void onLoad() {
        this.enchantmentsListMenu = new EnchantmentsListMenu(this.plugin);
        this.addListener(new EnchantGenericListener(this));
        this.addListener(new EnchantAnvilListener(this.plugin));
        this.arrowTrailsTask = new ArrowTrailsTask(this.plugin);
        this.arrowTrailsTask.start();
        this.passiveEnchantsTask = new PassiveEnchantsTask(this.plugin);
        this.passiveEnchantsTask.start();
    }

    protected void onShutdown() {
        if (this.enchantmentsListMenu != null) {
            this.enchantmentsListMenu.clear();
            this.enchantmentsListMenu = null;
        }
        if (this.arrowTrailsTask != null) {
            this.arrowTrailsTask.stop();
            this.arrowTrailsTask = null;
        }
        if (this.passiveEnchantsTask != null) {
            this.passiveEnchantsTask.stop();
            this.passiveEnchantsTask = null;
        }
    }

    @NotNull
    public EnchantmentsListMenu getEnchantsListGUI() {
        return this.enchantmentsListMenu;
    }
}

