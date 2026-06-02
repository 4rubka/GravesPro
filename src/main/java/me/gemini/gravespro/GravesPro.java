package me.gemini.gravespro;

import lombok.Getter;
import me.gemini.gravespro.command.GraveCommand;
import me.gemini.gravespro.listener.DeathListener;
import me.gemini.gravespro.listener.GraveListener;
import me.gemini.gravespro.listener.WorldListener;
import me.gemini.gravespro.manager.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

@Getter
public final class GravesPro extends JavaPlugin {

    private static GravesPro instance;
    
    private ConfigurationManager configurationManager;
    private StorageManager storageManager;
    private GraveManager graveManager;
    private HookManager hookManager;
    private GUIManager guiManager;
    private ParticleManager particleManager;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        instance = this;

        long start = System.currentTimeMillis();
        getLogger().info("Initializing GravesPro...");

        try {
            // Initialize Managers
            this.configurationManager = new ConfigurationManager(this);
            this.configurationManager.loadConfigs();

            this.storageManager = new StorageManager(this);
            this.storageManager.init();

            this.hookManager = new HookManager(this);
            this.hookManager.init();

            this.graveManager = new GraveManager(this);
            this.graveManager.init();

            this.guiManager = new GUIManager(this);
            this.guiManager.init();

            this.particleManager = new ParticleManager(this);
            this.particleManager.init();

            this.statsManager = new StatsManager(this);

            // Register Listeners
            getServer().getPluginManager().registerEvents(new DeathListener(this), this);
            getServer().getPluginManager().registerEvents(new WorldListener(this), this);
            getServer().getPluginManager().registerEvents(new GraveListener(this), this);

            // Register Commands
            getCommand("grave").setExecutor(new GraveCommand(this));

            getLogger().info("GravesPro enabled successfully in " + (System.currentTimeMillis() - start) + "ms!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable GravesPro!", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (hookManager != null) {
            hookManager.disable();
        }
        if (storageManager != null) {
            storageManager.close();
        }
        getLogger().info("GravesPro is being disabled...");
    }

    public static GravesPro getInstance() {
        return instance;
    }
}
