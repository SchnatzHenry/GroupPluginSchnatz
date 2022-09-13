import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.sql.SQLException;

@Plugin(name="SchnatzBewerbungsplugin", version="1.0")
@Description("A plugin that manages users by using groups")
@Author("Henry Schnatz")

public class Main extends JavaPlugin {
    private DatabaseManager databaseManager;

    private final FileConfiguration config = this.getConfig();
    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        buildConfig();

        try {
            if (!(config.isString("DatabaseIpAddress")
                    && config.isInt("DatabasePort")
                    && config.isString("DatabaseUser")
                    && config.isString("DatabasePassword")
                    && config.isString("DatabaseName")
                    && config.isString("DefaultGroupName")
                    && config.isString("DefaultGroupPrefix")
                    && config.isInt("DefaultGroupLevel")
                    && config.isInt("DefaultGroupColorCode")))
                throw new IllegalStateException();
            databaseManager = new DatabaseManager(config.getString("DatabaseIpAddress"), config.getInt("DatabasePort"), config.getString("DatabaseUser"), config.getString("DatabasePassword"), config.getString("DatabaseName"), config.getString("DefaultGroupName"), config.getString("DefaultGroupPrefix"), config.getInt("DefaultGroupLevel"), config.getInt("DefaultGroupColorCode"));
        } catch (SQLException e) {
            this.getLogger().severe(e.getMessage());
            System.exit(1);
        }


        if(!(config.isString("MessageServerJoin")
                && config.isString("MessageServerLeave")
                && config.isString("MessageChat")
                && config.isString("DefaultGroupName")))
            throw new IllegalStateException();
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new GroupListener(config.getString("MessageServerJoin"),  config.getString("MessageServerLeave"), config.getString("MessageChat"), config.getString("DefaultGroupName") ,databaseManager), this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public void buildConfig() {
        new ConfigBuilder(this);
    }
}