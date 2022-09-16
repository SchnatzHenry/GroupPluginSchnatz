package com.schnatz.groupplugin.commands;

import com.schnatz.groupplugin.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

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
        //TODO init Strings
        this.insufficientPermissionMessage = "You have insufficient permission to use this command!";
        this.usageMessage = "Please use as following: /editgroupname <oldname> <newname>";
        this.sqlErrorMessage = "Something went wrong internally, please try again later!";
        this.groupNameEditedMessage = "Changed the groupname of group %oldname% to %newname%";
        this.editingNameDefaultGroupMessage = "The default group's name must not be edited!";
        this.groupNameDoesNotExistMessage = "There is no group with the given group name!";
        this.newGroupNameTooLongMessage = "Group names must not be longer than 30 characters!";
        this.newGroupNameDoesAlreadyExist = "There is already a group with the given name!";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
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
     * @param sender the person trying to delete the group
     * @param oldName the group's old name
     * @param newName the group's new name
     */
    private void editGroupName(CommandSender sender, String oldName, String newName) {
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
            case "The default group must not be renamed" -> editingNameDefaultGroupMessage;
            case "The given group does not exist!" -> groupNameDoesNotExistMessage;
            case "Group names must only be 30 characters long!" -> newGroupNameTooLongMessage;
            case "The given new group name does already exist!" -> newGroupNameDoesAlreadyExist;
            default -> "Something went wrong!";
        };
    }
}
