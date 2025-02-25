package com.ssquadteam.fallout.storage;

import com.ssquadteam.fallout.Fallout;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Manages data storage for the plugin
 */
public class StorageManager {

    private final Fallout plugin;
    private StorageType storageType;
    private Connection mysqlConnection;

    public StorageManager(Fallout plugin) {
        this.plugin = plugin;
        
        // Determine storage type from config
        String storageTypeStr = plugin.getConfigManager().getConfig().getString("storage.type", "YAML");
        try {
            this.storageType = StorageType.valueOf(storageTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid storage type: " + storageTypeStr + ". Defaulting to YAML.");
            this.storageType = StorageType.YAML;
        }
        
        // Initialize storage
        if (storageType == StorageType.MYSQL) {
            initializeMysql();
        }
    }

    /**
     * Initialize MySQL connection
     */
    private void initializeMysql() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        String host = config.getString("storage.mysql.host", "localhost");
        int port = config.getInt("storage.mysql.port", 3306);
        String database = config.getString("storage.mysql.database", "fallout");
        String username = config.getString("storage.mysql.username", "root");
        String password = config.getString("storage.mysql.password", "password");
        String tablePrefix = config.getString("storage.mysql.table-prefix", "fallout_");
        
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        
        try {
            mysqlConnection = DriverManager.getConnection(url, username, password);
            
            // Create tables if they don't exist
            createTables(tablePrefix);
            
            plugin.getLogger().info("Connected to MySQL database!");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to MySQL database: " + e.getMessage(), e);
            plugin.getLogger().warning("Falling back to YAML storage.");
            storageType = StorageType.YAML;
        }
    }

    /**
     * Create necessary database tables
     * 
     * @param prefix Table prefix
     * @throws SQLException If an error occurs
     */
    private void createTables(String prefix) throws SQLException {
        // Create sources table
        String sourcesTable = "CREATE TABLE IF NOT EXISTS " + prefix + "sources ("
                + "id VARCHAR(36) PRIMARY KEY,"
                + "name VARCHAR(64) NOT NULL,"
                + "world VARCHAR(64) NOT NULL,"
                + "x DOUBLE NOT NULL,"
                + "y DOUBLE NOT NULL,"
                + "z DOUBLE NOT NULL,"
                + "radius INT NOT NULL,"
                + "strength INT NOT NULL,"
                + "power INT NOT NULL,"
                + "active BOOLEAN NOT NULL"
                + ")";
        
        // Create player data table
        String playerDataTable = "CREATE TABLE IF NOT EXISTS " + prefix + "player_data ("
                + "uuid VARCHAR(36) PRIMARY KEY,"
                + "radiation_level INT NOT NULL,"
                + "rad_x_expiry BIGINT"
                + ")";
        
        try (PreparedStatement sourcesStmt = mysqlConnection.prepareStatement(sourcesTable);
             PreparedStatement playerDataStmt = mysqlConnection.prepareStatement(playerDataTable)) {
            
            sourcesStmt.executeUpdate();
            playerDataStmt.executeUpdate();
        }
    }

    /**
     * Close the database connection
     */
    public void closeConnection() {
        if (mysqlConnection != null) {
            try {
                mysqlConnection.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error closing MySQL connection: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Get the current storage type
     * 
     * @return The storage type
     */
    public StorageType getStorageType() {
        return storageType;
    }

    /**
     * Get the MySQL connection
     * 
     * @return The connection or null if not using MySQL
     */
    public Connection getMysqlConnection() {
        return mysqlConnection;
    }

    /**
     * Storage types
     */
    public enum StorageType {
        YAML,
        MYSQL
    }
} 