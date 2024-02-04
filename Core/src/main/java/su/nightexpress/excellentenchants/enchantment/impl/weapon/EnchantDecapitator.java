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
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.values.UniParticle;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EnchantDecapitator extends ExcellentEnchant implements Chanced, DeathEnchant {

    public static final String ID = "decapitator";

    private Set<EntityType>     ignoredEntityTypes;
    private String              headName;
    private Map<EntityType, String> headTextures;

    private ChanceImplementation chanceImplementation;

    private final NamespacedKey skullKey;

    public EnchantDecapitator(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to obtain player''s or mob''s head.");
        this.getDefaults().setLevelMax(4);
        this.getDefaults().setTier(0.75);

        this.skullKey = new NamespacedKey(plugin, this.getId() + ".entity_type");
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "5.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 1.75");

        this.ignoredEntityTypes = JOption.forSet("Settings.Ignored_Entity_Types",
            str -> StringUtil.getEnum(str, EntityType.class).orElse(null),
            () -> Set.of(
                EntityType.BAT, EntityType.BEE, EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.WITHER_SKELETON
            ),
            "List of entities, that won't drop heads."
        ).setWriter((cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList())).read(cfg);

        this.headName = JOption.create("Settings.Head_Item.Name", "&c" + Placeholders.GENERIC_TYPE + "'s Head",
            "Head item display name. Use '" + Placeholders.GENERIC_TYPE + "' for entity name.")
            .mapReader(Colorizer::apply).read(cfg);

        this.headTextures = JOption.forMap("Settings.Head_Item.Textures",
            id -> StringUtil.getEnum(id, EntityType.class).orElse(null),
            (cfg, path, id) -> cfg.getString(path + "." + id),
            () -> {
                Map<EntityType, String> map = new HashMap<>();
                map.put(EntityType.AXOLOTL, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNThkYTFhMGEyYTEzZGQyMDliZmMyNTI5ZDljN2MyOWEyOWRkOWEyM2ZmNGI4MGIzOGI4OTk2MTc3MmU4MDM5ZSJ9fX0=");
                map.put(EntityType.BAT, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWU5OWRlZWY5MTlkYjY2YWMyYmQyOGQ2MzAyNzU2Y2NkNTdjN2Y4YjEyYjlkY2E4ZjQxYzNlMGEwNGFjMWNjIn19fQ==");
                map.put(EntityType.BEE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI3MjRhOWE0Y2RkNjhiYTQ5NDE1NTYwZTViZTQwYjRhMWM0N2NiNWJlMWQ2NmFlZGI1MmEzMGU2MmVmMmQ0NyJ9fX0=");
                map.put(EntityType.BLAZE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc4ZWYyZTRjZjJjNDFhMmQxNGJmZGU5Y2FmZjEwMjE5ZjViMWJmNWIzNWE0OWViNTFjNjQ2Nzg4MmNiNWYwIn19fQ==");
                map.put(EntityType.CAT, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTRiNDVjYmFhMTlmZTNkNjhjODU2Y2QzODQ2YzAzYjVmNTlkZTgxYTQ4MGVlYzkyMWFiNGZhM2NkODEzMTcifX19");
                map.put(EntityType.CAVE_SPIDER, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTYxN2Y3ZGQ1ZWQxNmYzYmQxODY0NDA1MTdjZDQ0MGExNzAwMTViMWNjNmZjYjJlOTkzYzA1ZGUzM2YifX19");
                map.put(EntityType.CHICKEN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTYzODQ2OWE1OTljZWVmNzIwNzUzNzYwMzI0OGE5YWIxMWZmNTkxZmQzNzhiZWE0NzM1YjM0NmE3ZmFlODkzIn19fQ==");
                map.put(EntityType.COD, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg5MmQ3ZGQ2YWFkZjM1Zjg2ZGEyN2ZiNjNkYTRlZGRhMjExZGY5NmQyODI5ZjY5MTQ2MmE0ZmIxY2FiMCJ9fX0=");
                map.put(EntityType.COW, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2RmYTBhYzM3YmFiYTJhYTI5MGU0ZmFlZTQxOWE2MTNjZDYxMTdmYTU2OGU3MDlkOTAzNzQ3NTNjMDMyZGNiMCJ9fX0=");
                map.put(EntityType.DOLPHIN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGU5Njg4Yjk1MGQ4ODBiNTViN2FhMmNmY2Q3NmU1YTBmYTk0YWFjNmQxNmY3OGU4MzNmNzQ0M2VhMjlmZWQzIn19fQ==");
                map.put(EntityType.DONKEY, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjNhOTc2YzA0N2Y0MTJlYmM1Y2IxOTcxMzFlYmVmMzBjMDA0YzBmYWY0OWQ4ZGQ0MTA1ZmNhMTIwN2VkYWZmMyJ9fX0=");
                map.put(EntityType.DROWNED, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzNmN2NjZjYxZGJjM2Y5ZmU5YTYzMzNjZGUwYzBlMTQzOTllYjJlZWE3MWQzNGNmMjIzYjNhY2UyMjA1MSJ9fX0=");
                map.put(EntityType.ELDER_GUARDIAN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM3OTc0ODJhMTRiZmNiODc3MjU3Y2IyY2ZmMWI2ZTZhOGI4NDEzMzM2ZmZiNGMyOWE2MTM5Mjc4YjQzNmIifX19");
                map.put(EntityType.ENDERMAN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjMGIzNmQ1M2ZmZjY5YTQ5YzdkNmYzOTMyZjJiMGZlOTQ4ZTAzMjIyNmQ1ZTgwNDVlYzU4NDA4YTM2ZTk1MSJ9fX0=");
                map.put(EntityType.ENDERMITE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWExYTA4MzFhYTAzYWZiNDIxMmFkY2JiMjRlNWRmYWE3ZjQ3NmExMTczZmNlMjU5ZWY3NWE4NTg1NSJ9fX0=");
                map.put(EntityType.EVOKER, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDk1NDEzNWRjODIyMTM5NzhkYjQ3ODc3OGFlMTIxMzU5MWI5M2QyMjhkMzZkZDU0ZjFlYTFkYTQ4ZTdjYmE2In19fQ==");
                map.put(EntityType.FOX, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDg5NTRhNDJlNjllMDg4MWFlNmQyNGQ0MjgxNDU5YzE0NGEwZDVhOTY4YWVkMzVkNmQzZDczYTNjNjVkMjZhIn19fQ==");
                map.put(EntityType.GHAST, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGU4YTM4ZTlhZmJkM2RhMTBkMTliNTc3YzU1YzdiZmQ2YjRmMmU0MDdlNDRkNDAxN2IyM2JlOTE2N2FiZmYwMiJ9fX0=");
                map.put(EntityType.GOAT, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDU3YTBkNTM4ZmEwOGE3YWZmZTMxMjkwMzQ2ODg2MTcyMGY5ZmEzNGU4NmQ0NGI4OWRjZWM1NjM5MjY1ZjAzIn19fQ==");
                map.put(EntityType.GUARDIAN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTBiZjM0YTcxZTc3MTViNmJhNTJkNWRkMWJhZTVjYjg1Zjc3M2RjOWIwZDQ1N2I0YmZjNWY5ZGQzY2M3Yzk0In19fQ==");
                map.put(EntityType.HOGLIN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWJiOWJjMGYwMWRiZDc2MmEwOGQ5ZTc3YzA4MDY5ZWQ3Yzk1MzY0YWEzMGNhMTA3MjIwODU2MWI3MzBlOGQ3NSJ9fX0=");
                map.put(EntityType.HORSE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjY2YjJiMzJkMzE1MzljNzM4M2Q5MjNiYWU0ZmFhZjY1ZGE2NzE1Y2Q1MjZjMzVkMmU0ZTY4MjVkYTExZmIifX19");
                map.put(EntityType.HUSK, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDY3NGM2M2M4ZGI1ZjRjYTYyOGQ2OWEzYjFmOGEzNmUyOWQ4ZmQ3NzVlMWE2YmRiNmNhYmI0YmU0ZGIxMjEifX19");
                map.put(EntityType.ILLUSIONER, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmYyODgyZGQwOTcyM2U0N2MwYWI5NjYzZWFiMDgzZDZhNTk2OTI3MzcwNjExMGM4MjkxMGU2MWJmOGE4ZjA3ZSJ9fX0=");
                map.put(EntityType.IRON_GOLEM, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODkwOTFkNzllYTBmNTllZjdlZjk0ZDdiYmE2ZTVmMTdmMmY3ZDQ1NzJjNDRmOTBmNzZjNDgxOWE3MTQifX19");
                map.put(EntityType.LLAMA, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODNkOWI1OTE1OTEyZmZjMmI4NTc2MWQ2YWRjYjQyOGE4MTJmOWI4M2ZmNjM0ZTMzMTE2MmNlNDZjOTllOSJ9fX0=");
                map.put(EntityType.MAGMA_CUBE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTFjOTdhMDZlZmRlMDRkMDAyODdiZjIwNDE2NDA0YWIyMTAzZTEwZjA4NjIzMDg3ZTFiMGMxMjY0YTFjMGYwYyJ9fX0=");
                map.put(EntityType.MULE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZkY2RhMjY1ZTU3ZTRmNTFiMTQ1YWFjYmY1YjU5YmRjNjA5OWZmZDNjY2UwYTY2MWIyYzAwNjVkODA5MzBkOCJ9fX0=");
                map.put(EntityType.MUSHROOM_COW, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmI1Mjg0MWYyZmQ1ODllMGJjODRjYmFiZjllMWMyN2NiNzBjYWM5OGY4ZDZiM2RkMDY1ZTU1YTRkY2I3MGQ3NyJ9fX0=");
                map.put(EntityType.OCELOT, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY1N2NkNWMyOTg5ZmY5NzU3MGZlYzRkZGNkYzY5MjZhNjhhMzM5MzI1MGMxYmUxZjBiMTE0YTFkYjEifX19");
                map.put(EntityType.PANDA, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDE4OGM5ODBhYWNmYTk0Y2YzMzA4ODUxMmIxYjk1MTdiYTgyNmIxNTRkNGNhZmMyNjJhZmY2OTc3YmU4YSJ9fX0=");
                map.put(EntityType.PARROT, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjBiZmE4NTBmNWRlNGIyOTgxY2NlNzhmNTJmYzJjYzdjZDdiNWM2MmNhZWZlZGRlYjljZjMxMWU4M2Q5MDk3In19fQ==");
                map.put(EntityType.PHANTOM, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDExZDI1YmNkYWJhZmFkNWZkNmUwMTBjNWIxY2Y3YTAwYzljY2E0MGM1YTQ2NzQ3ZjcwNmRjOWNiM2EifX19");
                map.put(EntityType.PIG, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWEzNzFhMDZlYTc4NThmODlkMjdjYzEwNTVjMzE3YjIzZjEwNWM5MTI1YmM1MTZkMzg5MWFhNGM4MzVjMjk5In19fQ==");
                map.put(EntityType.PIGLIN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2NlZDlkODAxYWE2ZjgzZjhlNDlmOTBkOWE4Yjg1YjdmOGZkYTU4M2Q4NWY3MmNmZmI2OTg2NzI1Nzg5ZjYzNiJ9fX0=");
                map.put(EntityType.PIGLIN_BRUTE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2NlZDlkODAxYWE2ZjgzZjhlNDlmOTBkOWE4Yjg1YjdmOGZkYTU4M2Q4NWY3MmNmZmI2OTg2NzI1Nzg5ZjYzNiJ9fX0=");
                map.put(EntityType.PILLAGER, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGFlZTZiYjM3Y2JmYzkyYjBkODZkYjVhZGE0NzkwYzY0ZmY0NDY4ZDY4Yjg0OTQyZmRlMDQ0MDVlOGVmNTMzMyJ9fX0=");
                map.put(EntityType.POLAR_BEAR, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzRmZTkyNjkyMmZiYjQwNmYzNDNiMzRhMTBiYjk4OTkyY2VlNDQxMDEzN2QzZjg4MDk5NDI3YjIyZGUzYWI5MCJ9fX0=");
                map.put(EntityType.PUFFERFISH, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjkyMzUwYzlmMDk5M2VkNTRkYjJjNzExMzkzNjMyNTY4M2ZmYzIwMTA0YTliNjIyYWE0NTdkMzdlNzA4ZDkzMSJ9fX0=");
                map.put(EntityType.RABBIT, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzk3N2EzMjY2YmYzYjllYWYxN2U1YTAyZWE1ZmJiNDY4MDExNTk4NjNkZDI4OGI5M2U2YzEyYzljYiJ9fX0=");
                map.put(EntityType.RAVAGER, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2QyMGJmNTJlYzM5MGEwNzk5Mjk5MTg0ZmM2NzhiZjg0Y2Y3MzJiYjFiZDc4ZmQxYzRiNDQxODU4ZjAyMzVhOCJ9fX0=");
                map.put(EntityType.SALMON, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBlYTlhMjIzNjIwY2RiNTRiMzU3NDEzZDQzYmQ4OWM0MDA4YmNhNmEyMjdmM2I3ZGI5N2Y3NzMzZWFkNWZjZiJ9fX0=");
                map.put(EntityType.SHEEP, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMxZjljY2M2YjNlMzJlY2YxM2I4YTExYWMyOWNkMzNkMThjOTVmYzczZGI4YTY2YzVkNjU3Y2NiOGJlNzAifX19");
                map.put(EntityType.SILVERFISH, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGE5MWRhYjgzOTFhZjVmZGE1NGFjZDJjMGIxOGZiZDgxOWI4NjVlMWE4ZjFkNjIzODEzZmE3NjFlOTI0NTQwIn19fQ==");
                map.put(EntityType.SLIME, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODk1YWVlYzZiODQyYWRhODY2OWY4NDZkNjViYzQ5NzYyNTk3ODI0YWI5NDRmMjJmNDViZjNiYmI5NDFhYmU2YyJ9fX0=");
                map.put(EntityType.SNOWMAN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGU4ZDIwNmY2MWU2ZGU4YTc5ZDBjYjBiY2Q5OGFjZWQ0NjRjYmZlZmM5MjFiNDE2MGEyNTI4MjE2MzExMmEifX19");
                map.put(EntityType.SPIDER, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ==");
                map.put(EntityType.SQUID, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDljMmM5Y2U2N2ViNTk3MWNjNTk1ODQ2M2U2YzlhYmFiOGU1OTlhZGMyOTVmNGQ0MjQ5OTM2YjAwOTU3NjlkZCJ9fX0=");
                map.put(EntityType.STRAY, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM1MDk3OTE2YmMwNTY1ZDMwNjAxYzBlZWJmZWIyODcyNzdhMzRlODY3YjRlYTQzYzYzODE5ZDUzZTg5ZWRlNyJ9fX0=");
                map.put(EntityType.STRIDER, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2I3ZmZkZGE2NTZjNjhkODg4NTFhOGUwNWI0OGNkMjQ5Mzc3M2ZmYzRhYjdkNjRlOTMwMjIyOWZlMzU3MTA1OSJ9fX0=");
                map.put(EntityType.TRADER_LLAMA, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODQyNDc4MGIzYzVjNTM1MWNmNDlmYjViZjQxZmNiMjg5NDkxZGY2YzQzMDY4M2M4NGQ3ODQ2MTg4ZGI0Zjg0ZCJ9fX0=");
                map.put(EntityType.TROPICAL_FISH, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDZkZDVlNmFkZGI1NmFjYmM2OTRlYTRiYTU5MjNiMWIyNTY4ODE3OGZlZmZhNzIyOTAyOTllMjUwNWM5NzI4MSJ9fX0=");
                map.put(EntityType.TURTLE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMGE0MDUwZTdhYWNjNDUzOTIwMjY1OGZkYzMzOWRkMTgyZDdlMzIyZjlmYmNjNGQ1Zjk5YjU3MThhIn19fQ==");
                map.put(EntityType.VEX, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzJlYzVhNTE2NjE3ZmYxNTczY2QyZjlkNWYzOTY5ZjU2ZDU1NzVjNGZmNGVmZWZhYmQyYTE4ZGM3YWI5OGNkIn19fQ==");
                map.put(EntityType.VILLAGER, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDFiODMwZWI0MDgyYWNlYzgzNmJjODM1ZTQwYTExMjgyYmI1MTE5MzMxNWY5MTE4NDMzN2U4ZDM1NTU1ODMifX19");
                map.put(EntityType.VINDICATOR, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmRlYWVjMzQ0YWIwOTViNDhjZWFkNzUyN2Y3ZGVlNjFiMDYzZmY3OTFmNzZhOGZhNzY2NDJjODY3NmUyMTczIn19fQ==");
                map.put(EntityType.WANDERING_TRADER, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYxMzc5YTgyMjkwZDdhYmUxZWZhYWJiYzcwNzEwZmYyZWMwMmRkMzRhZGUzODZiYzAwYzkzMGM0NjFjZjkzMiJ9fX0=");
                map.put(EntityType.WITCH, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBlMTNkMTg0NzRmYzk0ZWQ1NWFlYjcwNjk1NjZlNDY4N2Q3NzNkYWMxNmY0YzNmODcyMmZjOTViZjlmMmRmYSJ9fX0=");
                map.put(EntityType.WOLF, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDA0OThkZTZmNWIwOWUwY2UzNWE3MjkyZmU1MGI3OWZjZTkwNjVkOWJlOGUyYTg3YzdhMTM1NjZlZmIyNmQ3MiJ9fX0=");
                map.put(EntityType.ZOGLIN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTY3ZTE4NjAyZTAzMDM1YWQ2ODk2N2NlMDkwMjM1ZDg5OTY2NjNmYjllYTQ3NTc4ZDNhN2ViYmM0MmE1Y2NmOSJ9fX0=");
                map.put(EntityType.ZOMBIFIED_PIGLIN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2VhYmFlY2M1ZmFlNWE4YTQ5Yzg4NjNmZjQ4MzFhYWEyODQxOThmMWEyMzk4ODkwYzc2NWUwYThkZTE4ZGE4YyJ9fX0=");
                map.put(EntityType.ZOMBIE_HORSE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDIyOTUwZjJkM2VmZGRiMThkZTg2ZjhmNTVhYzUxOGRjZTczZjEyYTZlMGY4NjM2ZDU1MWQ4ZWI0ODBjZWVjIn19fQ==");
                map.put(EntityType.ZOMBIE_VILLAGER, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVlMDhhODc3NmMxNzY0YzNmZTZhNmRkZDQxMmRmY2I4N2Y0MTMzMWRhZDQ3OWFjOTZjMjFkZjRiZjNhYzg5YyJ9fX0=");
                map.put(EntityType.SKELETON_HORSE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDdlZmZjZTM1MTMyYzg2ZmY3MmJjYWU3N2RmYmIxZDIyNTg3ZTk0ZGYzY2JjMjU3MGVkMTdjZjg5NzNhIn19fQ==");
                return map;
            },
            "Head texture values for each entity type.",
            "You can take some from http://minecraft-heads.com",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html"
        ).setWriter((cfg, path, map) -> map.forEach((type, txt) -> cfg.set(path + "." + type.name(), txt))).read(cfg);

        /*for (String sType : cfg.getSection("Settings.Head_Item.Textures")) {
            this.headTextures.put(sType.toUpperCase(), cfg.getString("Settings.Head_Item.Textures." + sType, ""));
        }
        this.cfg.setComments("Settings.Head_Item.Textures",
            "Head texture values for each entity type.",
            "You can take some from http://minecraft-heads.com",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html");

        this.headTextures = new JOption<Map<String, String>>("Settings.Head_Item.Textures",
            (cfg, path, def) -> cfg.getSection(path).stream().collect(Collectors.toMap(String::toUpperCase, v -> cfg.getString(path + "." + v, ""))),
            HashMap::new,
            "Head texture values for each entity type.",
            "You can take some from http://minecraft-heads.com",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html").read(cfg);*/
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
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
                entityName = this.headName.replace(Placeholders.GENERIC_TYPE, entity.getName());
                meta.setOwningPlayer(player);
            }
            else {
                String texture = this.headTextures.get(entity.getType());
                if (texture == null) return false;

                entityName = this.headName.replace(Placeholders.GENERIC_TYPE, LangManager.getEntityType(entity.getType()));
                ItemUtil.setSkullTexture(item, texture);
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
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!(e.getBlock().getState() instanceof Skull skull)) return;

        ItemStack skullItem = e.getItemInHand();
        PDCUtil.getString(skullItem, this.skullKey).ifPresent(type -> {
            PDCUtil.set(skull, this.skullKey, type);
            skull.update(true);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockDropItemEvent e) {
        if (!(e.getBlockState() instanceof Skull skull)) return;

        PDCUtil.getString(skull, this.skullKey).ifPresent(type -> {
            EntityType entityType = StringUtil.getEnum(type, EntityType.class).orElse(null);
            if (entityType == null) return;

            String texture = this.headTextures.get(entityType);
            if (texture == null) return;

            e.getItems().forEach(item -> {
                ItemStack drop = item.getItemStack();
                if (drop.getType() == Material.PLAYER_HEAD) {
                    ItemUtil.setSkullTexture(drop, texture);
                    ItemUtil.mapMeta(drop, meta -> {
                        String name = this.headName.replace(Placeholders.GENERIC_TYPE, LangManager.getEntityType(entityType));
                        meta.setDisplayName(name);
                        PDCUtil.set(meta, this.skullKey, type);
                    });
                }
                item.setItemStack(drop);
            });
        });
    }
}
