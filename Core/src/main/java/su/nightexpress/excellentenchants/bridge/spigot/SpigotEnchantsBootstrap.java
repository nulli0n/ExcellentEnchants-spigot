package su.nightexpress.excellentenchants.bridge.spigot;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.nms.mc_1_21_11.RegistryHack_1_21_11;
import su.nightexpress.excellentenchants.EnchantsKeys;
import su.nightexpress.excellentenchants.api.item.ItemSetRegistry;
import su.nightexpress.excellentenchants.bridge.RegistryHack;
import su.nightexpress.excellentenchants.enchantment.DistributionConfig;
import su.nightexpress.excellentenchants.enchantment.EnchantCatalog;
import su.nightexpress.excellentenchants.enchantment.EnchantRegistry;
import su.nightexpress.excellentenchants.nms.mc_1_21_10.RegistryHack_1_21_10;
import su.nightexpress.excellentenchants.nms.mc_1_21_8.RegistryHack_1_21_8;
import su.nightexpress.nightcore.util.Version;

import java.nio.file.Path;

public class SpigotEnchantsBootstrap {

    public void bootstrap(@NotNull EnchantsPlugin plugin) {
        RegistryHack registryHack = switch (Version.getCurrent()) {
            case MC_1_21_8 -> new RegistryHack_1_21_8(plugin);
            case MC_1_21_10 -> new RegistryHack_1_21_10(plugin);
            case MC_1_21_11 -> new RegistryHack_1_21_11(plugin);
            default -> null;
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

        EnchantCatalog.loadAll(dataDirectory, itemSetRegistry, (entry, exception) -> plugin.error("Could not load '%s' enchantment: '%s'".formatted(entry.getId(), exception.getMessage())));

        EnchantCatalog.enabled().forEach(catalog -> registryHack.registerEnchantment(catalog, distributionConfig));

        EnchantRegistry.getRegistered().forEach(registryHack::addExclusives);

        registryHack.freezeRegistry();
    }
}
