package su.nightexpress.excellentenchants.enchantment;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.DistributionWay;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.DistributionWaySettings;
import su.nightexpress.excellentenchants.enchantment.data.CustomDistribution;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.util.random.Rnd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

public class EnchantPopulator {

    //private final ExcellentEnchants                plugin;

    private final DistributionWay                   distributionWay;
    private final ItemStack                         item;
    private final Map<Rarity, Set<EnchantmentData>> candidates;
    private final Map<Enchantment, Integer>         defaultPopulation;
    private final Set<BiFunction<EnchantmentData, CustomDistribution, Boolean>>   predicates;

    private World world;
    private BiFunction<EnchantmentData, CustomDistribution, Integer> levelGenerator;

    public EnchantPopulator(@NotNull EnchantsPlugin plugin, @NotNull ItemStack item, @NotNull DistributionWay distributionWay) {
        //this.plugin = plugin;
        this.item = item;
        this.distributionWay = distributionWay;
        this.candidates = new HashMap<>();
        this.defaultPopulation = new HashMap<>();
        this.predicates = new HashSet<>();
        this.withLevelGenerator((data, distribution) -> distribution.generateLevel(this.getDistributionWay()));

        //this.fillDefaultCandidates();
    }

    @NotNull
    public EnchantPopulator withWorld(@NotNull World world) {
        this.world = world;
        return this;
    }

    @NotNull
    public EnchantPopulator withLevelGenerator(@NotNull BiFunction<EnchantmentData, CustomDistribution, Integer> levelGenerator) {
        this.levelGenerator = levelGenerator;
        return this;
    }

    @NotNull
    public EnchantPopulator withCondition(@NotNull BiFunction<EnchantmentData, CustomDistribution, Boolean> predicate) {
        this.predicates.add(predicate);
        return this;
    }

    @NotNull
    public EnchantPopulator withDefaultPopulation(@NotNull Map<Enchantment, Integer> population) {
        this.defaultPopulation.putAll(population);
        //this.getPopulation().putAll(population);
        return this;
    }

    private void fillDefaultCandidates() {
        for (Rarity rarity : Rarity.values()) {
            Set<EnchantmentData> dataSet = EnchantRegistry.getEnchantments(rarity);

            dataSet.removeIf(data -> {
                CustomDistribution distribution = (CustomDistribution) data.getDistributionOptions();

                // Check if can be distributed.
                if (!distribution.isDistributable(this.getDistributionWay())) return true;

                // Check for custom conditions.
                if (!this.predicates.isEmpty() && !this.predicates.stream().allMatch(predicate -> predicate.apply(data, distribution))) return true;

                // Enchanting books is always good.
                if (this.getItem().getType() == Material.BOOK && this.getDistributionWay() == DistributionWay.ENCHANTING) return false;

                // Check if item can be enchanted.
                return !data.getEnchantment().canEnchantItem(this.getItem()) && !EnchantUtils.isEnchantedBook(this.getItem());
            });

            this.candidates.put(rarity, dataSet);
        }
    }

    public boolean isEmpty() {
        return this.getCandidates().isEmpty() || this.getCandidates().values().stream().allMatch(Set::isEmpty);
    }

    public boolean isEmpty(@NotNull Rarity rarity) {
        return this.getCandidates(rarity).isEmpty();
    }

    public void purge(@NotNull Rarity rarity) {
        this.getCandidates().remove(rarity);
    }

    public void purge(@NotNull Rarity tier, @NotNull EnchantmentData enchant) {
        this.getCandidates(tier).remove(enchant);
        this.getCandidates().keySet().removeIf(this::isEmpty);
    }

    @NotNull
    public ItemStack getItem() {
        return item;
    }

    @NotNull
    public DistributionWay getDistributionWay() {
        return distributionWay;
    }

    @Nullable
    public World getWorld() {
        return world;
    }

