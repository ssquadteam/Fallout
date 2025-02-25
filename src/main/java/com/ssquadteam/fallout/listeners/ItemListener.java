package com.ssquadteam.fallout.listeners;

import com.ssquadteam.fallout.Fallout;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Handles item-related events
 */
public class ItemListener implements Listener {

    private final Fallout plugin;

    public ItemListener(Fallout plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle item consumption event
     * 
     * @param event The event
     */
    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Check if the item is a radiation healing item
        if (isRadiationHealingItem(item)) {
            handleRadiationHealingItem(player, item);
        }
    }
    
    /**
     * Check if an item is a radiation healing item
     * 
     * @param item The item to check
     * @return true if the item is a radiation healing item
     */
    private boolean isRadiationHealingItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        // Check vanilla items from config
        ConfigurationSection healingItems = plugin.getConfigManager().getConfig().getConfigurationSection("healing.items");
        if (healingItems == null) {
            return false;
        }
        
        for (String key : healingItems.getKeys(false)) {
            String materialName = healingItems.getString(key + ".material");
            if (materialName == null) continue;
            
            Material material = Material.getMaterial(materialName);
            if (material != null && material == item.getType()) {
                // Check custom model data if specified
                if (healingItems.contains(key + ".custom-model-data")) {
                    int customModelData = healingItems.getInt(key + ".custom-model-data");
                    ItemMeta meta = item.getItemMeta();
                    
                    if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData) {
                        return true;
                    }
                } else {
                    // If no custom model data specified, just match the material
                    return true;
                }
            }
        }
        
        // Check MMOItems if enabled
        if (plugin.isMMOItemsEnabled()) {
            try {
                // This would normally use MMOItems API to check the item
                // For now, we'll just return false
                return false;
                
                // In a real implementation, you would use code like this:
                // net.Indyuce.mmoitems.api.Type type = net.Indyuce.mmoitems.api.Type.get(item);
                // String id = net.Indyuce.mmoitems.api.ItemStats.getID(item);
                // return plugin.getMmoItemsIntegration().isRadiationHealingItem(type.getId(), id);
            } catch (Exception e) {
                plugin.getLogger().warning("Error checking MMOItems item: " + e.getMessage());
            }
        }
        
        return false;
    }
    
    /**
     * Handle consumption of a radiation healing item
     * 
     * @param player The player
     * @param item The item
     */
    private void handleRadiationHealingItem(Player player, ItemStack item) {
        // Check vanilla items from config
        ConfigurationSection healingItems = plugin.getConfigManager().getConfig().getConfigurationSection("healing.items");
        if (healingItems == null) {
            return;
        }
        
        for (String key : healingItems.getKeys(false)) {
            String materialName = healingItems.getString(key + ".material");
            if (materialName == null) continue;
            
            Material material = Material.getMaterial(materialName);
            if (material != null && material == item.getType()) {
                // Check custom model data if specified
                if (healingItems.contains(key + ".custom-model-data")) {
                    int customModelData = healingItems.getInt(key + ".custom-model-data");
                    ItemMeta meta = item.getItemMeta();
                    
                    if (meta == null || !meta.hasCustomModelData() || meta.getCustomModelData() != customModelData) {
                        continue;
                    }
                }
                
                // Apply effects based on item type
                if (key.equalsIgnoreCase("rad-away")) {
                    // Rad-Away removes radiation
                    int amount = healingItems.getInt(key + ".amount", 15);
                    plugin.getRadiationManager().removeRadiation(player, amount);
                    player.sendMessage("§eYou feel the radiation leaving your body. (-" + amount + "%)");
                } else if (key.equalsIgnoreCase("radx")) {
                    // Rad-X provides temporary resistance
                    int duration = healingItems.getInt(key + ".duration", 600);
                    plugin.getRadiationManager().applyRadXEffect(player, duration);
                }
                
                return;
            }
        }
        
        // Handle MMOItems if enabled
        if (plugin.isMMOItemsEnabled()) {
            try {
                // This would normally use MMOItems API to handle the item
                // For now, we'll just do nothing
                
                // In a real implementation, you would use code like this:
                // net.Indyuce.mmoitems.api.Type type = net.Indyuce.mmoitems.api.Type.get(item);
                // String id = net.Indyuce.mmoitems.api.ItemStats.getID(item);
                // if (type.getId().equalsIgnoreCase("CONSUMABLE") && id.equalsIgnoreCase("rad_away")) {
                //     plugin.getRadiationManager().removeRadiation(player, 15);
                // } else if (type.getId().equalsIgnoreCase("CONSUMABLE") && id.equalsIgnoreCase("rad_x")) {
                //     plugin.getRadiationManager().applyRadXEffect(player, 600);
                // }
            } catch (Exception e) {
                plugin.getLogger().warning("Error handling MMOItems item: " + e.getMessage());
            }
        }
    }
} 