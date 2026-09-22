package su.nightexpress.excellentenchants.api;

import org.jspecify.annotations.NullMarked;

import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;

import java.util.Collections;
import java.util.List;

@NullMarked
public class EnchantTranslation implements Writeable {

    private final String       displayName;
    private final List<String> description;

    public EnchantTranslation(String displayName, List<String> description) {
        this.displayName = displayName;
        this.description = description;
    }

    public static EnchantTranslation read(FileConfig config, String path) {
        String displayName = ConfigValue.create(path + ".DisplayName",
            "null",
            "Translated enchantment display name.",
            "https://docs.advntr.dev/minimessage/format.html#standard-tags",
            "[*] Only one color and decorations are allowed.",
            "[*] Reboot required when changed."
        ).read(config);

        List<String> description = ConfigValue.create(path + ".Description",
            Collections.emptyList(),
            "Translated enchantment description.",
            "[*] Reboot required when changed."
        ).read(config);

        return new EnchantTranslation(displayName, description);
    }

    @Override
    public void write(FileConfig config, String path) {
        config.set(path + ".DisplayName", this.displayName);
        config.set(path + ".Description", this.description);
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public List<String> getDescription() {
        return this.description;
    }
}
