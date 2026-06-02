package me.gemini.gravespro.api;

import me.gemini.gravespro.model.Grave;
import org.bukkit.Location;

public interface HologramProvider {
    
    void createHologram(Grave grave);
    void updateHologram(Grave grave);
    void deleteHologram(Grave grave);
    
    void deleteAll();
}
