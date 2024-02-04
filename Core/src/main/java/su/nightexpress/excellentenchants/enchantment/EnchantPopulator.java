package su.nightexpress.excellentenchants.enchantment;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.ObtainSettings;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.api.enchantment.ObtainType;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.excellentenchants.tier.Tier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnchantPopulator {

    private final ExcellentEnchants                plugin;
    private final ObtainType                       obtainType;
    private final ItemStack                        item;
    private final Map<Tier, Set<ExcellentEnchant>> candidates;
    private final Map<Enchantment, Integer>        population;

    private World world;
    private Function<ExcellentEnchant, Integer> levelGenerator;

    public EnchantPopulator(@NotNull ExcellentEnchants plugin, @NotNull ItemStack item, @NotNull ObtainType obtainType) {
        this.plugin = plugin;
        this.item = item;
        this.obtainType = obtainType;
        this.candidates = new HashMap<>();
        this.population = new HashMap<>();
        this.withLevelGenerator(enchant -> enchant.generateLevel(this.getObtainType()));

        this.fillDefaultCandidates();
    }

    @NotNull
    public EnchantPopulator withWorld(@NotNull World world) {
        this.world = world;
        return this;
    }

    @NotNull
    public EnchantPopulator withLevelGenerator(@NotNull Function<ExcellentEnchant, Integer> levelGenerator) {
        this.levelGenerator = levelGenerator;
        return this;
    }

    @NotNull
    public EnchantPopulator withDefaultPopulation(@NotNull Map<Enchantment, Integer> population) {
        this.getPopulation().putAll(population);
        return this;
    }

    private void fillDefaultCandidates() {
        this.plugin.getTierManager().getTiers().forEach(tier -> {
            Set<ExcellentEnchant> enchants = EnchantRegistry.getOfTier(tier);

            enchants.removeIf(enchant -> {
                return !enchant.isObtainable(this.getObtainType()) || (!enchant.getBackend().canEnchantItem(this.getItem()) && !EnchantUtils.isBook(this.getItem()));
            });

            this.candidates.put(tier, enchants);
        });
    }

    public boolean isEmpty() {
        return this.getCandidates().isEmpty() || this.getCandidates().values().stream().allMatch(Set::isEmpty);
    }

    public boolean isEmpty(@NotNull Tier tier) {
        return this.getCandidates(tier).isEmpty();
    }

    public void purge(@NotNull Tier tier) {
        this.getCandidates().remove(tier);
    }

    public void purge(@NotNull Tier tier, @NotNull ExcellentEnchant enchant) {
        this.getCandidates(tier).remove(enchant);
        this.getCandidates().keySet().removeIf(this::isEmpty);
    }

    @NotNull
    public ItemStack getItem() {
        return item;
    }

    @NotNull
    public ObtainType getObtainType() {
        return obtainType;
    }

    @Nullable
    public World getWorld() {
        return world;
    }

    @NotNull
    public Function<ExcellentEnchant, Integer> getLevelGenerator() {
        return levelGenerator;
    }

    @NotNull
    public Map<Tier, Set<ExcellentEnchant>> getCandidates() {
        return this.candidates;
    }

    @NotNull
    public Set<ExcellentEnchant> getCandidates(@NotNull Tier tier) {
        return this.getCandidates().getOrDefault(tier, new HashSet<>());
    }

    @NotNull
    public Map<Enchantment, Integer> getPopulation() {
        return this.population;
    }

    @Nullable
    public Tier getTierByChance() {
        Map<Tier, Double> map = this.getCandidates().keySet().stream()
            .filter(tier -> tier.getChance(this.getObtainType()) > 0D)
            .collect(Collectors.toMap(k -> k, v -> v.getChance(this.getObtainType()), (o, n) -> n, HashMap::new));
        if (map.isEmpty()) return null;

        return Rnd.getByWeight(map);
    }

    @Nullable
    public ExcellentEnchant getEnchantByChance(@NotNull Tier tier) {
        Map<ExcellentEnchant, Double> map = this.getCandidates(tier).stream()
            .collect(Collectors.toMap(k -> k, v -> v.getObtainChance(this.getObtainType())));
        return map.isEmpty() ? null : Rnd.getByWeight(map);
    }

    @NotNull
    public Map<Enchantment, Integer> createPopulation() {
        Map<Enchantment, Integer> population = this.getPopulation();

        ObtainSettings settings = Config.getObtainSettings(this.getObtainType()).orElse(null);
        if (settings == null || !Rnd.chance(settings.getEnchantsCustomGenerationChance())) return population;

        int enchantsLimit = settings.getEnchantsTotalMax();
        int enchantsRolled = Rnd.get(settings.getEnchantsCustomMin(), settings.getEnchantsCustomMax());

        // Try to populate as many as possible.
        while (!this.isEmpty() && enchantsRolled > 0) {
            // Limit reached.
            if (population.size() >= enchantsLimit) break;

            Tier tier = this.getTierByChance();
            if (tier == null) break; // no tiers left.

            ExcellentEnchant enchant = this.getEnchantByChance(tier);
            // Remove entire tier if no enchants can be selected.
            if (enchant == null) {
                this.purge(tier);
                continue;
            }

            // Remove disabled world enchants.
            if (world != null && enchant.isDisabledInWorld(world)) {
                this.purge(tier, enchant);
                continue;
            }

            // Remove conflicting enchants.
            if (population.keySet().stream().anyMatch(has -> has.conflictsWith(enchant.getBackend()) || enchant.getBackend().conflictsWith(has))) {
                this.purge(tier, enchant);
                continue;
            }

            // Level generation failed.
            int level = this.getLevelGenerator().apply(enchant);
            if (level < enchant.getStartLevel()) {
                this.purge(tier, enchant);
                continue;
            }

            // All good!
            this.purge(tier, enchant);
            population.put(enchant.getBackend(), level);
            enchantsRolled--;
        }

        return population;
    }

    public boolean populate() {
        ItemStack item = this.getItem();
        AtomicBoolean status = new AtomicBoolean(false);

        var population = this.getPopulation().isEmpty() ? this.createPopulation() : this.getPopulation();

        boolean singleVillagerBook = this.getObtainType() == ObtainType.VILLAGER
            && item.getType() == Material.ENCHANTED_BOOK
            && Config.ENCHANTMENTS_SINGLE_ENCHANT_IN_VILLAGER_BOOKS.get();

        if (singleVillagerBook) {
            if (!population.isEmpty()) {
                EnchantUtils.getAll(item).keySet().forEach(enchantment -> EnchantUtils.remove(item, enchantment));
            }
            while (population.size() > 1) {
                population.remove(Rnd.get(population.keySet()));
            }
        }

        population.forEach((enchantment, level) -> {
            if (EnchantUtils.add(item, enchantment, level, false)) {
                status.set(true);
            }
        });

        if (status.get()) {
            EnchantUtils.updateDisplay(item);
        }

        return status.get();
    }
}
