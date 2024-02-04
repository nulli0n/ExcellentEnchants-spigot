package su.nightexpress.excellentenchants.enchantment.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.Evaluator;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;

import java.util.*;

public class EnchantScaler {

    public EnchantScaler(@NotNull ExcellentEnchant enchant, @NotNull String path) {
        this(enchant.getConfig(), path, Placeholders.ENCHANTMENT_LEVEL, enchant.getStartLevel(), enchant.getMaxLevel());
    }

    @NotNull
    public static EnchantScaler read(@NotNull ExcellentEnchant enchant, @NotNull String path, @NotNull String def, @Nullable String... comments) {
        enchant.getConfig().addMissing(path, def);
        if (comments != null) {
            List<String> list = new ArrayList<>(Arrays.asList(comments));
            list.add("You can use formulas/expressions here: " + Placeholders.URL_ENGINE_SCALER);
            list.add("Level placeholder: " + Placeholders.ENCHANTMENT_LEVEL);
            enchant.getConfig().setComments(path, list);
        }
        return new EnchantScaler(enchant, path);
    }

    private final int                      levelMin;
    private final int                      levelMax;
    private final TreeMap<Integer, Double> values;

    public EnchantScaler(@NotNull JYML cfg, @NotNull String path, @NotNull String levelPlaceholder, int levelMin, int levelMax) {
        this.levelMin = levelMin;
        this.levelMax = levelMax;
        this.values = new TreeMap<>();

        // Load different values for each object level.
        Set<String> lvlKeys = cfg.getSection(path);
        if (!lvlKeys.isEmpty()) {
            for (String sLvl : lvlKeys) {
                int eLvl = StringUtil.getInteger(sLvl, 0);
                if (eLvl < this.getLevelMin() || eLvl > this.getLevelMax()) continue;

                String formula = cfg.getString(path + "." + sLvl, "0").replace(levelPlaceholder, sLvl);
                values.put(eLvl, Evaluator.evaluate(formula));
            }
            return;
        }

        // Load the single formula for all object levels.
        for (int lvl = this.getLevelMin(); lvl < (this.getLevelMax() + 1); lvl++) {
            String sLvl = String.valueOf(lvl);
            String exChance = cfg.getString(path, "").replace(levelPlaceholder, sLvl);
            if (exChance.isEmpty()) continue;

            values.put(lvl, Evaluator.evaluate(exChance));
        }
    }

    public int getLevelMin() {
        return this.levelMin;
    }

    public int getLevelMax() {
        return this.levelMax;
    }

    @NotNull
    public TreeMap<Integer, Double> getValues() {
        return this.values;
    }

    public double getValue(int level) {
        Map.Entry<Integer, Double> en = this.values.floorEntry(level);
        return en != null ? en.getValue() : 0D;
    }
}