    @NotNull
    public BiFunction<EnchantmentData, CustomDistribution, Integer> getLevelGenerator() {
        return levelGenerator;
    }

    @NotNull
    public Map<Rarity, Set<EnchantmentData>> getCandidates() {
        return this.candidates;
    }

    @NotNull
    public Set<EnchantmentData> getCandidates(@NotNull Rarity rarity) {
        return this.candidates.getOrDefault(rarity, new HashSet<>());
    }

    @Nullable
    public Rarity getRarityByWeight() {
        Map<Rarity, Double> map = new HashMap<>();

        for (Rarity rarity : this.getCandidates().keySet()) {
            map.put(rarity, (double) rarity.getWeight());
        }

        return map.isEmpty() ? null : Rnd.getByWeight(map);
    }

    @Nullable
    public EnchantmentData getEnchantmentByWeight(@NotNull Rarity rarity) {
        Map<EnchantmentData, Double> map = new HashMap<>();

        this.getCandidates(rarity).forEach(enchantmentData -> {
            CustomDistribution distribution = (CustomDistribution) enchantmentData.getDistributionOptions();
            map.put(enchantmentData, distribution.getWeight(this.getDistributionWay()));
        });

        return map.isEmpty() ? null : Rnd.getByWeight(map);
    }

    @NotNull
    public Map<Enchantment, Integer> createPopulation() {
        this.candidates.clear();
        this.fillDefaultCandidates();

        Map<Enchantment, Integer> population = new HashMap<>(this.defaultPopulation);

        DistributionWaySettings settings = Config.getDistributionWaySettings(this.getDistributionWay()).orElse(null);
        if (settings == null || !Rnd.chance(settings.getGenerationChance())) return population;

        int enchantsLimit = settings.getMaxEnchantments();
        int enchantsRolled = settings.rollAmount();

        // Try to populate as many as possible.
        while (!this.isEmpty() && enchantsRolled > 0) {
            // Limit reached.
            if (population.size() >= enchantsLimit) break;

            Rarity rarity = this.getRarityByWeight();
            if (rarity == null) break; // no tiers left.

            EnchantmentData enchantmentData = this.getEnchantmentByWeight(rarity);
            // Remove entire rarity if no enchants can be selected.
            if (enchantmentData == null) {
                this.purge(rarity);
                continue;
            }

            if (!(enchantmentData.getDistributionOptions() instanceof CustomDistribution distribution)) {
                this.purge(rarity, enchantmentData);
                continue;
            }

            // Remove disabled world enchants.
            if (this.world != null && !enchantmentData.isAvailableToUse(this.world)) {
                this.purge(rarity, enchantmentData);
                continue;
            }

            // Remove conflicting enchants.
            if (population.keySet().stream().anyMatch(has -> has.conflictsWith(enchantmentData.getEnchantment()) || enchantmentData.getEnchantment().conflictsWith(has))) {
                this.purge(rarity, enchantmentData);
                continue;
            }

            // Level generation failed.
            int level = this.getLevelGenerator().apply(enchantmentData, distribution);
            if (level < 1) {
                this.purge(rarity, enchantmentData);
                continue;
            }

            // All good!
            this.purge(rarity, enchantmentData);
            population.put(enchantmentData.getEnchantment(), level);
            enchantsRolled--;
        }

        return population;
    }

    public boolean populate() {
        ItemStack item = this.getItem();
        AtomicBoolean status = new AtomicBoolean(false);
        Map<Enchantment, Integer> population = this.createPopulation();//this.getPopulation().isEmpty() ? this.createPopulation() : this.getPopulation();

        boolean singleVillagerBook = this.getDistributionWay() == DistributionWay.VILLAGER
            && EnchantUtils.isEnchantedBook(item)
            && Config.DISTRIBUTION_SINGLE_ENCHANT_IN_VILLAGER_BOOKS.get();

        if (singleVillagerBook) {
            if (!population.isEmpty()) {
                EnchantUtils.removeAll(item);
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
