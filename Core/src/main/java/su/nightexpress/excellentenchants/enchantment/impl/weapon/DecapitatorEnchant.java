package su.nightexpress.excellentenchants.enchantment.impl.weapon;


import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.excellentenchants.hook.HookPlugin;
import su.nightexpress.excellentenchants.hook.impl.MythicMobsHook;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.language.LangAssets;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static su.nightexpress.excellentenchants.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class DecapitatorEnchant extends AbstractEnchantmentData implements ChanceData, DeathEnchant {

    public static final String ID = "decapitator";

    private boolean ignoreMythicMobs;
    private Set<EntityType>         ignoredEntityTypes;
    private String                  headName;
    private Map<EntityType, String> headTextures;
    private ChanceSettingsImpl      chanceSettings;

    private final NamespacedKey skullKey;

    public DecapitatorEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to obtain player''s or mob''s head.");
        this.setMaxLevel(4);
        this.setRarity(Rarity.RARE);

        this.skullKey = new NamespacedKey(plugin, this.getId() + ".entity_type");
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(5, 1.75, 1, 100));

        this.ignoreMythicMobs = ConfigValue.create("Settings.Ignore_Mythic_Mobs",
            true,
            "Sets whether or not MythicMobs should be ignored."
        ).read(config);

        this.ignoredEntityTypes = ConfigValue.forSet("Settings.Ignored_Entity_Types",
            str -> StringUtil.getEnum(str, EntityType.class).orElse(null),
            (cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()),
            () -> Set.of(
                EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.WITHER_SKELETON
            ),
            "List of entities, that won't drop heads."
        ).read(config);

        this.headName = ConfigValue.create("Settings.Head_Item.Name",
                LIGHT_YELLOW.enclose(GENERIC_TYPE + "'s Head"),
                "Head item display name. Use '" + GENERIC_TYPE + "' for entity name.")
            .read(config);

        this.headTextures = ConfigValue.forMap("Settings.Head_Item.Textures",
            id -> StringUtil.getEnum(id, EntityType.class).orElse(null),
            (cfg2, path, id) -> cfg2.getString(path + "." + id),
            (cfg2, path, map) -> map.forEach((type, txt) -> cfg2.set(path + "." + type.name(), txt)),
            () -> {
                Map<EntityType, String> map = new HashMap<>();
                map.put(EntityType.AXOLOTL, "5c167410409336acc58e6433ffa8b7f86a8786e35ec7300b9062340281d4691c");
                map.put(EntityType.BAT, "3820a10db222f69ac2215d7d10dca47eeafa215553764a2b81bafd479e7933d1");
                map.put(EntityType.BEE, "cce9edbbc5fdc0d8487ac72eab239d2cacfe408d74288d6384b044111ba4de0f");
                map.put(EntityType.BLAZE, "b20657e24b56e1b2f8fc219da1de788c0c24f36388b1a409d0cd2d8dba44aa3b");
                map.put(EntityType.CAT, "d0dba942c06b77a2828e3f66a1faec5e8643e9ea61a81a4523279739ed82d");
                map.put(EntityType.CAVE_SPIDER, "eccc4a32d45d74e8b14ef1ffd55cd5f381a06d4999081d52eaea12e13293e209");
                map.put(EntityType.CHICKEN, "1638469a599ceef7207537603248a9ab11ff591fd378bea4735b346a7fae893");
                map.put(EntityType.COD, "7892d7dd6aadf35f86da27fb63da4edda211df96d2829f691462a4fb1cab0");
                map.put(EntityType.COW, "b667c0e107be79d7679bfe89bbc57c6bf198ecb529a3295fcfdfd2f24408dca3");
                map.put(EntityType.DOLPHIN, "8e9688b950d880b55b7aa2cfcd76e5a0fa94aac6d16f78e833f7443ea29fed3");
                map.put(EntityType.DONKEY, "63a976c047f412ebc5cb197131ebef30c004c0faf49d8dd4105fca1207edaff3");
                map.put(EntityType.DROWNED, "c84df79c49104b198cdad6d99fd0d0bcf1531c92d4ab6269e40b7d3cbbb8e98c");
                map.put(EntityType.ELDER_GUARDIAN, "1c797482a14bfcb877257cb2cff1b6e6a8b8413336ffb4c29a6139278b436b");
                map.put(EntityType.ENDERMAN, "c09f1de6135f4bea781c5a8e0d61095f833ee2685d8154ecea814ee6d328a5c6");
                map.put(EntityType.ENDERMITE, "1730127e3ac7677122422df0028d9e7368bd157738c8c3cddecc502e896be01c");
                map.put(EntityType.EVOKER, "806ac02fd9dac966b7e5806736b6feb90e2f3b0577969e673291b8307c1ef8e5");
                map.put(EntityType.FOX, "d8954a42e69e0881ae6d24d4281459c144a0d5a968aed35d6d3d73a3c65d26a");
                map.put(EntityType.GHAST, "64ab8a22e7687cc4c78f3b6ff5b1eb04917b51cd3cd7dbce36171160b3c77ced");
                map.put(EntityType.GOAT, "457a0d538fa08a7affe312903468861720f9fa34e86d44b89dcec5639265f03");
                map.put(EntityType.GUARDIAN, "a0bf34a71e7715b6ba52d5dd1bae5cb85f773dc9b0d457b4bfc5f9dd3cc7c94");
                map.put(EntityType.HOGLIN, "9bb9bc0f01dbd762a08d9e77c08069ed7c95364aa30ca1072208561b730e8d75");
                map.put(EntityType.HORSE, "a996399fff9cbcfb7ba677dd0c2d104229d1cc2307a6f075a882da4694ef80ae");
                map.put(EntityType.HUSK, "d674c63c8db5f4ca628d69a3b1f8a36e29d8fd775e1a6bdb6cabb4be4db121");
                map.put(EntityType.ILLUSIONER, "512512e7d016a2343a7bff1a4cd15357ab851579f1389bd4e3a24cbeb88b");
                map.put(EntityType.IRON_GOLEM, "a9ceb73d97cf5dc32e333dbef7af25f39e42033d684649075ba4681af2a3c01b");
                map.put(EntityType.LLAMA, "9f7d90b305aa64313c8d4404d8d652a96eba8a754b67f4347dcccdd5a6a63398");
                map.put(EntityType.MAGMA_CUBE, "38957d5023c937c4c41aa2412d43410bda23cf79a9f6ab36b76fef2d7c429");
                map.put(EntityType.MULE, "a0486a742e7dda0bae61ce2f55fa13527f1c3b334c57c034bb4cf132fb5f5f");
                map.put(EntityType.MUSHROOM_COW, "45603d539f666fdf0f7a0fe20b81dfef3abe6c51da34b9525a5348432c5523b2");
                map.put(EntityType.OCELOT, "5657cd5c2989ff97570fec4ddcdc6926a68a3393250c1be1f0b114a1db1");
                map.put(EntityType.PANDA, "8018a1771d69c11b8dad42cd310375ba2d827932b25ef357f7e572c1bd0f9");
                map.put(EntityType.PARROT, "a4ba8d66fecb1992e94b8687d6ab4a5320ab7594ac194a2615ed4df818edbc3");
                map.put(EntityType.PHANTOM, "7e95153ec23284b283f00d19d29756f244313a061b70ac03b97d236ee57bd982");
                map.put(EntityType.PIG, "fa305e321e87ec91421ecccf7cfef10703fb77f62658d6b998f117fcf34cd0b2");
                map.put(EntityType.PILLAGER, "18e57841607f449e76b7c820fcbd1913ec1b80c4ac81728874db230f5df2b3b");
                map.put(EntityType.POLAR_BEAR, "d46d23f04846369fa2a3702c10f759101af7bfe8419966429533cd81a11d2b");
                map.put(EntityType.PUFFERFISH, "6df8c316962949ba3be445c94ebf714108252d46459b66110f4bc14e0e1b59dc");
                map.put(EntityType.RABBIT, "ffecc6b5e6ea5ced74c46e7627be3f0826327fba26386c6cc7863372e9bc");
                map.put(EntityType.RAVAGER, "cd20bf52ec390a0799299184fc678bf84cf732bb1bd78fd1c4b441858f0235a8");
                map.put(EntityType.SALMON, "8aeb21a25e46806ce8537fbd6668281cf176ceafe95af90e94a5fd84924878");
                map.put(EntityType.SHEEP, "a723893df4cfb9c7240fc47b560ccf6ddeb19da9183d33083f2c71f46dad290a");
                map.put(EntityType.SILVERFISH, "da91dab8391af5fda54acd2c0b18fbd819b865e1a8f1d623813fa761e924540");
                map.put(EntityType.SLIME, "a5acd8b24f7389a40404348f4344eec2235d4ca718453be9803b60b71a125891");
                map.put(EntityType.SNOWMAN, "8e8d206f61e6de8a79d0cb0bcd98aced464cbfefc921b4160a25282163112a");
                map.put(EntityType.SPIDER, "cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1");
                map.put(EntityType.SQUID, "d8705624daa2956aa45956c81bab5f4fdb2c74a596051e24192039aea3a8b8");
                map.put(EntityType.STRAY, "9e391c6e535f7aa5a2b6ee6d137f59f2d7c60def88853ba611ceb2d16a7e7c73");
                map.put(EntityType.STRIDER, "125851a86ee1c54c94fc5bed017823dfb3ba08eddbcab2a914ef45b596c1603");
                map.put(EntityType.TRADER_LLAMA, "8424780b3c5c5351cf49fb5bf41fcb289491df6c430683c84d7846188db4f84d");
                map.put(EntityType.TROPICAL_FISH, "d6dd5e6addb56acbc694ea4ba5923b1b25688178feffa72290299e2505c97281");
                map.put(EntityType.TURTLE, "8fa552139966c5fac1b98061ce23fc0ddef058c163142dd6d1c768cd2da207c2");
                map.put(EntityType.VEX, "5e7330c7d5cd8a0a55ab9e95321535ac7ae30fe837c37ea9e53bea7ba2de86b");
                map.put(EntityType.VILLAGER, "a36e9841794a37eb99524925668b47a62b5cb72e096a9f8f95e106804ae13e1b");
                map.put(EntityType.VINDICATOR, "6deaec344ab095b48cead7527f7dee61b063ff791f76a8fa76642c8676e2173");
                map.put(EntityType.WANDERING_TRADER, "ee011aac817259f2b48da3e5ef266094703866608b3d7d1754432bf249cd2234");
                map.put(EntityType.WITCH, "8aa986a6e1c2d88ff198ab2c3259e8d2674cb83a6d206f883bad2c8ada819");
                map.put(EntityType.WOLF, "28d408842e76a5a454dc1c7e9ac5c1a8ac3f4ad34d6973b5275491dff8c5c251");
                map.put(EntityType.ZOGLIN, "e67e18602e03035ad68967ce090235d8996663fb9ea47578d3a7ebbc42a5ccf9");
                map.put(EntityType.ZOMBIFIED_PIGLIN, "7eabaecc5fae5a8a49c8863ff4831aaa284198f1a2398890c765e0a8de18da8c");
                map.put(EntityType.ZOMBIE_HORSE, "d22950f2d3efddb18de86f8f55ac518dce73f12a6e0f8636d551d8eb480ceec");
                map.put(EntityType.ZOMBIE_VILLAGER, "b2b393be2dc2973d41a834e19dd6b73b866782d684a097ebfe99cb390194f");
                map.put(EntityType.SKELETON_HORSE, "47effce35132c86ff72bcae77dfbb1d22587e94df3cbc2570ed17cf8973a");
                return map;
            },
            "Head texture values for each entity type.",
            "You can take some from http://minecraft-heads.com",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html"
        ).read(config);
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onDeath(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, ItemStack item, int level) {
        return false;
    }

    @Override
    public boolean onResurrect(@NotNull EntityResurrectEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        return false;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, @NotNull Player killer, ItemStack weapon, int level) {
        EntityType entityType = entity.getType();
        if (this.ignoredEntityTypes.contains(entityType)) return false;
        if (this.ignoreMythicMobs && Plugins.isLoaded(HookPlugin.MYTHIC_MOBS) && MythicMobsHook.isMythicMob(entity)) return false;
        if (!this.checkTriggerChance(level)) return false;

        ItemStack item;
        if (entityType == EntityType.WITHER_SKELETON || entityType == EntityType.WITHER) {
            item = new ItemStack(Material.WITHER_SKELETON_SKULL);
        }
        else if (entityType == EntityType.ZOMBIE || entityType == EntityType.GIANT) {
            item = new ItemStack(Material.ZOMBIE_HEAD);
        }
        else if (entityType == EntityType.SKELETON) {
            item = new ItemStack(Material.SKELETON_SKULL);
        }
        else if (entityType == EntityType.CREEPER) {
            item = new ItemStack(Material.CREEPER_HEAD);
        }
        else if (entityType == EntityType.ENDER_DRAGON) {
            item = new ItemStack(Material.DRAGON_HEAD);
        }
        else {
            item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta == null) return false;

            String entityName;
            if (entity instanceof Player player) {
                entityName = this.headName.replace(GENERIC_TYPE, entity.getName());
                meta.setOwningPlayer(player);
            }
            else {
                String texture = this.headTextures.get(entity.getType());
                if (texture == null) return false;

                entityName = this.headName.replace(GENERIC_TYPE, LangAssets.get(entity.getType()));
                ItemUtil.setHeadSkin(item, texture);
                meta = (SkullMeta) item.getItemMeta();
            }

            meta.setDisplayName(entityName);
            item.setItemMeta(meta);
        }
        PDCUtil.set(item, this.skullKey, entityType.name());

        entity.getWorld().dropItemNaturally(entity.getLocation(), item);

        if (this.hasVisualEffects()) {
            UniParticle.blockCrack(Material.REDSTONE_BLOCK).play(entity.getEyeLocation(), 0.25, 0.15, 30);
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!(event.getBlock().getState() instanceof Skull skull)) return;

        ItemStack skullItem = event.getItemInHand();
        PDCUtil.getString(skullItem, this.skullKey).ifPresent(type -> {
            PDCUtil.set(skull, this.skullKey, type);
            skull.update(true);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockDropItemEvent event) {
        if (!(event.getBlockState() instanceof Skull skull)) return;

        PDCUtil.getString(skull, this.skullKey).ifPresent(type -> {
            EntityType entityType = StringUtil.getEnum(type, EntityType.class).orElse(null);
            if (entityType == null) return;

            String texture = this.headTextures.get(entityType);
            if (texture == null) return;

            event.getItems().forEach(item -> {
                ItemStack drop = item.getItemStack();
                if (drop.getType() == Material.PLAYER_HEAD) {
                    ItemUtil.setHeadSkin(drop, texture);
                    ItemUtil.editMeta(drop, meta -> {
                        String name = this.headName.replace(GENERIC_TYPE, LangAssets.get(entityType));
                        meta.setDisplayName(name);
                        PDCUtil.set(meta, this.skullKey, type);
                    });
                }
                item.setItemStack(drop);
            });
        });
    }
}
