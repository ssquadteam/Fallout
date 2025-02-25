package com.ssquadteam.fallout.commands;

import com.ssquadteam.fallout.Fallout;
import com.ssquadteam.fallout.models.RadiationSource;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Handles the /rad command
 */
public class RadCommandExecutor implements CommandExecutor {

    private final Fallout plugin;

    public RadCommandExecutor(Fallout plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "new":
                return handleNewCommand(sender, args);
            case "list":
                return handleListCommand(sender);
            case "remove":
                return handleRemoveCommand(sender, args);
            case "info":
                return handleInfoCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    /**
     * Handle the /rad new command
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return true if the command was handled
     */
    private boolean handleNewCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("fallout.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: /rad new <name> <radius> <strength> [power]");
            return true;
        }

        String name = args[1];
        int radius;
        int strength;
        int power;

        try {
            radius = Integer.parseInt(args[2]);
            strength = Integer.parseInt(args[3]);
            power = args.length > 4 ? Integer.parseInt(args[4]) : 5; // Default power is 5
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number format. Please use integers for radius, strength, and power.");
            return true;
        }

        // Check if a source with this name already exists
        if (plugin.getSourceManager().getSourceByName(name) != null) {
            player.sendMessage(ChatColor.RED + "A radiation source with the name '" + name + "' already exists.");
            return true;
        }

        // Create the radiation source
        Location location = player.getLocation();
        RadiationSource source = plugin.getSourceManager().createSource(name, location, radius, strength, power);

        player.sendMessage(ChatColor.GREEN + "Created radiation source '" + name + "' at your location.");
        player.sendMessage(ChatColor.GRAY + "Radius: " + radius + ", Strength: " + strength + ", Power: " + power);

        return true;
    }

    /**
     * Handle the /rad list command
     * 
     * @param sender The command sender
     * @return true if the command was handled
     */
    private boolean handleListCommand(CommandSender sender) {
        if (!sender.hasPermission("fallout.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        Collection<RadiationSource> sources = plugin.getSourceManager().getAllSources();

        if (sources.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No radiation sources found.");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "Radiation Sources (" + sources.size() + "):");
        
        for (RadiationSource source : sources) {
            Location loc = source.getLocation();
            String status = source.isActive() ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive";
            
            sender.sendMessage(ChatColor.GOLD + source.getName() + ChatColor.GRAY + " - " + status);
            sender.sendMessage(ChatColor.GRAY + "  Location: " + loc.getWorld().getName() + " " + 
                    Math.round(loc.getX()) + ", " + Math.round(loc.getY()) + ", " + Math.round(loc.getZ()));
            sender.sendMessage(ChatColor.GRAY + "  Radius: " + source.getRadius() + ", Strength: " + 
                    source.getStrength() + ", Power: " + source.getPower());
        }

        return true;
    }

    /**
     * Handle the /rad remove command
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return true if the command was handled
     */
    private boolean handleRemoveCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fallout.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /rad remove <name>");
            return true;
        }

        String name = args[1];
        boolean removed = plugin.getSourceManager().removeSourceByName(name);

        if (removed) {
            sender.sendMessage(ChatColor.GREEN + "Removed radiation source '" + name + "'.");
        } else {
            sender.sendMessage(ChatColor.RED + "No radiation source found with the name '" + name + "'.");
        }

        return true;
    }

    /**
     * Handle the /rad info command
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return true if the command was handled
     */
    private boolean handleInfoCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fallout.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage: /rad info <name>");
                return true;
            }
            
            // If no name provided and sender is a player, show info about radiation at current location
            Player player = (Player) sender;
            Location location = player.getLocation();
            int radiationLevel = plugin.getSourceManager().getRadiationLevelAt(location);
            
            sender.sendMessage(ChatColor.GREEN + "Radiation Level: " + radiationLevel + "%");
            
            if (radiationLevel > 0) {
                sender.sendMessage(ChatColor.GRAY + "You are in a radiation zone!");
                
                // Show nearby sources
                sender.sendMessage(ChatColor.GRAY + "Nearby radiation sources:");
                for (RadiationSource source : plugin.getSourceManager().getSourcesNearLocation(location)) {
                    double distance = location.distance(source.getLocation());
                    sender.sendMessage(ChatColor.GOLD + source.getName() + ChatColor.GRAY + " - " + 
                            Math.round(distance) + " blocks away");
                }
            }
            
            return true;
        }

        String name = args[1];
        RadiationSource source = plugin.getSourceManager().getSourceByName(name);

        if (source == null) {
            sender.sendMessage(ChatColor.RED + "No radiation source found with the name '" + name + "'.");
            return true;
        }

        Location loc = source.getLocation();
        String status = source.isActive() ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive";
        
        sender.sendMessage(ChatColor.GREEN + "Radiation Source: " + ChatColor.GOLD + source.getName());
        sender.sendMessage(ChatColor.GRAY + "Status: " + status);
        sender.sendMessage(ChatColor.GRAY + "Location: " + loc.getWorld().getName() + " " + 
                Math.round(loc.getX()) + ", " + Math.round(loc.getY()) + ", " + Math.round(loc.getZ()));
        sender.sendMessage(ChatColor.GRAY + "Radius: " + source.getRadius() + " blocks");
        sender.sendMessage(ChatColor.GRAY + "Strength: " + source.getStrength() + "%");
        sender.sendMessage(ChatColor.GRAY + "Power: " + source.getPower());

        return true;
    }

    /**
     * Handle the /rad reload command
     * 
     * @param sender The command sender
     * @return true if the command was handled
     */
    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("fallout.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        // Save current data
        plugin.getSourceManager().saveSources();
        plugin.getRadiationManager().saveAllPlayerData();
        
        // Reload config
        plugin.getConfigManager().reloadAll();
        
        // Reload sources
        plugin.getSourceManager().loadSources();
        
        sender.sendMessage(ChatColor.GREEN + "Fallout plugin reloaded!");
        return true;
    }

    /**
     * Send help message to the sender
     * 
     * @param sender The command sender
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== Fallout Plugin Commands ===");
        sender.sendMessage(ChatColor.GOLD + "/rad new <name> <radius> <strength> [power]" + ChatColor.GRAY + " - Create a new radiation source");
        sender.sendMessage(ChatColor.GOLD + "/rad list" + ChatColor.GRAY + " - List all radiation sources");
        sender.sendMessage(ChatColor.GOLD + "/rad remove <name>" + ChatColor.GRAY + " - Remove a radiation source");
        sender.sendMessage(ChatColor.GOLD + "/rad info [name]" + ChatColor.GRAY + " - Show info about a radiation source or your current location");
        sender.sendMessage(ChatColor.GOLD + "/rad reload" + ChatColor.GRAY + " - Reload the plugin configuration");
    }
} 