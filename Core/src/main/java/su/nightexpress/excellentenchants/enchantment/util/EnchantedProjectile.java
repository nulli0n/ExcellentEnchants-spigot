package su.nightexpress.excellentenchants.enchantment.util;

import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantedProjectile {

    private final Projectile               projectile;
    private final ItemStack                item;
    private final Set<UniParticle>         particles;
    private final Map<BowEnchant, Integer> enchantments;

    public EnchantedProjectile(@NotNull Projectile projectile, @Nullable ItemStack item) {
        this.projectile = projectile;
        this.item = item;

        this.particles = ConcurrentHashMap.newKeySet();
        this.enchantments = new HashMap<>();
    }

    public boolean isValid() {
        return this.projectile.isValid() && !this.projectile.isDead();
    }

    public void playParticles() {
        this.particles.forEach(entry -> {
            entry.play(this.projectile.getLocation(), 0f, 0f, 10);
        });
    }

    public void addParticle(@NotNull UniParticle particle) {
        this.particles.add(particle);
    }

    @NotNull
    public Projectile getProjectile() {
        return projectile;
    }

    @Nullable
    public ItemStack getItem() {
        return item;
    }

    @NotNull
    public Set<UniParticle> getParticles() {
        return particles;
    }

    @NotNull
    public Map<BowEnchant, Integer> getEnchantments() {
        return enchantments;
    }
}
