package com.schnatz.groupplugin.commands;

import com.schnatz.groupplugin.DatabaseManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * This class features a {@link DatabaseManager} object allowing subclasses to use a given database
 * @author Henry Schnatz
 */
public abstract class DatabaseCommand implements CommandExecutor, TabCompleter {
    /**
     * The database manager object
     */
    protected final DatabaseManager databaseManager;
    /**
     * The plugins config
     */
    protected final FileConfiguration config;

    /**
     * Initialising the missing constants with the given values
     * @param databaseManager the database manager
     * @param config the config
     */
    public DatabaseCommand(DatabaseManager databaseManager, FileConfiguration config) {
        this.databaseManager = databaseManager;
        this.config = config;
    }
}