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
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nexmedia.engine.utils.values.UniParticle;
import su.nexmedia.engine.utils.values.UniSound;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

public class EnchantCutter extends ExcellentEnchant implements Chanced, CombatEnchant {

    public static final String ID = "cutter";
    private static final String PLACEHOLDER_DURABILITY_DAMAGE = "%enchantment_durability_damage%";

    private EnchantScaler durabilityReduction;
    private ChanceImplementation chanceImplementation;
    private boolean allowPlayers;
    private boolean allowMobs;

    public EnchantCutter(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to throw away enemy''s armor and damage it for " + PLACEHOLDER_DURABILITY_DAMAGE + "%.");
        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(0.75);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "1.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 0.6");
        this.durabilityReduction = EnchantScaler.read(this, "Settings.Item.Durability_Reduction",
            Placeholders.ENCHANTMENT_LEVEL + " / 100",
            "Amount (in percent) of how much item durability will be reduced.");

        this.allowPlayers = JOption.create("Settings.Allow_Players", true,
            "Sets whether or not this enchantment will have effect on players.").read(cfg);

        this.allowMobs = JOption.create("Settings.Allow_Mobs", true,
            "Sets whether or not this enchantment will have effect on mobs.").read(cfg);

        this.addPlaceholder(PLACEHOLDER_DURABILITY_DAMAGE, level -> NumberUtil.format(this.getDurabilityReduction(level) * 100D));
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    public final double getDurabilityReduction(int level) {
        return this.durabilityReduction.getValue(level);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
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
