package me.gemini.gravespro.manager;

import lombok.Getter;
import me.gemini.gravespro.GravesPro;
import me.gemini.gravespro.model.Grave;
import me.gemini.gravespro.model.GraveState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GraveManager {

    private final GravesPro plugin;
    @Getter
    private final Map<UUID, Grave> activeGraves = new ConcurrentHashMap<>();

    public GraveManager(GravesPro plugin) {
        this.plugin = plugin;
    }

    public void init() {
        plugin.getStorageManager().getProvider().loadAllGraves().thenAccept(graves -> {
            for (Grave grave : graves) {
                activeGraves.put(grave.getId(), grave);
            }
            plugin.getLogger().info("Loaded " + graves.size() + " active graves from database.");
        });

        // Start the state machine task
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateGraveStates, 20L, 20L); // Every second
    }

    public void createGrave(Player player, Location location, Map<Integer, ItemStack> items, int xp, String deathCause) {
        long expirationTime = System.currentTimeMillis() + (plugin.getConfigurationManager().getConfig("settings.yml").getLong("grave.expiration-time") * 1000L);
        
        plugin.getLogger().info("Creating grave for " + player.getName() + " with " + items.size() + " slots and " + xp + " xp.");

        Grave grave = Grave.builder()
                .id(UUID.randomUUID())
                .ownerId(player.getUniqueId())
                .ownerName(player.getName())
                .location(findSafeLocation(location))
                .creationTime(System.currentTimeMillis())
                .expirationTime(expirationTime)
                .state(GraveState.PROTECTED)
                .items(new HashMap<>(items))
                .experience(xp)
                .deathCause(deathCause)
                .build();

        activeGraves.put(grave.getId(), grave);
        plugin.getStorageManager().getProvider().saveGrave(grave).exceptionally(e -> {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to save grave to database!", e);
            return null;
        });
        
        // Spawn the grave block (must be on main thread)
        Bukkit.getScheduler().runTask(plugin, () -> {
            spawnGraveBlock(grave);
            // Effects
            Location loc = grave.getLocation().clone().add(0.5, 0.5, 0.5);
            loc.getWorld().spawnParticle(org.bukkit.Particle.SOUL, loc, 30, 0.3, 0.3, 0.3, 0.05);
            loc.getWorld().spawnParticle(org.bukkit.Particle.SOUL_FIRE_FLAME, loc, 10, 0.2, 0.2, 0.2, 0.02);
            loc.getWorld().playSound(loc, org.bukkit.Sound.BLOCK_ANVIL_LAND, 1.0f, 0.5f);
            loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_WITHER_SPAWN, 0.3f, 1.2f);
            loc.getWorld().playSound(loc, org.bukkit.Sound.BLOCK_END_PORTAL_SPAWN, 0.5f, 1.0f);
        });
    }

    public void spawnGraveBlock(Grave grave) {
        Location loc = grave.getLocation();
        if (!loc.isChunkLoaded()) return;

        Block block = loc.getBlock();
        Material graveMaterial = Material.valueOf(plugin.getConfigurationManager().getConfig("settings.yml").getString("grave.block", "PLAYER_HEAD"));
        block.setType(graveMaterial);
        
        if (block.getState() instanceof org.bukkit.block.Skull skull) {
            skull.setOwningPlayer(Bukkit.getOfflinePlayer(grave.getOwnerId()));
            skull.update();
        }
        
        // Spawn Hologram
        if (plugin.getConfigurationManager().getConfig("settings.yml").getBoolean("hologram.enabled")) {
            plugin.getHookManager().getHologramProvider().createHologram(grave);
        }
    }

    private Location findSafeLocation(Location loc) {
        Location safe = loc.clone();
        
        if (safe.getY() < safe.getWorld().getMinHeight()) {
            safe.setY(safe.getWorld().getMinHeight() + 1);
        }
        
        if (safe.getY() > safe.getWorld().getMaxHeight()) {
            safe.setY(safe.getWorld().getMaxHeight() - 1);
        }

        Block block = safe.getBlock();
        if (block.getType().isAir() || block.isLiquid()) {
            for (int y = safe.getBlockY(); y > safe.getWorld().getMinHeight(); y--) {
                Block b = safe.getWorld().getBlockAt(safe.getBlockX(), y, safe.getBlockZ());
                if (b.getType().isSolid()) {
                    safe.setY(y + 1);
                    return safe;
                }
            }
            for (int y = safe.getBlockY(); y < safe.getWorld().getMaxHeight(); y++) {
                Block b = safe.getWorld().getBlockAt(safe.getBlockX(), y, safe.getBlockZ());
                if (b.getType().isSolid()) {
                    safe.setY(y + 1);
                    return safe;
                }
            }
        }
        
        return safe;
    }

    private void updateGraveStates() {
        for (Grave grave : activeGraves.values()) {
            if (grave.isExpired()) {
                expireGrave(grave);
            } else {
                // Update Hologram
                if (plugin.getConfigurationManager().getConfig("settings.yml").getBoolean("hologram.enabled")) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getHookManager().getHologramProvider().updateHologram(grave);
                        // Ambient particles
                        if (Math.random() < 0.1) { // 10% chance per second
                            Location particleLoc = grave.getLocation().clone().add(0.5, 0.5, 0.5);
                            particleLoc.getWorld().spawnParticle(org.bukkit.Particle.SOUL, particleLoc, 1, 0.2, 0.2, 0.2, 0.01);
                        }
                    });
                }
            }
        }
    }

    public void expireGrave(Grave grave) {
        grave.setState(GraveState.EXPIRED);
        removeGrave(grave);
    }

    public void removeGrave(Grave grave) {
        activeGraves.remove(grave.getId());
        plugin.getStorageManager().getProvider().deleteGrave(grave.getId());
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            Block block = grave.getLocation().getBlock();
            Material graveMaterial = Material.valueOf(plugin.getConfigurationManager().getConfig("settings.yml").getString("grave.block", "PLAYER_HEAD"));
            if (block.getType() == graveMaterial) {
                block.setType(Material.AIR);
            }
            // Remove hologram
            plugin.getHookManager().getHologramProvider().deleteHologram(grave);
        });
    }
}
