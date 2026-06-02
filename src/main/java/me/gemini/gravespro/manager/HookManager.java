package me.gemini.gravespro.manager;

import lombok.Getter;
import me.gemini.gravespro.GravesPro;
import me.gemini.gravespro.api.HologramProvider;
import me.gemini.gravespro.hook.InternalHologramProvider;
import org.bukkit.configuration.file.FileConfiguration;

public class HookManager {

    private final GravesPro plugin;
    @Getter
    private HologramProvider hologramProvider;

    public HookManager(GravesPro plugin) {
        this.plugin = plugin;
    }

    public void init() {
        FileConfiguration settings = plugin.getConfigurationManager().getConfig("settings.yml");
        
        // Holograms
        String holoType = settings.getString("hologram.provider", "INTERNAL").toUpperCase();
        if (holoType.equals("INTERNAL")) {
            this.hologramProvider = new InternalHologramProvider(plugin);
        }
        // TODO: Add DH and CMI hooks
        
        if (this.hologramProvider == null) {
             this.hologramProvider = new InternalHologramProvider(plugin);
        }
    }

    public void disable() {
        if (hologramProvider != null) {
            hologramProvider.deleteAll();
        }
    }
}
