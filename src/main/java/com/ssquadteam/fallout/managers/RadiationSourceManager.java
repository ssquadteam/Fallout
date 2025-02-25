package com.ssquadteam.fallout.managers;

import com.ssquadteam.fallout.Fallout;
import com.ssquadteam.fallout.models.RadiationSource;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages all radiation sources in the game
 */
public class RadiationSourceManager {

    private final Fallout plugin;
    private final Map<UUID, RadiationSource> sources = new HashMap<>();

    public RadiationSourceManager(Fallout plugin) {
        this.plugin = plugin;
    }

    /**
     * Create a new radiation source
     * 
     * @param name The name of the source
     * @param location The location of the source
     * @param radius The radius of effect in blocks
     * @param strength The strength of radiation at center (0-100)
     * @param power How quickly radiation builds up (1-10)
     * @return The created RadiationSource
     */
    public RadiationSource createSource(String name, Location location, int radius, int strength, int power) {
        RadiationSource source = new RadiationSource(name, location, radius, strength, power);
        sources.put(source.getId(), source);
        plugin.debug("Created radiation source: " + source);
        return source;
    }

    /**
     * Get a radiation source by its ID
     * 
     * @param id The source ID
     * @return The RadiationSource or null if not found
     */
    public RadiationSource getSource(UUID id) {
        return sources.get(id);
    }

    /**
     * Get a radiation source by its name
     * 
     * @param name The source name
     * @return The RadiationSource or null if not found
     */
    public RadiationSource getSourceByName(String name) {
        return sources.values().stream()
                .filter(source -> source.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all radiation sources
     * 
     * @return Collection of all radiation sources
     */
    public Collection<RadiationSource> getAllSources() {
        return Collections.unmodifiableCollection(sources.values());
    }

    /**
     * Get active radiation sources
     * 
     * @return Collection of active radiation sources
     */
    public Collection<RadiationSource> getActiveSources() {
        return sources.values().stream()
                .filter(RadiationSource::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Get radiation sources near a location
     * 
     * @param location The location to check
     * @return List of radiation sources that affect the location
     */
    public List<RadiationSource> getSourcesNearLocation(Location location) {
        return sources.values().stream()
                .filter(RadiationSource::isActive)
                .filter(source -> source.getLocation().getWorld().equals(location.getWorld()))
                .filter(source -> source.getLocation().distance(location) <= source.getRadius())
                .collect(Collectors.toList());
    }

    /**
     * Calculate the total radiation level at a location
     * 
     * @param location The location to check
     * @return The total radiation level (0-100)
     */
    public int getRadiationLevelAt(Location location) {
        List<RadiationSource> nearbySources = getSourcesNearLocation(location);
        
        if (nearbySources.isEmpty()) {
            return 0;
        }
        
        // Use the highest radiation level from all nearby sources
        // Could be changed to additive with diminishing returns if preferred
        return nearbySources.stream()
                .mapToInt(source -> source.getRadiationLevelAt(location))
                .max()
                .orElse(0);
    }

    /**
     * Remove a radiation source
     * 
     * @param id The ID of the source to remove
     * @return true if removed, false if not found
     */
    public boolean removeSource(UUID id) {
        RadiationSource removed = sources.remove(id);
        if (removed != null) {
            plugin.debug("Removed radiation source: " + removed);
            return true;
        }
        return false;
    }

    /**
     * Remove a radiation source by name
     * 
     * @param name The name of the source to remove
     * @return true if removed, false if not found
     */
    public boolean removeSourceByName(String name) {
        RadiationSource source = getSourceByName(name);
        if (source != null) {
            return removeSource(source.getId());
        }
        return false;
    }

    /**
     * Load all radiation sources from storage
     */
    public void loadSources() {
        sources.clear();
        
        FileConfiguration config = plugin.getConfigManager().getSourcesConfig();
        ConfigurationSection sourcesSection = config.getConfigurationSection("sources");
        
        if (sourcesSection == null) {
            plugin.debug("No radiation sources found in sources.yml");
            return;
        }
        
        for (String key : sourcesSection.getKeys(false)) {
            try {
                ConfigurationSection sourceSection = sourcesSection.getConfigurationSection(key);
                if (sourceSection == null) continue;
                
                Map<String, Object> sourceMap = new HashMap<>();
                for (String field : sourceSection.getKeys(false)) {
                    sourceMap.put(field, sourceSection.get(field));
                }
                
                RadiationSource source = new RadiationSource(sourceMap);
                sources.put(source.getId(), source);
                plugin.debug("Loaded radiation source: " + source);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load radiation source at key " + key + ": " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Loaded " + sources.size() + " radiation sources");
    }

    /**
     * Save all radiation sources to storage
     */
    public void saveSources() {
        FileConfiguration config = plugin.getConfigManager().getSourcesConfig();
        config.set("sources", null); // Clear existing sources
        
        ConfigurationSection sourcesSection = config.createSection("sources");
        
        int count = 0;
        for (RadiationSource source : sources.values()) {
            try {
                ConfigurationSection sourceSection = sourcesSection.createSection(source.getId().toString());
                Map<String, Object> serialized = source.serialize();
                
                for (Map.Entry<String, Object> entry : serialized.entrySet()) {
                    sourceSection.set(entry.getKey(), entry.getValue());
                }
                
                count++;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save radiation source " + source.getId() + ": " + e.getMessage());
            }
        }
        
        plugin.getConfigManager().saveSourcesConfig();
        plugin.getLogger().info("Saved " + count + " radiation sources");
    }
} 