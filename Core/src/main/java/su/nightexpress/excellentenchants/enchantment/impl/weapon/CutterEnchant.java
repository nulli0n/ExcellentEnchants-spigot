package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.wrapper.UniParticle;
import su.nightexpress.nightcore.util.wrapper.UniSound;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_DAMAGE;

public class CutterEnchant extends GameEnchantment implements ChanceMeta, CombatEnchant {

    public static final String ID = "cutter";

    private Modifier durabilityReduction;
    private boolean  allowPlayers;
    private boolean  allowMobs;

    public CutterEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            ENCHANTMENT_CHANCE + "% chance to throw away enemy''s armor and damage it for " + GENERIC_DAMAGE + "%.",
            EnchantRarity.LEGENDARY,
            5,
            ItemCategories.WEAPON
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(1, 0.5, 1, 100)));

        this.durabilityReduction = Modifier.read(config, "Settings.Item.Durability_Reduction",
            Modifier.add(0, 0.01, 1, 1D),
            "Amount (in percent) of how much item durability will be reduced.");

        this.allowPlayers = ConfigValue.create("Settings.Allow_Players",
            true,
            "Sets whether or not this enchantment will have effect on players.").read(config);

        this.allowMobs = ConfigValue.create("Settings.Allow_Mobs",
            true,
            "Sets whether or not this enchantment will have effect on mobs.").read(config);

        this.addPlaceholder(GENERIC_DAMAGE, level -> NumberUtil.format(this.getDurabilityReduction(level) * 100D));
    }

    public final double getDurabilityReduction(int level) {
        return this.durabilityReduction.getValue(level);
    }

    @NotNull
    @Override
    public EventPriority getAttackPriority() {
        return EventPriority.HIGHEST;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        EntityEquipment equipment = victim.getEquipment();
        if (equipment == null) return false;

        ItemStack[] armor = equipment.getArmorContents();
        if (armor.length == 0) return false;

        boolean isPlayer = victim instanceof Player;
        if (isPlayer && !this.allowPlayers || (!isPlayer && !this.allowMobs)) return false;

        int get = Rnd.get(armor.length);
        ItemStack itemCut = armor[get];

        if (itemCut == null || itemCut.getType().isAir() || itemCut.getType().getMaxDurability() == 0) return false;

        ItemMeta meta = itemCut.getItemMeta();
        if (!(meta instanceof Damageable damageable)) return false;
        if (!this.checkTriggerChance(level)) return false;

        damageable.setDamage((int) (itemCut.getType().getMaxDurability() * this.getDurabilityReduction(level)));
        itemCut.setItemMeta(damageable);

        armor[get] = null;
        equipment.setArmorContents(armor);

        Item drop = victim.getWorld().dropItemNaturally(victim.getLocation(), itemCut);
        drop.setPickupDelay(50);
        drop.getVelocity().multiply(3D);

        if (this.hasVisualEffects()) {
            UniParticle.itemCrack(itemCut).play(victim.getEyeLocation(), 0.25, 0.15, 30);
            UniSound.of(Sound.ENTITY_ITEM_BREAK).play(victim.getLocation());
        }

        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
