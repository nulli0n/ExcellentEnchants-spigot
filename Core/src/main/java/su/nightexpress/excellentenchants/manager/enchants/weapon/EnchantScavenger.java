package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class EnchantScavenger extends IEnchantChanceTemplate implements DeathEnchant {

    private Map<EntityType, Map<Material, Map.Entry<int[], Double>>> loot;

    public static final String ID = "scavenger";

    public EnchantScavenger(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.loot = new HashMap<>();

        for (String eId : cfg.getSection("Settings.Treasures")) {
            EntityType eType = CollectionsUtil.getEnum(eId, EntityType.class);
            if (eType == null || !eType.isAlive()) {
                plugin.error("[Scavenger] Invalid entity type '" + eId + "' !");
                continue;
            }

            Map<Material, Map.Entry<int[], Double>> items = new HashMap<>();
            for (String sFromArray : cfg.getSection("Settings.Treasures." + eId)) {
                Material material = Material.getMaterial(sFromArray.toUpperCase());
                if (material == null) {
                    plugin.error("[Scavenger] Invalid item material '" + sFromArray + "' !");
                    continue;
                }

                String path = "Settings.Treasures." + eId + "." + sFromArray + ".";
                String[] amountSplit = cfg.getString(path + "Amount", "1:1").split(":");
                int amountMin = StringUtil.getInteger(amountSplit[0], 1);
                int amountMax = StringUtil.getInteger(amountSplit[1], 1);
                int[] amount = new int[]{amountMin, amountMax};

                double chance = cfg.getDouble(path + "Chance");
                if (chance <= 0) continue;

                Map.Entry<int[], Double> item = new AbstractMap.SimpleEntry<>(amount, chance);
                items.put(material, item);
            }
            this.loot.put(eType, items);
        }
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean use(@NotNull EntityDeathEvent e, @NotNull LivingEntity dead, int level) {
        if (!this.isEnchantmentAvailable(dead)) return false;

        Map<Material, Map.Entry<int[], Double>> items = this.loot.get(dead.getType());
        if (items == null) return false;

        Player killer = dead.getKiller();
        if (killer == null) return false;

        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(killer)) return false;

        items.forEach((material, data) -> {
            double chance = data.getValue();
            if (Rnd.get(true) > chance) return;

            int amount = Rnd.get(data.getKey()[0], data.getKey()[1]);
            if (amount <= 0) return;

            ItemStack item = new ItemStack(material);
            e.getDrops().add(item);
        });

        return true;
    }
}
