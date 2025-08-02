package su.nightexpress.excellentenchants.util;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.EnchantHolder;
import su.nightexpress.excellentenchants.api.EnchantRegistry;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Keys;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.random.Rnd;

import java.util.*;
import java.util.function.Consumer;

public class EnchantUtils {

    private static final EquipmentSlot[] HANDS = {EquipmentSlot.HAND, EquipmentSlot.OFF_HAND};
    private static final Set<UUID> IGNORE_DISPLAY_UPDATE = new HashSet<>();

    private static boolean busyBreak;

    public static boolean isBusy() {
        return busyBreak;
    }

    public static void busyBreak(@NotNull Player player, @NotNull Block block) {
        busyBreak = true;
        player.breakBlock(block);
        busyBreak = false;
    }

    public static void safeBusyBreak(@NotNull Player player, @NotNull Block block) {
        if (!isBusy()) {
            busyBreak(player, block);
        }
    }

    public static boolean canUpdateDisplay(@NotNull Player player) {
        return !IGNORE_DISPLAY_UPDATE.contains(player.getUniqueId()) && player.getGameMode() != GameMode.CREATIVE;
    }

    public static void runInDisabledDisplayUpdate(@NotNull Player player, @NotNull Runnable runnable) {
        stopDisplayUpdate(player);
        runnable.run();
        allowDisplayUpdate(player);
    }

    public static void stopDisplayUpdate(@NotNull Player player) {
        IGNORE_DISPLAY_UPDATE.add(player.getUniqueId());
    }

    public static void allowDisplayUpdate(@NotNull Player player) {
        IGNORE_DISPLAY_UPDATE.remove(player.getUniqueId());
    }

    @Nullable
    public static ItemStack addDescription(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir() || !canHaveDescription(item)) return item;

        ItemStack copy = new ItemStack(item);
        ItemMeta meta = copy.getItemMeta();
        if (meta == null || meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) return item;

        Map<CustomEnchantment, Integer> enchants = getCustomEnchantments(meta);
        if (enchants.isEmpty()) return item;

        List<String> lore = ItemUtil.getLoreSerialized(meta);

        enchants.forEach((enchant, level) -> {
            int chargesAmount = enchant.getCharges(meta);
            lore.addAll(enchant.getDescription(level, chargesAmount));
        });

