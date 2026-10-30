package su.nightexpress.excellentenchants.bridge.spigot;

import java.nio.file.Path;

import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsKeys;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.item.ItemSetRegistry;
import su.nightexpress.excellentenchants.bridge.RegistryHack;
import su.nightexpress.excellentenchants.enchantment.DistributionConfig;
import su.nightexpress.excellentenchants.enchantment.EnchantCatalog;
import su.nightexpress.excellentenchants.enchantment.EnchantRegistry;
import su.nightexpress.excellentenchants.nms.mc_1_21_10.RegistryHack_1_21_10;
import su.nightexpress.excellentenchants.nms.mc_1_21_11.RegistryHack_1_21_11;
import su.nightexpress.excellentenchants.nms.mc_1_21_8.RegistryHack_1_21_8;
import su.nightexpress.excellentenchants.nms.mc_26_1_2.RegistryHack_26_1_2;
import su.nightexpress.nightcore.util.Version;

@NullMarked
public class SpigotEnchantsBootstrap {

    public void bootstrap(EnchantsPlugin plugin) {
        RegistryHack registryHack = switch (Version.getCurrent()) {
            case MC_1_21_8 -> new RegistryHack_1_21_8(plugin);
            case MC_1_21_10 -> new RegistryHack_1_21_10(plugin);
            case MC_1_21_11 -> new RegistryHack_1_21_11(plugin);
            case MC_26_1, MC_26_1_1, MC_26_1_2, MC_26_2, MC_26_3, MC_26_4 -> new RegistryHack_26_1_2(plugin);
            default -> {
                if (Version.isAbove(Version.MC_26_1_2) || Version.getCurrent() == Version.UNKNOWN) {
                    plugin.warn("Unrecognized or newer server version (" + Version.getCurrent().getLocalized() + "), attempting 26.x registry hack...");
                    RegistryHack fallback = null;
                    try {
                        fallback = new RegistryHack_26_1_2(plugin);
                    } catch (Throwable t) {
                        plugin.error("Failed to initialize registry hack: " + t.getMessage());
                    }
                    yield fallback;
                }
                yield null;
            }
        };

        if (registryHack == null) {
            plugin.error("Unsupported server version!");
            plugin.getPluginManager().disablePlugin(plugin);
            return;
        }

        Path dataDirectory = plugin.getDataFolder().toPath();

        DistributionConfig distributionConfig = DistributionConfig.load(dataDirectory);
        if (distributionConfig.isUseMinecraftNamespace()) {
            EnchantsKeys.setVanillaNamespace();
        }

        registryHack.unfreezeRegistry();

        SpigotItemTagLookup tagLookup = new SpigotItemTagLookup();
        ItemSetRegistry itemSetRegistry = new ItemSetRegistry(dataDirectory, tagLookup);

        // Load ItemTag set objects and register server Tags for them.
        itemSetRegistry.load();
        itemSetRegistry.values().forEach(registryHack::createItemsSet);

        EnchantCatalog.loadAll(dataDirectory, itemSetRegistry, (entry, exception) -> plugin.error(
            "Could not load '%s' enchantment: '%s'".formatted(entry.getId(), exception.getMessage())));

        EnchantCatalog.enabled().forEach(catalog -> registryHack.registerEnchantment(catalog, distributionConfig));

        EnchantRegistry.getRegistered().forEach(registryHack::addExclusives);

        registryHack.freezeRegistry();
    }
}
