package su.nightexpress.excellentenchants.bridge.spigot;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.enchantment.EnchantRegistry;
import su.nightexpress.excellentenchants.api.item.ItemSet;
import su.nightexpress.excellentenchants.api.item.ItemSetRegistry;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDefinition;
import su.nightexpress.excellentenchants.bridge.DistributionConfig;
import su.nightexpress.excellentenchants.enchantment.EnchantDataRegistry;
import su.nightexpress.excellentenchants.bridge.RegistryHack;
import su.nightexpress.excellentenchants.nms.mc_1_21_10.RegistryHack_1_21_10;
import su.nightexpress.excellentenchants.nms.mc_1_21_7.RegistryHack_1_21_7;
import su.nightexpress.excellentenchants.nms.mc_1_21_8.RegistryHack_1_21_8;
import su.nightexpress.excellentenchants.nms.v1_21_4.RegistryHack_1_21_4;
import su.nightexpress.excellentenchants.nms.v1_21_5.RegistryHack_1_21_5;
import su.nightexpress.nightcore.util.Version;

import java.nio.file.Path;

public class SpigotEnchantsBootstrap {

    public void bootstrap(@NotNull EnchantsPlugin plugin) {
        RegistryHack registryHack = switch (Version.getCurrent()) {
            case MC_1_21_4 -> new RegistryHack_1_21_4(plugin);
            case MC_1_21_5 -> new RegistryHack_1_21_5(plugin);
            case MC_1_21_7 -> new RegistryHack_1_21_7(plugin);
            case MC_1_21_8 -> new RegistryHack_1_21_8(plugin);
            case MC_1_21_10 -> new RegistryHack_1_21_10(plugin);
            default -> null;
        };

        if (registryHack == null) {
            plugin.error("Unsupported server version!");
            plugin.getPluginManager().disablePlugin(plugin);
            return;
        }

        Path dataDir = plugin.getDataFolder().toPath();

        DistributionConfig.load(dataDir); // Load distribution config.

        registryHack.unfreezeRegistry();

        ItemSetRegistry.initialize(dataDir, new SpigotItemTagLookup()); // Load ItemSets
        ItemSetRegistry.getMap().forEach(registryHack::createItemsSet); // Register tags for our ItemSet objects.

        EnchantDataRegistry.initialize(dataDir, false);
        EnchantDataRegistry.getMap().forEach((enchantId, data) -> {
            EnchantDefinition definition = data.getDefinition();

            String primaryId = definition.getPrimaryItemsId();
            String supportedId = definition.getSupportedItemsId();

            ItemSet primarySet = ItemSetRegistry.getByKey(primaryId);
            ItemSet supportedSet = ItemSetRegistry.getByKey(supportedId);
            if (primarySet == null || supportedSet == null) {
                plugin.error("Could not register enchantment '%s' due to invalid primary/supported items sets: %s/%s".formatted(enchantId, primaryId, supportedId));
                return;
            }

            registryHack.registerEnchantment(enchantId, data, supportedSet.getSlots());
        });

        EnchantRegistry.getRegistered().forEach(registryHack::addExclusives); // Add exclusives sets to registered enchantments.

        registryHack.freezeRegistry();
    }
}