        ItemUtil.setLore(meta, lore);
        copy.setItemMeta(meta);
        return copy;
    }

    public static void setBlockEnchant(@NotNull ItemStack itemStack, @NotNull BlockEnchant enchant) {
        PDCUtil.set(itemStack, Keys.blockEnchant, enchant.getId());
    }

    @Nullable
    public static BlockEnchant getBlockEnchant(@NotNull ItemStack itemStack) {
        String enchantId = PDCUtil.getString(itemStack, Keys.blockEnchant).orElse(null);
        if (enchantId == null) return null;

        return EnchantRegistry.BLOCK.getEnchant(enchantId);
    }

    public static boolean isEnchantedBook(@NotNull ItemStack item) {
        return item.getType() == Material.ENCHANTED_BOOK;
    }

    public static boolean isEquipment(@NotNull ItemStack itemStack) {
        return itemStack.hasItemMeta() && !isEnchantedBook(itemStack);
    }

    public static int randomLevel(@NotNull Enchantment enchantment) {
        return Rnd.get(1, enchantment.getMaxLevel());
    }

    public static boolean add(@NotNull ItemStack item, @NotNull Enchantment enchantment, int level, boolean force) {
        if (!force && (!enchantment.canEnchantItem(item) && !isEnchantedBook(item))) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            if (!storageMeta.addStoredEnchant(enchantment, level, true)) return false;
        }
        else {
            if (!meta.addEnchant(enchantment, level, true)) return false;
        }
        item.setItemMeta(meta);

        return true;
    }

    public static void remove(@NotNull ItemStack item, @NotNull Enchantment enchantment) {
        ItemUtil.editMeta(item, meta -> {
            remove(meta, enchantment);
        });
    }

    public static void remove(@NotNull ItemMeta meta, @NotNull Enchantment enchantment) {
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            storageMeta.removeStoredEnchant(enchantment);
        }
        else {
            meta.removeEnchant(enchantment);
        }
    }

    public static void removeAll(@NotNull ItemStack item) {
        ItemUtil.editMeta(item, meta -> {
            getEnchantments(meta).keySet().forEach(enchantment -> remove(meta, enchantment));
        });
    }

    public static void restoreCharges(@NotNull ItemStack itemStack, @NotNull Enchantment enchantment, int level) {
        CustomEnchantment customEnchantment = EnchantRegistry.getByBukkit(enchantment);
        if (customEnchantment != null && customEnchantment.hasComponent(EnchantComponent.CHARGES)) {
            customEnchantment.restoreCharges(itemStack, level);
        }
    }

    public static boolean canHaveDescription(@NotNull ItemStack item) {
        if (Config.DESCRIPTION_BOOKS_ONLY.get()) {
            return isEnchantedBook(item);
        }
        return true;
    }

    @Nullable
    public static EquipmentSlot getItemHand(@NotNull LivingEntity entity, @NotNull Material material) {
        for (EquipmentSlot slot : HANDS) {
            ItemStack itemStack = EntityUtil.getItemInSlot(entity, slot);
            if (itemStack != null && itemStack.getType() == material) {
                return slot;
            }
        }

        return null;
    }

    @NotNull
    public static Map<Enchantment, Integer> getEnchantments(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? Collections.emptyMap() : getEnchantments(meta);
    }

    @NotNull
    public static Map<Enchantment, Integer> getEnchantments(@NotNull ItemMeta meta) {
        return (meta instanceof EnchantmentStorageMeta storageMeta) ? storageMeta.getStoredEnchants() : meta.getEnchants();
    }

    public static boolean contains(@NotNull ItemStack item, @NotNull String id) {
        CustomEnchantment enchant = EnchantRegistry.getById(id);
        if (enchant == null) return false;

        return contains(item, enchant.getBukkitEnchantment());
    }

    public static boolean contains(@NotNull ItemStack item, @NotNull Enchantment enchantment) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && contains(meta, enchantment);
    }

    public static boolean contains(@NotNull ItemMeta meta, @NotNull Enchantment enchantment) {
        return (meta instanceof EnchantmentStorageMeta storageMeta) ? storageMeta.hasStoredEnchant(enchantment) : meta.hasEnchant(enchantment);
    }

    public static int getLevel(@NotNull ItemStack item, @NotNull Enchantment enchant) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? 0 : getLevel(meta, enchant);
    }

    public static int getLevel(@NotNull ItemMeta meta, @NotNull Enchantment enchant) {
        return meta instanceof EnchantmentStorageMeta storageMeta ? storageMeta.getStoredEnchantLevel(enchant) : meta.getEnchantLevel(enchant);
    }

    public static int countEnchantments(@NotNull ItemStack item) {
        return getEnchantments(item).size();
    }

    public static int countCustomEnchantments(@NotNull ItemStack item) {
        return getCustomEnchantments(item).size();
    }

    @NotNull
    public static Map<CustomEnchantment, Integer> getCustomEnchantments(@NotNull ItemStack item) {
        return toCustomEnchantments(getEnchantments(item));
    }

    @NotNull
    public static Map<CustomEnchantment, Integer> getCustomEnchantments(@NotNull ItemMeta meta) {
        return toCustomEnchantments(getEnchantments(meta));
    }

    @NotNull
    private static Map<CustomEnchantment, Integer> toCustomEnchantments(@NotNull Map<Enchantment, Integer> enchants) {
        Map<CustomEnchantment, Integer> map = new LinkedHashMap<>();
        enchants.forEach((enchantment, level) -> {
            CustomEnchantment excellent = EnchantRegistry.getByBukkit(enchantment);
            if (excellent != null) {
                map.put(excellent, level);
            }
        });
        return map;
    }

    @NotNull
    public static <T extends CustomEnchantment> Map<T, Integer> getCustomEnchantments(@NotNull ItemStack item, @NotNull EnchantHolder<T> holder) {
        Map<T, Integer> map = new HashMap<>();
        getEnchantments(item).forEach((enchantment, level) -> {
            T specific = holder.getEnchant(BukkitThing.getValue(enchantment));
            if (specific == null) return;

            map.put(specific, level);
        });
        return map;
    }

    @Nullable
    public static ItemStack getEquipped(@NotNull LivingEntity entity, @NotNull EquipmentSlot slot) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return null;

        return equipment.getItem(slot);
    }

    @NotNull
    public static <T extends CustomEnchantment> Map<ItemStack, Map<T, Integer>> getAll(@NotNull Player player, @NotNull EnchantHolder<T> holder) {
        Map<ItemStack, Map<T, Integer>> map = new HashMap<>();

        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null || itemStack.getType().isAir()) continue;

            getCustomEnchantments(itemStack, holder).forEach((enchant, level) -> {
                map.computeIfAbsent(itemStack, k -> new LinkedHashMap<>()).put(enchant, level);
            });
        }

        return map;
    }

    @NotNull
    public static <T extends CustomEnchantment> Map<ItemStack, Map<T, Integer>> getEquipped(@NotNull LivingEntity entity, @NotNull EnchantHolder<T> holder) {
        Map<ItemStack, Map<T, Integer>> map = new HashMap<>();

        EntityUtil.getEquippedItems(entity).forEach((slot, itemStack) -> {
            if (itemStack == null || !isEquipment(itemStack)) return;

            getCustomEnchantments(itemStack, holder).forEach((enchant, level) -> {
                EquipmentSlot[] enchantSlots = enchant.getSupportedItems().getSlots();
                if (!Lists.contains(enchantSlots, slot)) return;

                map.computeIfAbsent(itemStack, k -> new LinkedHashMap<>()).put(enchant, level);
            });
        });
        return map;
    }

    public static void addArrowEnchant(@NotNull AbstractArrow arrow, @NotNull CustomEnchantment enchant, int level) {
        PDCUtil.set(arrow, enchant.getBukkitEnchantment().getKey(), level);
    }

    @NotNull
    public static <T extends CustomEnchantment> Map<T, Integer> getArrowEnchants(@NotNull AbstractArrow arrow, @NotNull EnchantHolder<T> holder) {
        Map<T, Integer> map = new HashMap<>();

        holder.getEnchants().forEach(enchant -> {
            int level = PDCUtil.getInt(arrow, enchant.getBukkitEnchantment().getKey()).orElse(-1);
            if (level <= 0) return;

            map.put(enchant, level);
        });

        return map;
    }

    public static void setSpawnReason(@NotNull Entity entity, @NotNull CreatureSpawnEvent.SpawnReason reason) {
        PDCUtil.set(entity, Keys.entitySpawnReason, reason.name());
    }

    @Nullable
    public static CreatureSpawnEvent.SpawnReason getSpawnReason(@NotNull Entity entity) {
        String name = PDCUtil.getString(entity, Keys.entitySpawnReason).orElse(null);
        return name == null ? null : Enums.get(name, CreatureSpawnEvent.SpawnReason.class);
    }

    public static void populateResource(@NotNull BlockDropItemEvent event, @NotNull ItemStack itemStack) {
        populateResource(event, itemStack, null);
    }

    public static void populateResource(@NotNull BlockDropItemEvent event, @NotNull ItemStack itemStack, @Nullable Consumer<Item> consumer) {
        Block block = event.getBlock();
        World world = block.getWorld();
        Location location = block.getLocation();

        float yMod = 0.25F / 2.0F;
        double x = (location.getX() + 0.5F) + Rnd.getDouble(-0.25D, 0.25D);
        double y = (location.getY() + 0.5F) + Rnd.getDouble(-0.25D, 0.25D) - yMod;
        double z = (location.getZ() + 0.5F) + Rnd.getDouble(-0.25D, 0.25D);

        Location spawn = new Location(world, x, y, z);
        Item item = world.createEntity(spawn, Item.class);
        item.setItemStack(itemStack);
        if (consumer != null) consumer.accept(item);

        event.getItems().add(item);
    }
}
