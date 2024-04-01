package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
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
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.wrapper.UniParticle;
import su.nightexpress.nightcore.util.wrapper.UniSound;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class CutterEnchant extends AbstractEnchantmentData implements ChanceData, CombatEnchant {

    public static final String ID = "cutter";

    private Modifier           durabilityReduction;
    private ChanceSettingsImpl chanceSettings;
    private boolean            allowPlayers;
    private boolean            allowMobs;

    public CutterEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to throw away enemy''s armor and damage it for " + GENERIC_DAMAGE + "%.");
        this.setMaxLevel(5);
        this.setRarity(Rarity.RARE);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(1, 0.5, 1, 100));

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

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    public final double getDurabilityReduction(int level) {
        return this.durabilityReduction.getValue(level);
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.WEAPON;
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
