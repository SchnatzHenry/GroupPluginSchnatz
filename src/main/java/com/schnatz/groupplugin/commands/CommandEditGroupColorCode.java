package com.schnatz.groupplugin.commands;

import com.schnatz.groupplugin.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class CommandEditGroupColorCode extends DatabaseCommand {
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
     * The message that gets displayed when the color code was edited successfully
     */
    private final String groupColorCodeEditedMessage;
    /**
     * The message that gets displayed when there is no group with the given name
     */
    private final String groupNameDoesNotExistMessage;
    /**
     * The message that gets displayed when the given color code is invalid
     */
    private final String invalidColorCodeMessage;

    /**
     * Initialising the missing constants with the given values
     *
     * @param databaseManager the database manager
     * @param config          the config
     */
    public CommandEditGroupColorCode(DatabaseManager databaseManager, FileConfiguration config) {
        super(databaseManager, config);
        // TODO init Strings
        this.insufficientPermissionMessage = "You have insufficient permission to use this command!";
        this.usageMessage = "Please use as following: /editgroupcolorcode <groupname> <newpcolorcode>";
        this.sqlErrorMessage = "Something went wrong internally, please try again later!";
        this.groupColorCodeEditedMessage = "Changed the color code of group %group% to %newcolorcode%!";
        this.groupNameDoesNotExistMessage = "There is no group with the given name!";
        this.invalidColorCodeMessage = "The given color code is invalid, please do only use the following 0-9 and a-f!";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("groupplugin.editgroupprefix")) {
            sender.sendMessage(insufficientPermissionMessage);
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(usageMessage);
            return true;
        }
        if(args[1].length() != 1) {
            sender.sendMessage(invalidColorCodeMessage);
            return true;
        }
        char colorCodeChar = args[1].charAt(0);
        int colorCode;
        try{
            colorCode = colorCodeCharToInt(colorCodeChar);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(invalidColorCodeMessage);
            return true;
        }
        editGroupPrefix(sender, args[0], colorCode);
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
     * Tries to edit the color code of the group with the given name. Send an error message to the sender if editing the color code goes wrong
     * @param sender the person trying to edit the group's color code
     * @param group the group's name
     * @param newColorCode the group's new color code
     */
    private void editGroupPrefix(CommandSender sender, String group, int newColorCode) {
        try {
            databaseManager.editGroupColorCode(group, newColorCode);
            sender.sendMessage(groupColorCodeEditedMessage.replace("%group%", group).replace("%newcolorcode%", String.valueOf(newColorCode)));
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
            case "The given group does not exist!" -> groupNameDoesNotExistMessage;
            default -> "Something went wrong!";
        };
    }

    /**
     * Returns the integer representation of the given color code character
     * @param colorCodeChar the given character
     * @return the integer representation of the given color code character
     * @throws IllegalArgumentException if the given character is not in range of 0-9 or a-f
     */
    private int colorCodeCharToInt(char colorCodeChar) throws IllegalArgumentException {
        int colorCode = Character.digit(colorCodeChar, 16);
        if(colorCode < 0)
            throw new IllegalArgumentException("Only the following characters are allowed: 0-9 and a-f");
        return colorCode;
    }
}
