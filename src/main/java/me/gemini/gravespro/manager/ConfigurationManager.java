package me.gemini.gravespro.manager;

import me.gemini.gravespro.GravesPro;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigurationManager {

    private final GravesPro plugin;
    private final Map<String, ConfigFile> configs = new HashMap<>();

    public ConfigurationManager(GravesPro plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        setupConfig("settings.yml");
        setupConfig("messages.yml");
        setupConfig("gui.yml");
    }

    private void setupConfig(String name) {
        ConfigFile configFile = new ConfigFile(name);
        configFile.saveDefaultConfig();
        configFile.reloadConfig();
        configs.put(name, configFile);
    }

    public FileConfiguration getConfig(String name) {
        ConfigFile configFile = configs.get(name);
        return configFile != null ? configFile.getConfig() : null;
    }

    public void reloadConfigs() {
        for (ConfigFile configFile : configs.values()) {
            configFile.reloadConfig();
        }
    }

    private class ConfigFile {
        private final String fileName;
        private FileConfiguration config;
        private File configFile;

        public ConfigFile(String fileName) {
            this.fileName = fileName;
        }

        public void reloadConfig() {
            if (configFile == null) {
                configFile = new File(plugin.getDataFolder(), fileName);
            }
            config = YamlConfiguration.loadConfiguration(configFile);

            InputStream defConfigStream = plugin.getResource(fileName);
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
                config.setDefaults(defConfig);
            }
        }

        public FileConfiguration getConfig() {
            if (config == null) {
                reloadConfig();
            }
            return config;
        }

        public void saveConfig() {
            if (config == null || configFile == null) {
                return;
            }
            try {
                getConfig().save(configFile);
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
            }
        }

        public void saveDefaultConfig() {
            if (configFile == null) {
                configFile = new File(plugin.getDataFolder(), fileName);
            }
            if (!configFile.exists()) {
                plugin.saveResource(fileName, false);
            }
        }
    }
}
