package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Pair;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

import java.util.HashMap;
import java.util.Map;

public class EnchantScavenger extends ExcellentEnchant implements Chanced, DeathEnchant {

    public static final String ID = "scavenger";

    private Map<EntityType, Map<Material, Pair<int[], Double>>> loot;

    private ChanceImplementation chanceImplementation;

    public EnchantScavenger(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to obtain additional loot from mobs.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.3);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "15.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 10");

        this.loot = new HashMap<>();

        if (!this.cfg.contains("Settings.Treasures")) {
            cfg.addMissing("Settings.Treasures.VILLAGER.EMERALD.Amount", "1:1");
            cfg.addMissing("Settings.Treasures.VILLAGER.EMERALD.Chance", "50");

            cfg.addMissing("Settings.Treasures.SKELETON.BONE_MEAL.Amount", "1:2");
            cfg.addMissing("Settings.Treasures.SKELETON.BONE_MEAL.Chance", "50");

            cfg.saveChanges();
        }

        for (String eId : cfg.getSection("Settings.Treasures")) {
            EntityType eType = StringUtil.getEnum(eId, EntityType.class).orElse(null);
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
    public boolean onKill(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull Player killer, int level) {
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
            event.getDrops().add(item);
        });

        return true;
    }

    @Override
    public boolean onDeath(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, ItemStack item, int level) {
        return false;
    }
}
