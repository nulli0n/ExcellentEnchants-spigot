package su.nightexpress.excellentenchants.api.enchantment;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface Distribution {

    boolean isTreasure();

    boolean isDiscoverable();

    boolean isTradable();

    @NotNull Set<TradeType> getTrades();

    boolean isOnMobSpawnEquipment();

    boolean isOnRandomLoot();

    boolean isOnTradedEquipment();
}
