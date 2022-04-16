package su.nightexpress.excellentenchants.config;

import org.bukkit.enchantments.EnchantmentTarget;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.LangMessage;
import su.nexmedia.engine.core.config.CoreLang;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

public class Lang extends CoreLang {

    public Lang(@NotNull ExcellentEnchants plugin) {
        super(plugin);
        this.setupEnum(EnchantmentTarget.class);
        this.setupEnum(FitItemType.class);
    }

    public LangMessage Command_List_Desc = new LangMessage(this, "List of all custom enchantments.");

    public LangMessage Command_Enchant_Usage = new LangMessage(this, "<enchant> <level>");
    public LangMessage Command_Enchant_Desc  = new LangMessage(this, "Enchants the item in your hand.");
    public LangMessage Command_Enchant_Done  = new LangMessage(this, "&aSuccessfully enchanted!");

    public LangMessage Command_Book_Usage = new LangMessage(this, "<player> <enchant> <level>");
    public LangMessage Command_Book_Desc  = new LangMessage(this, "Gives custom enchanted book.");
    public LangMessage Command_Book_Done  = new LangMessage(this, "Given &6%enchant%&7 enchanted book to &6%player%&7.");

    public LangMessage Command_TierBook_Usage = new LangMessage(this, "<player> <tier> <level>");
    public LangMessage Command_TierBook_Desc  = new LangMessage(this, "Gives an enchanted book.");
    public LangMessage Command_TierBook_Error = new LangMessage(this, "&cInvalid tier!");
    public LangMessage Command_TierBook_Done  = new LangMessage(this, "Given &6%tier%&7 enchanted book to &6%player%&7.");

    public LangMessage Error_NoEnchant = new LangMessage(this, "&cNo such enchant.");

}
