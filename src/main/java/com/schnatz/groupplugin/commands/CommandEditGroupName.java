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
 * This command can be used to edit a group's name
 * @author Henry Schnatz
 */
public class CommandEditGroupName  extends DatabaseCommand {
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
     * The message that gets displayed when the group's name was edited successfully
     */
    private final String groupNameEditedMessage;
    /**
     * The message that gets displayed when trying to edit the default group's name
     */
    private final String editingNameDefaultGroupMessage;
    /**
     * The message that gets displayed when there is no group with the given (old)name
     */
    private final String groupNameDoesNotExistMessage;
    /**
     * The message that gets displayed when the given (new) group name is too long
     */
    private final String newGroupNameTooLongMessage;
    /**
     * The message that gets displayed when the given (new) group name does already exist
     */
    private final String newGroupNameDoesAlreadyExist;

    /**
     * Initialising the missing constants with the given values
     *
     * @param databaseManager the database manager
     * @param config          the config
     */
    public CommandEditGroupName(DatabaseManager databaseManager, FileConfiguration config) {
        super(databaseManager, config);
        if(!(config.isString("CommandEditGroupNameInsufficientPermissionMessage")
                && config.isString("CommandEditGroupNameUsageMessage")
                && config.isString("CommandEditGroupNameSqlErrorMessage")
                && config.isString("CommandEditGroupNameGroupNameEditedMessage")
                && config.isString("CommandEditGroupNameEditingNameDefaultGroupMessage")
                && config.isString("CommandEditGroupNameGroupNameDoesNotExistMessage")
                && config.isString("CommandEditGroupNameNewGroupNameTooLongMessage")
                && config.isString("CommandEditGroupNameNewGroupNameDoesAlreadyExist")))
            throw new IllegalStateException();

        this.insufficientPermissionMessage = config.getString("CommandEditGroupNameInsufficientPermissionMessage");
        this.usageMessage = config.getString("CommandEditGroupNameUsageMessage");
        this.sqlErrorMessage = config.getString("CommandEditGroupNameSqlErrorMessage");
        this.groupNameEditedMessage = config.getString("CommandEditGroupNameGroupNameEditedMessage");
        this.editingNameDefaultGroupMessage = config.getString("CommandEditGroupNameEditingNameDefaultGroupMessage");
        this.groupNameDoesNotExistMessage = config.getString("CommandEditGroupNameGroupNameDoesNotExistMessage");
        this.newGroupNameTooLongMessage = config.getString("CommandEditGroupNameNewGroupNameTooLongMessage");
        this.newGroupNameDoesAlreadyExist = config.getString("CommandEditGroupNameNewGroupNameDoesAlreadyExist");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("groupplugin.editgroupname")) {
            sender.sendMessage(insufficientPermissionMessage);
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(usageMessage);
            return true;
        }
        editGroupName(sender, args[0], args[1]);
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
     * Tries to edit the name of the group with the given name to the given new name. Send an error message to the sender if editing the group's name goes wrong
     * @param sender the person trying to edit the group's name
     * @param oldName the group's old name
     * @param newName the group's new name
     */
    private void editGroupName(@NotNull CommandSender sender, @NotNull String oldName, @NotNull String newName) {
        try {
            databaseManager.editGroupName(oldName, newName);
            sender.sendMessage(groupNameEditedMessage.replace("%oldname%", oldName).replace("%newname%", newName));
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
            case "The default group must not be renamed!" -> editingNameDefaultGroupMessage;
            case "The given group does not exist!" -> groupNameDoesNotExistMessage;
            case "Group names must only be 30 characters long!" -> newGroupNameTooLongMessage;
            case "The given new group name does already exist!" -> newGroupNameDoesAlreadyExist;
            default -> "Something went wrong!";
        };
    }
}
