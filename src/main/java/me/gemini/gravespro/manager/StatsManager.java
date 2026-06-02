package me.gemini.gravespro.manager;

import me.gemini.gravespro.GravesPro;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatsManager {
    private final GravesPro plugin;
    private final Map<UUID, Integer> deathCounts = new ConcurrentHashMap<>();

    public StatsManager(GravesPro plugin) {
        this.plugin = plugin;
    }

    public void incrementDeath(UUID playerId) {
        deathCounts.merge(playerId, 1, Integer::sum);
    }

    public int getDeaths(UUID playerId) {
        return deathCounts.getOrDefault(playerId, 0);
    }
}
