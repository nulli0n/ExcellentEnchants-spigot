package su.nightexpress.excellentenchants.api.item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.EnchantsFiles;
import su.nightexpress.excellentenchants.bridge.ItemTagLookup;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.LowerCase;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ItemSetRegistry {

    private final Map<String, ItemSet> registryMap;
    private final ItemTagLookup        tagLookup;
    private final Path                 file;

    public ItemSetRegistry(@NotNull Path dataDir, @NotNull ItemTagLookup tagLookup) {
        this.registryMap = new HashMap<>();
        this.tagLookup = tagLookup;
        this.file = Path.of(dataDir.toString(), EnchantsFiles.FILE_ITEM_TYPES);
    }

    @Nullable
    public ItemSet getByKey(@NotNull String id) {
        return this.registryMap.get(LowerCase.INTERNAL.apply(id));
    }

    @NotNull
    public Set<ItemSet> values() {
        return Set.copyOf(this.registryMap.values());
    }

    public void load() {
        ItemSetDefaults.initializeAll(this.tagLookup);

        FileConfig config = FileConfig.load(this.file);

        if (config.getSection("Categories").isEmpty()) {
            ItemSetDefaults.stream().map(ItemSetDefaults::getItemSet).forEach(itemSet -> {
                config.set("Categories." + itemSet.getId(), itemSet);
            });
        }

        config.getSection("Categories").forEach(sId -> {
            ItemSet category = ItemSet.read(config, "Categories." + sId, sId);
            this.registryMap.put(LowerCase.INTERNAL.apply(sId), category);
        });

        config.saveChanges();
    }
}
