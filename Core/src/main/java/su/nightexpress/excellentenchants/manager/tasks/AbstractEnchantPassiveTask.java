package su.nightexpress.excellentenchants.manager.tasks;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.task.AbstractTask;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.config.Config;

import java.util.*;

public abstract class AbstractEnchantPassiveTask extends AbstractTask<ExcellentEnchants> {

    public AbstractEnchantPassiveTask(@NotNull ExcellentEnchants plugin, long interval, boolean async) {
        super(plugin, interval, async);
    }

    protected abstract void apply(@NotNull LivingEntity entity, @NotNull ItemStack armor, @NotNull ItemMeta meta);

    @Override
    public void action() {
        for (LivingEntity entity : this.getEntities()) {

            List<ItemStack> list = new ArrayList<>(Arrays.asList(EntityUtil.getArmor(entity)));
            EntityEquipment equipment = entity.getEquipment();
            if (equipment != null && !ItemUtil.isArmor(equipment.getItemInMainHand())) {
                list.add(equipment.getItemInMainHand());
            }
            list.removeIf(armor -> armor == null || armor.getType().isAir() || armor.getType() == Material.ENCHANTED_BOOK);

            for (ItemStack armor : list) {
                ItemMeta meta = armor.getItemMeta();
                if (meta == null) continue;

                this.apply(entity, armor, meta);
            }
        }
    }

    @NotNull
    protected Collection<@NotNull ? extends LivingEntity> getEntities() {
        Set<LivingEntity> list = new HashSet<>(plugin.getServer().getOnlinePlayers());

        if (Config.ENCHANTMENTS_ENTITY_PASSIVE_FOR_MOBS) {
            plugin.getServer().getWorlds().forEach(world -> {
                list.addAll(world.getEntitiesByClass(LivingEntity.class));
            });
        }
        return list;
    }
}
