package com.ssquadteam.fallout.managers;

import com.ssquadteam.fallout.Fallout;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player radiation levels and effects
 */
public class RadiationManager {

    private final Fallout plugin;
    private final Map<UUID, Integer> playerRadiationLevels = new HashMap<>();
    private final Map<UUID, BossBar> playerBossBars = new HashMap<>();
    private final Map<UUID, Long> radXEffects = new HashMap<>();

    public RadiationManager(Fallout plugin) {
        this.plugin = plugin;
        loadPlayerData();
    }

    /**
     * Get a player's current radiation level
     * 
     * @param player The player to check
     * @return The player's radiation level (0-100)
     */
    public int getRadiationLevel(Player player) {
        return playerRadiationLevels.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Set a player's radiation level
     * 
     * @param player The player
     * @param level The new radiation level (0-100)
     */
    public void setRadiationLevel(Player player, int level) {
        int maxLevel = plugin.getConfigManager().getConfig().getInt("general.max-radiation-level", 100);
        int newLevel = Math.min(maxLevel, Math.max(0, level));
        
        // Store the value
        playerRadiationLevels.put(player.getUniqueId(), newLevel);
        
        // Update the boss bar
        updateBossBar(player, newLevel);
        
        // Apply effects based on radiation level
        applyRadiationEffects(player, newLevel);
    }

    /**
     * Add radiation to a player
     * 
     * @param player The player
     * @param amount The amount of radiation to add
     */
    public void addRadiation(Player player, int amount) {
        if (player.hasPermission("fallout.bypass")) {
            return;
        }
        
        // Apply radiation resistance if player has any
        double resistanceMultiplier = calculateResistanceMultiplier(player);
        int adjustedAmount = (int) (amount * (1.0 - resistanceMultiplier));
        
        if (adjustedAmount <= 0) {
            return;
        }
        
        int currentLevel = getRadiationLevel(player);
        setRadiationLevel(player, currentLevel + adjustedAmount);
    }

    /**
     * Remove radiation from a player
     * 
     * @param player The player
     * @param amount The amount of radiation to remove
     */
    public void removeRadiation(Player player, int amount) {
        int currentLevel = getRadiationLevel(player);
        setRadiationLevel(player, currentLevel - amount);
    }

    /**
     * Calculate a player's radiation resistance multiplier
     * 
     * @param player The player
     * @return The resistance multiplier (0.0-1.0)
     */
    public double calculateResistanceMultiplier(Player player) {
        double resistance = 0.0;
        
        // Check if player has Rad-X effect
        if (hasRadXEffect(player)) {
            resistance += getRadXResistance();
        }
        
        // Add armor resistance
        if (plugin.getConfigManager().getConfig().getBoolean("equipment.enabled", true)) {
            resistance += calculateArmorResistance(player);
        }
        
        // Add MMOItems resistance if available
        if (plugin.isMMOItemsEnabled()) {
            resistance += calculateMMOItemsResistance(player);
        }
        
        // Cap at 95% resistance (never completely immune)
        return Math.min(0.95, resistance);
    }

    /**
     * Load player radiation data from storage
     */
    private void loadPlayerData() {
        FileConfiguration config = plugin.getConfigManager().getPlayerDataConfig();
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        
        if (playersSection == null) {
            plugin.debug("No player radiation data found");
            return;
        }
        
        for (String uuidString : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                int radiationLevel = playersSection.getInt(uuidString + ".radiation-level", 0);
                
                if (radiationLevel > 0) {
                    playerRadiationLevels.put(uuid, radiationLevel);
                    plugin.debug("Loaded radiation level " + radiationLevel + " for player " + uuidString);
                }
                
                // Load Rad-X effect if it exists and hasn't expired
                if (playersSection.contains(uuidString + ".rad-x-expiry")) {
                    long expiryTime = playersSection.getLong(uuidString + ".rad-x-expiry", 0);
                    if (expiryTime > System.currentTimeMillis()) {
                        radXEffects.put(uuid, expiryTime);
                        plugin.debug("Loaded Rad-X effect for player " + uuidString + " expiring at " + expiryTime);
                    }
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in player data: " + uuidString);
            }
        }
        
        plugin.getLogger().info("Loaded radiation data for " + playerRadiationLevels.size() + " players");
    }

    /**
     * Save all player radiation data to storage
     */
    public void saveAllPlayerData() {
        FileConfiguration config = plugin.getConfigManager().getPlayerDataConfig();
        config.set("players", null); // Clear existing data
        
        ConfigurationSection playersSection = config.createSection("players");
        
        for (Map.Entry<UUID, Integer> entry : playerRadiationLevels.entrySet()) {
            String uuidString = entry.getKey().toString();
            int radiationLevel = entry.getValue();
            
            if (radiationLevel > 0) {
                playersSection.set(uuidString + ".radiation-level", radiationLevel);
            }
            
            // Save Rad-X effect if it exists and hasn't expired
            if (radXEffects.containsKey(entry.getKey())) {
                long expiryTime = radXEffects.get(entry.getKey());
                if (expiryTime > System.currentTimeMillis()) {
                    playersSection.set(uuidString + ".rad-x-expiry", expiryTime);
                }
            }
        }
        
        plugin.getConfigManager().savePlayerDataConfig();
        plugin.getLogger().info("Saved radiation data for " + playerRadiationLevels.size() + " players");
    }

    /**
     * Update the boss bar for a player
     * 
     * @param player The player
     * @param radiationLevel The current radiation level
     */
    private void updateBossBar(Player player, int radiationLevel) {
        if (!plugin.getConfigManager().getConfig().getBoolean("effects.boss-bar.enabled", true)) {
            return;
        }
        
        BossBar bossBar = playerBossBars.get(player.getUniqueId());
        
        // Create boss bar if it doesn't exist
        if (bossBar == null && radiationLevel > 0) {
            String title = plugin.getConfigManager().getConfig().getString("effects.boss-bar.title", "&c☢ Radiation Level: {level}% ☢");
            title = title.replace("{level}", String.valueOf(radiationLevel)).replace("&", "§");
            
            BarColor color = BarColor.RED;
            try {
                color = BarColor.valueOf(plugin.getConfigManager().getConfig().getString("effects.boss-bar.color", "RED"));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid boss bar color in config: " + e.getMessage());
            }
            
            BarStyle style = BarStyle.SOLID;
            try {
                style = BarStyle.valueOf(plugin.getConfigManager().getConfig().getString("effects.boss-bar.style", "SOLID"));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid boss bar style in config: " + e.getMessage());
            }
            
            bossBar = Bukkit.createBossBar(title, color, style);
            bossBar.addPlayer(player);
            playerBossBars.put(player.getUniqueId(), bossBar);
        }
        
        // Update existing boss bar
        if (bossBar != null) {
            if (radiationLevel <= 0) {
                // Remove boss bar if radiation is gone
                bossBar.removePlayer(player);
                playerBossBars.remove(player.getUniqueId());
                bossBar.setVisible(false);
            } else {
                // Update boss bar
                String title = plugin.getConfigManager().getConfig().getString("effects.boss-bar.title", "&c☢ Radiation Level: {level}% ☢");
                title = title.replace("{level}", String.valueOf(radiationLevel)).replace("&", "§");
                
                bossBar.setTitle(title);
                bossBar.setProgress(radiationLevel / 100.0);
                bossBar.setVisible(true);
            }
        }
    }

    /**
     * Apply radiation effects to a player based on their radiation level
     * 
     * @param player The player
     * @param radiationLevel The current radiation level
     */
    private void applyRadiationEffects(Player player, int radiationLevel) {
        if (!plugin.getConfigManager().getConfig().getBoolean("damage.enabled", true)) {
            return;
        }
        
        // Apply potion effects based on radiation level thresholds
        ConfigurationSection effectsSection = plugin.getConfigManager().getConfig().getConfigurationSection("damage.effects");
        if (effectsSection != null) {
            for (String thresholdStr : effectsSection.getKeys(false)) {
                try {
                    int threshold = Integer.parseInt(thresholdStr);
                    
                    if (radiationLevel >= threshold) {
                        for (String effectStr : effectsSection.getStringList(thresholdStr)) {
                            applyPotionEffect(player, effectStr);
                        }
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid radiation effect threshold: " + thresholdStr);
                }
            }
        }
    }
    
    /**
     * Apply a potion effect from a string format
     * 
     * @param player The player
     * @param effectStr The effect string (format: TYPE:AMPLIFIER:DURATION)
     */
    private void applyPotionEffect(Player player, String effectStr) {
        String[] parts = effectStr.split(":");
        if (parts.length < 3) {
            plugin.getLogger().warning("Invalid effect format: " + effectStr);
            return;
        }
        
        try {
            PotionEffectType type = PotionEffectType.getByName(parts[0]);
            if (type == null) {
                plugin.getLogger().warning("Invalid potion effect type: " + parts[0]);
                return;
            }
            
            int amplifier = Integer.parseInt(parts[1]);
            int duration = Integer.parseInt(parts[2]) * 20; // Convert seconds to ticks
            
            player.addPotionEffect(new PotionEffect(type, duration, amplifier));
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid number in effect: " + effectStr);
        }
    }
    
    /**
     * Check if a player has an active Rad-X effect
     * 
     * @param player The player
     * @return true if the player has an active Rad-X effect
     */
    public boolean hasRadXEffect(Player player) {
        Long expiryTime = radXEffects.get(player.getUniqueId());
        if (expiryTime == null) {
            return false;
        }
        
        if (expiryTime <= System.currentTimeMillis()) {
            // Effect has expired, remove it
            radXEffects.remove(player.getUniqueId());
            return false;
        }
        
        return true;
    }
    
    /**
     * Apply a Rad-X effect to a player
     * 
     * @param player The player
     * @param durationSeconds The duration in seconds
     */
    public void applyRadXEffect(Player player, int durationSeconds) {
        long expiryTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        radXEffects.put(player.getUniqueId(), expiryTime);
        
        player.sendMessage("§bYou feel more resistant to radiation. (+" + getRadXResistance() * 100 + "% resistance)");
    }
    
    /**
     * Get the resistance provided by Rad-X
     * 
     * @return The resistance value (0.0-1.0)
     */
    public double getRadXResistance() {
        return plugin.getConfigManager().getConfig().getDouble("healing.items.radx.resistance-amount", 50) / 100.0;
    }
    
    /**
     * Calculate armor radiation resistance for a player
     * 
     * @param player The player
     * @return The resistance value (0.0-1.0)
     */
    public double calculateArmorResistance(Player player) {
        double resistance = 0.0;
        ConfigurationSection armorSection = plugin.getConfigManager().getConfig().getConfigurationSection("equipment.vanilla");
        
        if (armorSection == null) {
            return 0.0;
        }
        
        // Check each armor piece
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack item : armor) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            
            String materialName = item.getType().name();
            for (String key : armorSection.getKeys(false)) {
                if (materialName.contains(key)) {
                    resistance += armorSection.getDouble(key) / 100.0;
                    break;
                }
            }
        }
        
        return resistance;
    }
    
    /**
     * Calculate MMOItems radiation resistance for a player
     * 
     * @param player The player
     * @return The resistance value (0.0-1.0)
     */
    public double calculateMMOItemsResistance(Player player) {
        if (!plugin.isMMOItemsEnabled()) {
            return 0.0;
        }
        
        try {
            return plugin.getMmoItemsIntegration().getRadiationResistance(player);
        } catch (Exception e) {
            plugin.getLogger().warning("Error calculating MMOItems radiation resistance: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Clean up resources when a player quits
     * 
     * @param player The player
     */
    public void cleanupPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Remove boss bar
        BossBar bossBar = playerBossBars.remove(uuid);
        if (bossBar != null) {
            bossBar.removePlayer(player);
            bossBar.setVisible(false);
        }
    }
}
