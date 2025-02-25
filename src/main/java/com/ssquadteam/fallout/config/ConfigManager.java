package com.ssquadteam.fallout.config;

import com.ssquadteam.fallout.Fallout;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Manages the plugin's configuration files
 */
public class ConfigManager {

    private final Fallout plugin;
    private FileConfiguration config;
    private File configFile;
    
    // Data files for radiation sources and player data
    private File sourcesFile;
    private FileConfiguration sourcesConfig;
    private File playerDataFile;
    private FileConfiguration playerDataConfig;

    public ConfigManager(Fallout plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.sourcesFile = new File(plugin.getDataFolder(), "sources.yml");
        this.playerDataFile = new File(plugin.getDataFolder(), "playerdata.yml");
    }

    /**
     * Load or create the main configuration file
     */
    public void loadConfig() {
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        
        config = plugin.getConfig();
        
        // Load or create the sources data file
        loadSourcesConfig();
        
        // Load or create the player data file
        loadPlayerDataConfig();
    }

    /**
     * Load or create the sources data file
     */
    private void loadSourcesConfig() {
        if (!sourcesFile.exists()) {
            try {
                if (!plugin.getDataFolder().exists()) {
                    plugin.getDataFolder().mkdirs();
                }
                sourcesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create sources.yml", e);
            }
        }
        
        sourcesConfig = YamlConfiguration.loadConfiguration(sourcesFile);
    }

    /**
     * Load or create the player data file
     */
    private void loadPlayerDataConfig() {
        if (!playerDataFile.exists()) {
            try {
                if (!plugin.getDataFolder().exists()) {
                    plugin.getDataFolder().mkdirs();
                }
                playerDataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create playerdata.yml", e);
            }
        }
        
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    /**
     * Get the main config
     * @return The main FileConfiguration
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Save the main config
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config.yml", e);
        }
    }

    /**
     * Get the sources config
     * @return The sources FileConfiguration
     */
    public FileConfiguration getSourcesConfig() {
        return sourcesConfig;
    }

    /**
     * Save the sources config
     */
    public void saveSourcesConfig() {
        try {
            sourcesConfig.save(sourcesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save sources.yml", e);
        }
    }

    /**
     * Get the player data config
     * @return The player data FileConfiguration
     */
    public FileConfiguration getPlayerDataConfig() {
        return playerDataConfig;
    }

    /**
     * Save the player data config
     */
    public void savePlayerDataConfig() {
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save playerdata.yml", e);
        }
    }

    /**
     * Reload all configuration files
     */
    public void reloadAll() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        sourcesConfig = YamlConfiguration.loadConfiguration(sourcesFile);
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }
} 