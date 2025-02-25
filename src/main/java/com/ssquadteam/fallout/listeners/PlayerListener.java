package com.ssquadteam.fallout.listeners;

import com.ssquadteam.fallout.Fallout;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player-related events
 */
public class PlayerListener implements Listener {

    private final Fallout plugin;

    public PlayerListener(Fallout plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle player join event
     * 
     * @param event The event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Update boss bar if player has radiation
        int radiationLevel = plugin.getRadiationManager().getRadiationLevel(player);
        if (radiationLevel > 0) {
            plugin.getRadiationManager().setRadiationLevel(player, radiationLevel);
        }
    }

    /**
     * Handle player quit event
     * 
     * @param event The event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Clean up resources
        plugin.getRadiationManager().cleanupPlayer(player);
    }
} 