package su.nightexpress.excellentenchants.enchantment;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchantsAPI;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.excellentenchants.tier.Tier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EnchantPopulator {

    private final ObtainType                       obtainType;
    private final ItemStack                        item;
    private final Map<Tier, Set<ExcellentEnchant>> enchants;

    public EnchantPopulator(@NotNull ObtainType obtainType, @NotNull ItemStack item) {
        this.obtainType = obtainType;
        this.item = item;
        this.enchants = new HashMap<>();

        ExcellentEnchantsAPI.getTierManager().getTiers().forEach(tier -> {
            Set<ExcellentEnchant> enchants = EnchantRegistry.getOfTier(tier);
            this.enchants.put(tier, EnchantUtils.populateFilter(enchants, obtainType, item));
        });
    }

    public boolean isEmpty() {
        return this.getEnchants().isEmpty() || this.getEnchants().values().stream().allMatch(Set::isEmpty);
    }

    public boolean isEmpty(@NotNull Tier tier) {
        return this.getEnchants(tier).isEmpty();
    }

    public void purge(@NotNull Tier tier) {
        this.getEnchants().remove(tier);
    }

    public void purge(@NotNull Tier tier, @NotNull ExcellentEnchant enchant) {
        this.getEnchants(tier).remove(enchant);
        this.getEnchants().keySet().removeIf(this::isEmpty);
    }

    @NotNull
    public ObtainType getObtainType() {
        return obtainType;
    }

    @NotNull
    public ItemStack getItem() {
        return item;
    }

    @NotNull
    public Map<Tier, Set<ExcellentEnchant>> getEnchants() {
        return this.enchants;
    }

    @NotNull
    public Set<ExcellentEnchant> getEnchants(@NotNull Tier tier) {
        return this.getEnchants().getOrDefault(tier, new HashSet<>());
    }

    @Nullable
    public Tier getTierByChance() {
        if (this.getEnchants().keySet().isEmpty()) return null;
        return ExcellentEnchantsAPI.getTierManager().getTierByChance(this.getObtainType());
    }

    @Nullable
    public ExcellentEnchant getEnchantByChance(@NotNull Tier tier) {
        Map<ExcellentEnchant, Double> map = this.getEnchants(tier).stream()
            .collect(Collectors.toMap(k -> k, v -> v.getObtainChance(this.getObtainType())));
        return map.isEmpty() ? null : Rnd.getByWeight(map);
    }
}
