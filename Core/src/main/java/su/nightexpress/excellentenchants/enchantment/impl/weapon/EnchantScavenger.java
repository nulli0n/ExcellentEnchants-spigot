package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.Pair;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

import java.util.HashMap;
import java.util.Map;

public class EnchantScavenger extends ExcellentEnchant implements Chanced, DeathEnchant {

    public static final String ID = "scavenger";

    private Map<EntityType, Map<Material, Pair<int[], Double>>> loot;
    private ChanceImplementation                                chanceImplementation;

    public EnchantScavenger(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.chanceImplementation = ChanceImplementation.create(this);
        this.loot = new HashMap<>();

        for (String eId : cfg.getSection("Settings.Treasures")) {
            EntityType eType = CollectionsUtil.getEnum(eId, EntityType.class);
            if (eType == null || !eType.isAlive()) {
                plugin.error("[Scavenger] Invalid entity type '" + eId + "' !");
                continue;
            }

            Map<Material, Pair<int[], Double>> items = new HashMap<>();
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

                Pair<int[], Double> item = Pair.of(amount, chance);
                items.put(material, item);
            }
            this.loot.put(eType, items);
        }
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent e, @NotNull LivingEntity entity, @NotNull Player killer, int level) {
        if (!this.isAvailableToUse(entity)) return false;

        Map<Material, Pair<int[], Double>> items = this.loot.get(entity.getType());
        if (items == null) return false;

        if (!this.checkTriggerChance(level)) return false;

        items.forEach((material, data) -> {
            double chance = data.getSecond();
            if (Rnd.get(true) > chance) return;

            int amount = Rnd.get(data.getFirst()[0], data.getFirst()[1]);
            if (amount <= 0) return;

            ItemStack item = new ItemStack(material);
            e.getDrops().add(item);
        });

        return true;
    }

    @Override
    public boolean onDeath(@NotNull EntityDeathEvent e, @NotNull LivingEntity entity, int level) {
        return false;
    }
}
