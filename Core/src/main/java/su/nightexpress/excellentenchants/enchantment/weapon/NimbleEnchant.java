package su.nightexpress.excellentenchants.enchantment.weapon;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.drops.DropExecutor;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.KillEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantData;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Players;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class NimbleEnchant extends GameEnchantment implements KillEnchant {

    private boolean ignorePlayers;

    // MythicMobs 相关
    private boolean collectMythicDrops;
    private double  mythicSearchRadius;
    private boolean debugLogging;

    // MythicMobs FancyDrops 的 PDC 标记（在 CMI 中显示为 public bukkit values -> mythicmobs:fancydrop）
    private static final NamespacedKey MYTHIC_FANCYDROP_KEY =
            NamespacedKey.fromString("mythicmobs:fancydrop");

    public NimbleEnchant(@NotNull EnchantsPlugin plugin,
                         @NotNull File file,
                         @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.oneHundred());
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.ignorePlayers = ConfigValue.create(
                "Nimble.Ignore_Players",
                false,
                "Sets whether or not to ignore drops from players."
        ).read(config);

        this.collectMythicDrops = ConfigValue.create(
                "Nimble.Collect_Mythic_Drops",
                true,
                "If enabled, Nimble will try to move MythicMobs ground drops into killer's inventory."
        ).read(config);

        this.mythicSearchRadius = ConfigValue.create(
                "Nimble.Mythic_Search_Radius",
                6.0D,
                "Radius around death location to look for MythicMobs item drops."
        ).read(config);

        this.debugLogging = ConfigValue.create(
                "Nimble.Debug_Logging",
                false,
                "If enabled, Nimble will print debug information to console."
        ).read(config);

        if (this.debugLogging) {
            this.plugin.getLogger().info("[ExcellentEnchants] [Nimble] Config loaded: " +
                    "Ignore_Players=" + this.ignorePlayers +
                    ", Collect_Mythic_Drops=" + this.collectMythicDrops +
                    ", Mythic_Search_Radius=" + this.mythicSearchRadius);
        }
    }

    @NotNull
    @Override
    public EnchantPriority getKillPriority() {
        // 和原版一样，监控阶段执行，避免和其他插件抢顺序
        return EnchantPriority.MONITOR;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent event,
                          @NotNull LivingEntity entity,
                          @NotNull Player killer,
                          @NotNull ItemStack weapon,
                          int level) {

        if (this.ignorePlayers && entity instanceof Player) {
            return false;
        }

        // 是否是 MythicMob，以及它的 Options.PreventOtherDrops 设置
        Boolean preventOtherDrops = getMythicPreventOtherDrops(entity); // null = 不是神话怪
        boolean isMythic = preventOtherDrops != null;

        if (this.debugLogging) {
            this.plugin.getLogger().info("[ExcellentEnchants] [Nimble] onKill: " +
                    "victim=" + entity.getType() +
                    ", killer=" + killer.getName() +
                    ", world=" + entity.getWorld().getName() +
                    ", drops=" + event.getDrops().size() +
                    ", isMythic=" + isMythic +
                    ", preventOtherDrops=" + preventOtherDrops);
        }

        // 1. 处理 Bukkit 掉落（原版 event.getDrops()）
        if (!event.getDrops().isEmpty()) {

            if (isMythic && Boolean.TRUE.equals(preventOtherDrops)) {
                // Mythic 配置禁止原版掉落：我们尊重这个配置，不塞进背包，只清掉
                if (this.debugLogging) {
                    List<String> names = event.getDrops().stream()
                            .map(item -> item.getType() + "x" + item.getAmount())
                            .collect(Collectors.toList());
                    this.plugin.getLogger().info("[ExcellentEnchants] [Nimble] MythicMob with PreventOtherDrops=true, clearing Bukkit drops WITHOUT giving: " + names);
                }
                event.getDrops().clear();
            }
            else {
                // 普通怪 或 Mythic 怪但允许原版掉落：按 Nimble 原始逻辑→进背包
                if (this.debugLogging) {
                    List<String> names = event.getDrops().stream()
                            .map(item -> item.getType() + "x" + item.getAmount())
                            .collect(Collectors.toList());
                    this.plugin.getLogger().info("[ExcellentEnchants] [Nimble] Moving Bukkit drops to inventory: " + names);
                }

                for (ItemStack drop : event.getDrops()) {
                    // 保险：去掉 fancydrop 标记，避免出现“带标记的孤立一格”
                    stripMythicFancyDropTag(drop);
                    Players.addItem(killer, drop);
                }
                event.getDrops().clear();
            }
        }

        // 2. 处理 MythicMobs 掉落表生成的地面掉落（包括花式掉落）
        if (this.collectMythicDrops && isMythic) {
            final Location deathLoc = entity.getLocation();
            final Player   player   = killer;
            final double   radius   = this.mythicSearchRadius;

            if (this.debugLogging) {
                this.plugin.getLogger().info("[ExcellentEnchants] [Nimble] Scheduling MythicMobs ground-drop sweep at " +
                        locToString(deathLoc) + ", radius=" + radius);
            }

            // 下一 tick 再扫，确保 Mythic 已经把物品丢到地上
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                if (deathLoc.getWorld() == null) {
                    if (debugLogging) {
                        plugin.getLogger().warning("[ExcellentEnchants] [Nimble] World is null when trying to collect Mythic drops.");
                    }
                    return;
                }

                // 从 MythicMobs 里拿到 Fancy 掉落的归属映射（itemUUID -> ownerUUID）
                Map<UUID, UUID> fancyPickupMap = null;
                try {
                    MythicBukkit mm = MythicBukkit.inst();
                    if (mm != null && mm.getDropManager() instanceof DropExecutor executor) {
                        fancyPickupMap = executor.getItemPickupMap();
                    }
                }
                catch (Throwable ignored) {
                    // 不可用就算了，后面用 null 判断
                }

                int collectedEntities = 0;

                for (Entity nearby : deathLoc.getWorld().getNearbyEntities(deathLoc, radius, radius, radius)) {
                    if (!(nearby instanceof Item itemEntity)) continue;

                    ItemStack stack = itemEntity.getItemStack();
                    if (stack == null || stack.getType().isAir()) continue;

                    boolean isFancyDrop = isMythicFancyDrop(stack);

                    // 如果是 FancyDrops，并且 Mythic 用 itemPickupMap 标记了归属，则只处理属于 killer 本人的那份
                    if (isFancyDrop && fancyPickupMap != null) {
                        UUID owner = fancyPickupMap.get(itemEntity.getUniqueId());
                        if (owner != null && !owner.equals(player.getUniqueId())) {
                            if (debugLogging) {
                                plugin.getLogger().info("[ExcellentEnchants] [Nimble] Skipping FancyDrop owned by another player at " +
                                        locToString(itemEntity.getLocation()) +
                                        " (" + stack.getType() + "x" + stack.getAmount() + ", owner=" + owner + ")");
                            }
                            continue;
                        }
                    }

                    // 额外尊重 Bukkit 原生 owner 机制（某些插件会用 owner 做归属）
                    if (cannotPickupForPlayer(itemEntity, player)) {
                        if (debugLogging) {
                            plugin.getLogger().info("[ExcellentEnchants] [Nimble] Skipping item not pickable for " +
                                    player.getName() + " at " + locToString(itemEntity.getLocation()));
                        }
                        continue;
                    }

                    // 清理 fancydrop 标记，防止同种物品无法堆叠
                    stripMythicFancyDropTag(stack);

                    if (debugLogging) {
                        plugin.getLogger().info("[ExcellentEnchants] [Nimble] Collecting Mythic ground drop: " +
                                stack.getType() + "x" + stack.getAmount() +
                                " at " + locToString(itemEntity.getLocation()));
                    }

                    Players.addItem(player, stack);
                    itemEntity.remove();
                    collectedEntities++;
                }

                if (debugLogging) {
                    plugin.getLogger().info("[ExcellentEnchants] [Nimble] MythicMobs ground-drop sweep finished. Collected entities: " + collectedEntities);
                }
            });
        }

        return true;
    }

    /**
     * 使用 MythicMobs 官方 API：
     * - 如果是神话怪：返回其 Options.PreventOtherDrops 的值（true/false，可能为 null）
     * - 如果不是神话怪：返回 null
     */
    private Boolean getMythicPreventOtherDrops(@NotNull LivingEntity entity) {
        try {
            MythicBukkit mm = MythicBukkit.inst();
            if (mm == null || mm.getMobManager() == null) return null;

            ActiveMob activeMob = mm.getMobManager().getMythicMobInstance(entity);
            if (activeMob == null) return null;

            MythicMob type = activeMob.getType();
            if (type == null) return null;

            return type.getPreventOtherDrops();
        }
        catch (Throwable t) {
            if (this.debugLogging) {
                this.plugin.getLogger().warning("[ExcellentEnchants] [Nimble] Failed to resolve Mythic data for entity "
                        + entity.getType() + ": " + t.getClass().getSimpleName() + " - " + t.getMessage());
            }
            return null;
        }
    }

    /**
     * 判断这个地上掉落对当前玩家是否“不可拾取”：
     * 这里只使用 Bukkit 自带的 owner 机制。
     */
    private boolean cannotPickupForPlayer(@NotNull Item item, @NotNull Player player) {
        try {
            UUID owner = item.getOwner();
            return owner != null && !owner.equals(player.getUniqueId());
        }
        catch (Throwable ignored) {
            // 兼容旧版本：出问题就当可拾取
        }
        return false;
    }

    /**
     * 判断物品是否带有 MythicMobs FancyDrops 的 PDC 标记。
     */
    private boolean isMythicFancyDrop(@NotNull ItemStack stack) {
        if (MYTHIC_FANCYDROP_KEY == null) return false;
        if (!stack.hasItemMeta()) return false;

        try {
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) return false;
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            return pdc != null && pdc.has(MYTHIC_FANCYDROP_KEY, PersistentDataType.STRING);
        }
        catch (Throwable ignored) {
        }
        return false;
    }

    /**
     * 清理 mythicmobs:fancydrop 标记，避免因为这个额外 NBT 导致同一物品无法堆叠。
     */
    private void stripMythicFancyDropTag(@NotNull ItemStack stack) {
        if (MYTHIC_FANCYDROP_KEY == null) return;
        if (!stack.hasItemMeta()) return;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;

        boolean changed = false;

        try {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (pdc != null && pdc.has(MYTHIC_FANCYDROP_KEY, PersistentDataType.STRING)) {
                pdc.remove(MYTHIC_FANCYDROP_KEY);
                changed = true;
            }
        }
        catch (Throwable ignored) {
        }

        if (changed) {
            stack.setItemMeta(meta);
            if (this.debugLogging) {
                this.plugin.getLogger().info("[ExcellentEnchants] [Nimble] Stripped mythicmobs:fancydrop from " +
                        stack.getType() + "x" + stack.getAmount());
            }
        }
    }

    private String locToString(@NotNull Location loc) {
        if (loc.getWorld() == null) {
            return "(null_world," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + ")";
        }
        return "(" + loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + ")";
    }
}
