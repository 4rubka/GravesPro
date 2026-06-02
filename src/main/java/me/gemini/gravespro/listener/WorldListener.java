package me.gemini.gravespro.listener;

import me.gemini.gravespro.GravesPro;
import me.gemini.gravespro.model.Grave;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class WorldListener implements Listener {

    private final GravesPro plugin;

    public WorldListener(GravesPro plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        
        for (Grave grave : plugin.getGraveManager().getActiveGraves().values()) {
            if (grave.getLocation().getWorld().equals(chunk.getWorld()) &&
                grave.getLocation().getBlockX() >> 4 == chunk.getX() &&
                grave.getLocation().getBlockZ() >> 4 == chunk.getZ()) {
                
                plugin.getGraveManager().spawnGraveBlock(grave);
            }
        }
    }
}
