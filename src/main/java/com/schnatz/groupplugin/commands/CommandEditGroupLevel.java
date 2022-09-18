package com.schnatz.groupplugin.commands;

import com.schnatz.groupplugin.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class CommandEditGroupLevel extends DatabaseCommand {
    /**
     * The message the sender gets displayed when he has insufficient permission to perform the command
     */
    private final String insufficientPermissionMessage;
    /**
     * The message that explains the command's usage
     */
    private final String usageMessage;
    /**
     * The message that gets displayed when something is wrong with the database connection
     */
    private final String sqlErrorMessage;
    /**
     * The message that gets displayed when the level was edited successfully
     */
    private final String groupLevelEditedMessage;
    /**
     * The message that gets displayed when there is no group with the given name
     */
    private final String groupNameDoesNotExistMessage;
    /**
     * The message that gets displayed when the given level is not an integer
     */
    private final String levelMustBeIntegerMessage;

    /**
     * Initialising the missing constants with the given values
     *
     * @param databaseManager the database manager
     * @param config          the config
     */
    public CommandEditGroupLevel(DatabaseManager databaseManager, FileConfiguration config) {
        super(databaseManager, config);
        if(!(config.isString("CommandEditGroupLevelInsufficientPermissionMessage")
                && config.isString("CommandEditGroupLevelUsageMessage")
                && config.isString("CommandEditGroupLevelSqlErrorMessage")
                && config.isString("CommandEditGroupLevelGroupLevelEditedMessage")
                && config.isString("CommandEditGroupLevelGroupNameDoesNotExistMessage")
                && config.isString("CommandEditGroupLevelLevelMustBeIntegerMessage")))
            throw new IllegalStateException();

        this.insufficientPermissionMessage = config.getString("CommandEditGroupLevelInsufficientPermissionMessage");
        this.usageMessage = config.getString("CommandEditGroupLevelUsageMessage");
        this.sqlErrorMessage = config.getString("CommandEditGroupLevelSqlErrorMessage");
        this.groupLevelEditedMessage = config.getString("CommandEditGroupLevelGroupLevelEditedMessage");
        this.groupNameDoesNotExistMessage = config.getString("CommandEditGroupLevelGroupNameDoesNotExistMessage");
        this.levelMustBeIntegerMessage = config.getString("CommandEditGroupLevelLevelMustBeIntegerMessage");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("groupplugin.editgrouplevel")) {
            sender.sendMessage(insufficientPermissionMessage);
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(usageMessage);
            return true;
        }
        int newLevel;
        try {
            newLevel = Integer.parseInt(args[1]);
        } catch(NumberFormatException e) {
            sender.sendMessage(levelMustBeIntegerMessage);
            return true;
        }
        editGroupLevel(sender, args[0], newLevel);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> list = new LinkedList<>();
        if (args.length == 1) {
            try {
                list.addAll(databaseManager.getAllGroups());
            } catch (SQLException e) {
                return list;
            }
        }
        return list;
    }

    // HELPER

    /**
     * Tries to edit the level of the group with the given name. Send an error message to the sender if editing the level goes wrong
     * @param sender the person trying to edit the group's level
     * @param group the group's name
     * @param newLevel the group's new level
     */
    private void editGroupLevel(CommandSender sender, String group, int newLevel) {
        try {
            databaseManager.editGroupLevel(group, newLevel);
            sender.sendMessage(groupLevelEditedMessage.replace("%group%", group).replace("%newlevel%", String.valueOf(newLevel)));
        } catch (SQLException e) {
            sender.sendMessage(sqlErrorMessage);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(getErrorMessage(e));
        }
    }

    /**
     * Returns the message that should be displayed for the given exception
     * @param e the given exception
     * @return the message that should be displayed
     */
    private String getErrorMessage(Exception e) {
        if(e.getMessage().equals("The given group does not exist!"))
            return groupNameDoesNotExistMessage;
        else
            return "Something went wrong!";
    }
}
