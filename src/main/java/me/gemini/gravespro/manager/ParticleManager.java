package me.gemini.gravespro.manager;

import me.gemini.gravespro.GravesPro;
import me.gemini.gravespro.model.Grave;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ParticleManager {

    private final GravesPro plugin;
    private final Set<UUID> locatingPlayers = new HashSet<>();

    public ParticleManager(GravesPro plugin) {
        this.plugin = plugin;
    }

    public void init() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::showParticles, 20L, 20L);
    }

    public void toggleLocate(Player player) {
        if (locatingPlayers.contains(player.getUniqueId())) {
            locatingPlayers.remove(player.getUniqueId());
        } else {
            locatingPlayers.add(player.getUniqueId());
        }
    }

    private void showParticles() {
        for (UUID uuid : locatingPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                locatingPlayers.remove(uuid);
                continue;
            }

            for (Grave grave : plugin.getGraveManager().getActiveGraves().values()) {
                if (grave.getOwnerId().equals(uuid) && grave.getLocation().getWorld().equals(player.getWorld())) {
                    spawnTrail(player, grave.getLocation());
                }
            }
        }
    }

    private void spawnTrail(Player player, Location target) {
        Location start = player.getLocation().add(0, 1, 0);
        org.bukkit.util.Vector direction = target.toVector().subtract(start.toVector()).normalize();
        
        for (double d = 1; d < 10; d += 0.5) {
            Location point = start.clone().add(direction.clone().multiply(d));
            player.spawnParticle(Particle.DUST, point, 1, new Particle.DustOptions(Color.LIME, 1));
        }
    }
}
