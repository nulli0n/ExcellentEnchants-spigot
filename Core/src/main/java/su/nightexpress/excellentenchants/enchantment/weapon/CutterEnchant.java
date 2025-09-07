package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.AttackEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.bukkit.NightSound;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

public class CutterEnchant extends GameEnchantment implements AttackEnchant {

    private Modifier durabilityReduction;
    private boolean  allowPlayers;
    private boolean  allowMobs;

    public CutterEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(2, 1));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.durabilityReduction = Modifier.load(config, "Cutter.Durability_Reduction",
            Modifier.addictive(0).perLevel(0.01).capacity(1D),
            "Amount (in percent) of how much item durability will be reduced.");

        this.allowPlayers = ConfigValue.create("Cutter.Allow_Players",
            true,
            "Sets whether or not this enchantment will have effect on players.").read(config);

        this.allowMobs = ConfigValue.create("Cutter.Allow_Mobs",
            true,
            "Sets whether or not this enchantment will have effect on mobs.").read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_DAMAGE, level -> NumberUtil.format(this.getDurabilityReduction(level) * 100D));
    }

    public final double getDurabilityReduction(int level) {
        return this.durabilityReduction.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.NORMAL;
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

        damageable.setDamage((int) (itemCut.getType().getMaxDurability() * this.getDurabilityReduction(level)));
        itemCut.setItemMeta(damageable);

        armor[get] = null;
        equipment.setArmorContents(armor);

        this.plugin.runTask(victim.getLocation(), () -> {
            Item drop = victim.getWorld().dropItemNaturally(victim.getLocation(), itemCut);
            this.plugin.runTask(drop, () -> {
                drop.setPickupDelay(50);
                drop.getVelocity().multiply(3D);
            });
        });

        if (this.hasVisualEffects()) {
            UniParticle.itemCrack(itemCut).play(victim.getEyeLocation(), 0.25, 0.15, 30);
            NightSound.of(Sound.ENTITY_ITEM_BREAK).play(victim.getLocation());
        }

        return true;
    }
}
