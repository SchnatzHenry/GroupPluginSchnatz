package com.schnatz.groupplugin.commands;

import com.schnatz.groupplugin.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * This command can be used to delete a group
 * @author Henry Schnatz
 */
public class CommandDeleteGroup extends DatabaseCommand{
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
     * The message that gets displayed when the given group name does already exist
     */
    private final String groupNameDoesNotExistMessage;
    /**
     * The message that gets displayed when the group was deleted successfully
     */
    private final String groupDeletedMessage;
    /**
     * The message that gets displayed when trying to delete the default group
     */
    private final String deletingDefaultGroupMessage;

    /**
     * Initialising the missing constants with the given values
     * @param databaseManager the database manager
     * @param config          the config
     */
    public CommandDeleteGroup(DatabaseManager databaseManager, FileConfiguration config) {
        super(databaseManager, config);
        //TODO init Strings
        this.insufficientPermissionMessage = "You have insufficient permission to use this command!";
        this.usageMessage = "Please use as following: /deletegroup <name>";
        this.sqlErrorMessage = "Something went wrong internally, please try again later";
        this.groupNameDoesNotExistMessage = "The given group name does already exist!";
        this.groupDeletedMessage = "Group %name% deleted";
        this.deletingDefaultGroupMessage = "The default group must not be deleted!";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("groupplugin.deletegroup")) {
            sender.sendMessage(insufficientPermissionMessage);
            return true;
        }
        if(args.length != 1) {
            sender.sendMessage(usageMessage);
            return true;
        }
        deleteGroup(sender, args[0]);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new LinkedList<>();
        if(args.length == 1) {
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
     * Tries to delete a group with the given name. Send an error message to the sender if deleting the group goes wrong
     * @param sender the person trying to delete the group
     * @param name the group's name
     */
    private void deleteGroup(CommandSender sender, String name) {
        try{
            databaseManager.removeGroup(name);
            sender.sendMessage(groupDeletedMessage.replace("%name%", name));
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
            case "The default group must not be deleted" -> deletingDefaultGroupMessage;
            case "The given group does not exist!" -> groupNameDoesNotExistMessage;
            default -> "Something went wrong!";
        };
    }
}
