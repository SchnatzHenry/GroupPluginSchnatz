package com.schnatz.groupplugin;

import com.schnatz.groupplugin.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.permission.ChildPermission;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.sql.SQLException;
@Plugin(name="GroupPluginSchnatz", version="1.0")
@Description("A plugin that manages users by using groups")
@ApiVersion(ApiVersion.Target.v1_19)
@Author("Henry Schnatz")
@Permission(name = "sign", desc = "Permission to summon an information sign", defaultValue = PermissionDefault.NOT_OP)
@Command(name = "sign", desc = "Allows to summon an information sign")
@Permission(name = "groupplugin.*", desc = "Wildcard groupplugin permission", defaultValue = PermissionDefault.OP)
@Permission(name = "groupplugin.*", desc = "Wildcard groupplugin permission", defaultValue = PermissionDefault.OP, children = {@ChildPermission(name = "groupplugin.creategroup"), @ChildPermission(name = "groupplugin.deletegroup"), @ChildPermission(name = "groupplugin.getgroups"), @ChildPermission(name = "groupplugin.editgroupname"), @ChildPermission(name = "groupplugin.editgroupprefix"), @ChildPermission(name = "groupplugin.editgrouplevel"), @ChildPermission(name = "groupplugin.editgroupcolorcode")})
@Permission(name = "groupplugin.creategroup", desc = "Permission to create groups", defaultValue = PermissionDefault.OP)
@Command(name = "creategroup", desc = "Allows to create groups")
@Permission(name = "groupplugin.deletegroup", desc = "Permission to create groups", defaultValue = PermissionDefault.OP)
@Command(name = "deletegroup", desc = "Allows to delete groups")
@Permission(name = "groupplugin.getgroups", desc = "Permission to get a users groups", defaultValue = PermissionDefault.OP)
@Command(name = "getgroups", desc = "Allows to get a list of a users groups")
@Permission(name = "groupplugin.editgroupname", desc = "Permission to edit group names", defaultValue =  PermissionDefault.OP)
@Command(name = "editgroupname", desc = "Allows to edit a group's name")
@Permission(name = "groupplugin.editgroupprefix", desc = "Permission to edit group prefixes", defaultValue =  PermissionDefault.OP)
@Command(name = "editgroupprefix", desc = "Allows to edit a group's prefix")
@Permission(name = "groupplugin.editgrouplevel", desc = "Permission to edit group levels", defaultValue =  PermissionDefault.OP)
@Command(name = "editgrouplevel", desc = "Allows to edit a group's level")
@Permission(name = "groupplugin.editgroupcolorcode", desc = "Permission to edit group color codes", defaultValue =  PermissionDefault.OP)
@Command(name = "editgroupcolorcode", desc = "Allows to edit a group's color code")
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

        CommandSign commandSign = new CommandSign(databaseManager, config);
        this.getCommand("sign").setExecutor(commandSign);
        this.getCommand("sign").setTabCompleter(commandSign);

        CommandCreateGroup commandCreateGroup = new CommandCreateGroup(databaseManager, config);
        this.getCommand("creategroup").setExecutor(commandCreateGroup);
        this.getCommand("creategroup").setTabCompleter(commandCreateGroup);
        CommandDeleteGroup commandDeleteGroup = new CommandDeleteGroup(databaseManager, config);
        this.getCommand("deletegroup").setExecutor(commandDeleteGroup);
        this.getCommand("deletegroup").setTabCompleter(commandDeleteGroup);
        CommandGetGroups commandGetGroups = new CommandGetGroups(databaseManager, config);
        this.getCommand("getgroups").setExecutor(commandGetGroups);
        this.getCommand("getgroups").setTabCompleter(commandGetGroups);
        CommandEditGroupName commandEditGroupName = new CommandEditGroupName(databaseManager, config);
        this.getCommand("editgroupname").setExecutor(commandEditGroupName);
        this.getCommand("editgroupname").setTabCompleter(commandEditGroupName);
        CommandEditGroupPrefix commandEditGroupPrefix = new CommandEditGroupPrefix(databaseManager, config);
        this.getCommand("editgroupprefix").setExecutor(commandEditGroupPrefix);
        this.getCommand("editgroupprefix").setTabCompleter(commandEditGroupPrefix);
        CommandEditGroupLevel commandEditGroupLevel = new CommandEditGroupLevel(databaseManager,config);
        this.getCommand("editgrouplevel").setExecutor(commandEditGroupLevel);
        this.getCommand("editgrouplevel").setTabCompleter(commandEditGroupLevel);
        CommandEditGroupColorCode commandEditGroupColorCode = new CommandEditGroupColorCode(databaseManager, config);
        this.getCommand("editgroupcolorcode").setExecutor(commandEditGroupColorCode);
        this.getCommand("editgroupcolorcode").setTabCompleter(commandEditGroupColorCode);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    /**
     * builds the plugin's config
     */
    public void buildConfig() {
        new ConfigBuilder(this);
    }
}