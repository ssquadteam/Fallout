package com.ssquadteam.fallout;

import com.ssquadteam.fallout.commands.RadCommandExecutor;
import com.ssquadteam.fallout.config.ConfigManager;
import com.ssquadteam.fallout.integration.MMOItemsIntegration;
import com.ssquadteam.fallout.listeners.PlayerListener;
import com.ssquadteam.fallout.listeners.ItemListener;
import com.ssquadteam.fallout.managers.RadiationManager;
import com.ssquadteam.fallout.managers.RadiationSourceManager;
import com.ssquadteam.fallout.storage.StorageManager;
import com.ssquadteam.fallout.tasks.RadiationTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class Fallout extends JavaPlugin {

    private static Fallout instance;
    private ConfigManager configManager;
    private StorageManager storageManager;
    private RadiationManager radiationManager;
    private RadiationSourceManager sourceManager;
    private MMOItemsIntegration mmoItemsIntegration;
    private boolean mmoItemsEnabled = false;

    @Override
    public void onEnable() {
        // Store instance for static access
        instance = this;
        
        // Initialize config manager
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Setup storage
        storageManager = new StorageManager(this);
        
        // Check for MMOItems integration
        checkForMMOItems();
        
        // Initialize managers
        radiationManager = new RadiationManager(this);
        sourceManager = new RadiationSourceManager(this);
        
        // Load saved radiation sources
        sourceManager.loadSources();
        
        // Register commands
        getCommand("rad").setExecutor(new RadCommandExecutor(this));
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        
        // Start radiation task
        int checkInterval = getConfigManager().getConfig().getInt("general.check-interval", 20);
        new RadiationTask(this).runTaskTimer(this, 20L, checkInterval);
        
        getLogger().info("Fallout plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all radiation sources
        if (sourceManager != null) {
            sourceManager.saveSources();
        }
        
        // Save all player radiation data
        if (radiationManager != null) {
            radiationManager.saveAllPlayerData();
        }
        
        getLogger().info("Fallout plugin has been disabled!");
    }
    
    /**
     * Check if MMOItems is present and initialize integration if available
     */
    private void checkForMMOItems() {
        Plugin mmoItems = Bukkit.getPluginManager().getPlugin("MMOItems");
        
        if (mmoItems != null && mmoItems.isEnabled()) {
            mmoItemsEnabled = true;
            getLogger().info("MMOItems found! Enabling integration...");
            mmoItemsIntegration = new MMOItemsIntegration(this);
            mmoItemsIntegration.registerAttributes();
        } else {
            mmoItemsEnabled = false;
            getLogger().info("MMOItems not found or disabled. Using built-in attribute system.");
        }
    }
    
    /**
     * Gets the static instance of the plugin
     * @return The plugin instance
     */
    public static Fallout getInstance() {
        return instance;
    }
    
    /**
     * Gets the config manager
     * @return The ConfigManager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Gets the storage manager
     * @return The StorageManager instance
     */
    public StorageManager getStorageManager() {
        return storageManager;
    }
    
    /**
     * Gets the radiation manager
     * @return The RadiationManager instance
     */
    public RadiationManager getRadiationManager() {
        return radiationManager;
    }
    
    /**
     * Gets the radiation source manager
     * @return The RadiationSourceManager instance
     */
    public RadiationSourceManager getSourceManager() {
        return sourceManager;
    }
    
    /**
     * Checks if MMOItems integration is enabled
     * @return true if MMOItems is available and integration is enabled
     */
    public boolean isMMOItemsEnabled() {
        return mmoItemsEnabled && configManager.getConfig().getBoolean("mmoitems.enabled", true);
    }
    
    /**
     * Gets the MMOItems integration
     * @return The MMOItemsIntegration instance or null if not enabled
     */
    public MMOItemsIntegration getMmoItemsIntegration() {
        return mmoItemsIntegration;
    }
    
    /**
     * Log a debug message if debug mode is enabled
     * @param message The message to log
     */
    public void debug(String message) {
        if (configManager.getConfig().getBoolean("general.debug", false)) {
            getLogger().log(Level.INFO, "[DEBUG] " + message);
        }
    }
} 