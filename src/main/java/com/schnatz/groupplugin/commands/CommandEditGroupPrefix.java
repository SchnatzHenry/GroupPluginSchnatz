package com.schnatz.groupplugin.commands;

import com.schnatz.groupplugin.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * This command can be used to edit a group's prefix
 * @author Henry Schnatz
 */
public class CommandEditGroupPrefix extends DatabaseCommand {
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
     * The message that gets displayed when the prefix was edited successfully
     */
    private final String groupPrefixEditedMessage;
    /**
     * The message that gets displayed when the given prefix is too long
     */
    private final String newGroupPrefixTooLongMessage;
    /**
     * The message that gets displayed when there is no group with the given name
     */
    private final String groupNameDoesNotExistMessage;

    /**
     * Initialising the missing constants with the given values
     *
     * @param databaseManager the database manager
     * @param config          the config
     */
    public CommandEditGroupPrefix(DatabaseManager databaseManager, FileConfiguration config) {
        super(databaseManager, config);
        if(!(config.isString("CommandEditGroupPrefixInsufficientPermissionMessage")
                && config.isString("CommandEditGroupPrefixUsageMessage")
                && config.isString("CommandEditGroupPrefixSqlErrorMessage")
                && config.isString("CommandEditGroupPrefixGroupPrefixEditedMessage")
                && config.isString("CommandEditGroupPrefixNewGroupPrefixTooLongMessage")
                && config.isString("CommandEditGroupPrefixGroupNameDoesNotExistMessage")))
            throw new IllegalStateException();

        this.insufficientPermissionMessage = config.getString("CommandEditGroupPrefixInsufficientPermissionMessage");
        this.usageMessage = config.getString("CommandEditGroupPrefixUsageMessage");
        this.sqlErrorMessage = config.getString("CommandEditGroupPrefixSqlErrorMessage");
        this.groupPrefixEditedMessage = config.getString("CommandEditGroupPrefixGroupPrefixEditedMessage");
        this.newGroupPrefixTooLongMessage = config.getString("CommandEditGroupPrefixNewGroupPrefixTooLongMessage");
        this.groupNameDoesNotExistMessage = config.getString("CommandEditGroupPrefixGroupNameDoesNotExistMessage");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("groupplugin.editgroupprefix")) {
            sender.sendMessage(insufficientPermissionMessage);
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(usageMessage);
            return true;
        }
        editGroupPrefix(sender, args[0], args[1]);
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
     * Tries to edit the prefix of the group with the given name. Send an error message to the sender if editing the prefix goes wrong
     * @param sender the person trying to edit the group's prefix
     * @param group the group's name
     * @param newPrefix the group's new prefix
     */
    private void editGroupPrefix(CommandSender sender, String group, String newPrefix) {
        try {
            databaseManager.editGroupPrefix(group, newPrefix);
            sender.sendMessage(groupPrefixEditedMessage.replace("%group%", group).replace("%newprefix%", newPrefix));
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
        return switch (e.getMessage()) {
            case "Group prefixes must only be 10 characters long!" -> newGroupPrefixTooLongMessage;
            case "The given group does not exist!" -> groupNameDoesNotExistMessage;
            default -> "Something went wrong!";
        };
    }
}
