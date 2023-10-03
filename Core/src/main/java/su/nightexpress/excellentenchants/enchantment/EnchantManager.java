package su.nightexpress.excellentenchants.enchantment;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.enchantment.listener.EnchantAnvilListener;
import su.nightexpress.excellentenchants.enchantment.listener.EnchantGenericListener;
import su.nightexpress.excellentenchants.enchantment.menu.EnchantmentsListMenu;
import su.nightexpress.excellentenchants.enchantment.task.ArrowTrailsTask;
import su.nightexpress.excellentenchants.enchantment.task.PotionEffectsTask;

public class EnchantManager extends AbstractManager<ExcellentEnchants> {

    public static final String DIR_ENCHANTS = "/enchants/";

    private EnchantmentsListMenu enchantmentsListMenu;

    private ArrowTrailsTask   arrowTrailsTask;
    private PotionEffectsTask potionEffectsTask;

    public EnchantManager(@NotNull ExcellentEnchants plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.enchantmentsListMenu = new EnchantmentsListMenu(this.plugin);
        //this.addListener(new EnchantHandlerListener(this));
        this.addListener(new EnchantGenericListener(this));
        this.addListener(new EnchantAnvilListener(this.plugin));

        this.arrowTrailsTask = new ArrowTrailsTask(this.plugin);
        this.arrowTrailsTask.start();

        this.potionEffectsTask = new PotionEffectsTask(this.plugin);
        this.potionEffectsTask.start();
    }

    @Override
    protected void onShutdown() {
        if (this.enchantmentsListMenu != null) {
            this.enchantmentsListMenu.clear();
            this.enchantmentsListMenu = null;
        }
        if (this.arrowTrailsTask != null) {
            this.arrowTrailsTask.stop();
            this.arrowTrailsTask = null;
        }
        if (this.potionEffectsTask != null) {
            this.potionEffectsTask.stop();
            this.potionEffectsTask = null;
        }
    }

    @NotNull
    public EnchantmentsListMenu getEnchantsListGUI() {
        return enchantmentsListMenu;
    }
}
