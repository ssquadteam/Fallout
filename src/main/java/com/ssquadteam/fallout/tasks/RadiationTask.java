package com.ssquadteam.fallout.tasks;

import com.ssquadteam.fallout.Fallout;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Task that periodically checks for players in radiation zones
 */
public class RadiationTask extends BukkitRunnable {

    private final Fallout plugin;
    private int damageCounter = 0;

    public RadiationTask(Fallout plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Check each online player
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Skip players with bypass permission
            if (player.hasPermission("fallout.bypass")) {
                continue;
            }
            
            // Skip players in disabled worlds
            if (!isWorldEnabled(player.getWorld().getName())) {
                continue;
            }
            
            // Get player's location
            Location location = player.getLocation();
            
            // Get radiation level at location
            int radiationLevel = plugin.getSourceManager().getRadiationLevelAt(location);
            
            if (radiationLevel > 0) {
                // Add radiation to player
                plugin.getRadiationManager().addRadiation(player, calculateRadiationGain(radiationLevel));
                
                // Show particles if enabled
                if (plugin.getConfigManager().getConfig().getBoolean("effects.particles.enabled", true)) {
                    showRadiationParticles(player);
                }
            } else {
                // Slowly decrease radiation when not in a radiation zone
                decreaseRadiation(player);
            }
        }
        
        // Apply damage on the configured interval
        damageCounter++;
        int damageInterval = plugin.getConfigManager().getConfig().getInt("damage.interval", 60);
        
        if (damageCounter >= damageInterval) {
            damageCounter = 0;
            applyRadiationDamage();
        }
    }
    
    /**
     * Calculate how much radiation a player should gain based on the radiation level
     * 
     * @param radiationLevel The radiation level at the player's location
     * @return The amount of radiation to add
     */
    private int calculateRadiationGain(int radiationLevel) {
        // Base gain is 1% of the radiation level per check
        return Math.max(1, radiationLevel / 100);
    }
    
    /**
     * Decrease a player's radiation level when not in a radiation zone
     * 
     * @param player The player
     */
    private void decreaseRadiation(Player player) {
        int currentLevel = plugin.getRadiationManager().getRadiationLevel(player);
        
        if (currentLevel > 0) {
            // Decrease by 1 point per check
            plugin.getRadiationManager().removeRadiation(player, 1);
        }
    }
    
    /**
     * Apply damage to players based on their radiation level
     */
    private void applyRadiationDamage() {
        if (!plugin.getConfigManager().getConfig().getBoolean("damage.enabled", true)) {
            return;
        }
        
        double baseDamage = plugin.getConfigManager().getConfig().getDouble("damage.base-amount", 2.0);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            int radiationLevel = plugin.getRadiationManager().getRadiationLevel(player);
            
            if (radiationLevel > 50) {
                // Calculate damage based on radiation level
                double damage = baseDamage * (radiationLevel / 100.0);
                
                // Apply damage
                player.damage(damage);
                
                // Play damage sound if enabled
                if (plugin.getConfigManager().getConfig().getBoolean("effects.sounds.damage.enabled", true)) {
                    String soundName = plugin.getConfigManager().getConfig().getString("effects.sounds.damage.sound", "ENTITY_PLAYER_HURT");
                    float volume = (float) plugin.getConfigManager().getConfig().getDouble("effects.sounds.damage.volume", 0.8);
                    float pitch = (float) plugin.getConfigManager().getConfig().getDouble("effects.sounds.damage.pitch", 1.2);
                    
                    try {
                        player.playSound(player.getLocation(), soundName, volume, pitch);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Invalid sound: " + soundName);
                    }
                }
            }
        }
    }
    
    /**
     * Show radiation particles around a player
     * 
     * @param player The player
     */
    private void showRadiationParticles(Player player) {
        try {
            String particleType = plugin.getConfigManager().getConfig().getString("effects.particles.type", "REDSTONE");
            int frequency = plugin.getConfigManager().getConfig().getInt("effects.particles.frequency", 10);
            
            // Only show a portion of particles each tick to achieve the desired frequency
            if (Math.random() * 20 > frequency) {
                return;
            }
            
            Location location = player.getLocation().add(0, 1, 0);
            
            // Spawn particles in a small radius around the player
            for (int i = 0; i < 3; i++) {
                double offsetX = (Math.random() - 0.5) * 2;
                double offsetY = Math.random();
                double offsetZ = (Math.random() - 0.5) * 2;
                
                location.getWorld().spawnParticle(
                    Particle.valueOf(particleType),
                    location.getX() + offsetX,
                    location.getY() + offsetY,
                    location.getZ() + offsetZ,
                    1, 0, 0, 0, 0
                );
            }
        } catch (Exception e) {
            // Silently fail if particle type is invalid
        }
    }
    
    /**
     * Check if radiation is enabled in a world
     * 
     * @param worldName The world name
     * @return true if radiation is enabled in the world
     */
    private boolean isWorldEnabled(String worldName) {
        List<String> enabledWorlds = plugin.getConfigManager().getConfig().getStringList("sources.enabled-worlds");
        return enabledWorlds.isEmpty() || enabledWorlds.contains(worldName);
    }
} 