package com.schnatz.groupplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class is used to build the default config
 * @author Henry Schnatz
 */

public class ConfigBuilder {
    /**
     * The plugin which the default config belongs to
     */
    private final JavaPlugin plugin;
    /**
     * The config that is edited.
     */
    private final FileConfiguration config;

    /**
     * Initializes variable {@link ConfigBuilder#plugin} with the given argument.
     * Initializes variable {@link ConfigBuilder#config} with the plugin's config.
     * And calls the method {@link ConfigBuilder#buildDefaultConfig}.
     *
     * @param plugin the plugin the default config will belong to
     */
    public ConfigBuilder(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        buildDefaultConfig();
    }

    /**
     * Builds and saves the default config.
     */
    public void buildDefaultConfig() {
        config.addDefault("DatabaseIpAddress", "localhost");
        config.addDefault("DatabasePort", 3306);
        config.addDefault("DatabaseUser", "root");
        config.addDefault("DatabasePassword", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4");
        config.addDefault("DatabaseName", "ServerGroups");

        config.addDefault("DefaultGroupName", "Player");
        config.addDefault("DefaultGroupPrefix", "P");
        config.addDefault("DefaultGroupLevel", 0);
        config.addDefault("DefaultGroupColorCode", 0);

        config.addDefault("MessageServerJoin", "\u00A77[\u00A7%color%%prefix%\u00A77]\u00A7%color%%name% \u00A7fjoined the Server");
        config.addDefault("MessageServerLeave", "\u00A77[\u00A7%color%%prefix%\u00A77]\u00A7%color%%name% \u00A7fleft the Server");
        config.addDefault("MessageChat", "\u00A77[\u00A7%color%%prefix%\u00A77]\u00A7%color%%name%\u00A7f: %message%");

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }
}