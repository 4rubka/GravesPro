package me.gemini.gravespro.manager;

import lombok.Getter;
import me.gemini.gravespro.GravesPro;
import me.gemini.gravespro.api.StorageProvider;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;

public class StorageManager {

    private final GravesPro plugin;
    @Getter
    private StorageProvider provider;

    public StorageManager(GravesPro plugin) {
        this.plugin = plugin;
    }

    public void init() {
        FileConfiguration settings = plugin.getConfigurationManager().getConfig("settings.yml");
        String type = settings.getString("storage.type", "SQLITE").toUpperCase();

        if (type.equals("MYSQL")) {
            this.provider = new MySQLProvider(plugin);
        } else {
            this.provider = new SQLiteProvider(plugin);
        }

        this.provider.init().thenRun(() -> {
            plugin.getLogger().info("Database storage initialized using " + type);
        }).exceptionally(e -> {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database storage!", e);
            return null;
        });
    }

    public void close() {
        if (provider != null) {
            provider.close();
        }
    }
}
