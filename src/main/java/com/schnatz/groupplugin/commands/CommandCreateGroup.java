package com.schnatz.groupplugin.commands;

import com.schnatz.groupplugin.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * This command can be used to create a group
 * @author Henry Schnatz
 */
public class CommandCreateGroup extends DatabaseCommand {
    /**
     * The message the sender gets displayed when he has insufficient permission to perform the command
     */
    private final String insufficientPermissionMessage;
    /**
     * The message that explains the command's usage
     */
    private final String usageMessage;
    /**
     * The default level for groups
     */
    private final int defaultGroupLevel;
    /**
     * The default group colorcode
     */
    private final int defaultGroupColorCode;
    /**
     * The message that gets displayed when something is wrong with the database connection
     */
    private final String sqlErrorMessage;
    /**
     * The message that gets displayed when the given group name does already exist
     */
    private final String groupNameDoesExistMessage;
    /**
     * The message that gets displayed when the given group name is too long
     */
    private final String groupNameTooLongMessage;
    /**
     * The message that gets displayed when the given prefix is too long
     */
    private final String prefixTooLongMessage;
    /**
     * The message that gets displayed when the given color code is invalid
     */
    private final String invalidColorCodeMessage;
    /**
     * The message that gets displayed when the group was created successfully
     */
    private final String groupCreatedMessage;
    /**
     * The message that gets displayed when the level can not be cast to an integer
     */
    private final String levelMustBeIntegerMessage;
    /**
     * Initialising the missing constants with the given values
     * @param databaseManager the database manager
     * @param config          the config
     */
    public CommandCreateGroup(DatabaseManager databaseManager, FileConfiguration config) {
        super(databaseManager, config);
        //TODO init Strings
        this.insufficientPermissionMessage = "You have insufficient permission to use this command!";
        this.usageMessage = "Please use as following: /createGroup <name>(30 characters) [<prefix>(10 characters) <level> <colorcode>(0-9, a-f)]";
        this.defaultGroupLevel = 0;
        this.defaultGroupColorCode = 0;
        this.sqlErrorMessage = "Something went wrong internally, please try again later";
        this.groupNameDoesExistMessage = "The given group name does already exist!";
        this.groupNameTooLongMessage = "The given group name is too long (max: 30 characters)!";
        this.prefixTooLongMessage = "The given prefix is too long (max: 10 characters)!";
        this.invalidColorCodeMessage = "The given color code is invalid, please do only use the following 0-9 and a-f!";
        this.groupCreatedMessage = "Group %name% created";
        this.levelMustBeIntegerMessage = "The given level must be a number!";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("groupplugin.creategroup")) {
            sender.sendMessage(insufficientPermissionMessage);
            return true;
        }
        if(args.length < 1 || args.length > 4) {
            sender.sendMessage(usageMessage);
            return true;
        }
        if(args.length == 1) {
            createGroup(sender, args[0], "", defaultGroupLevel, defaultGroupColorCode);
            return true;
        }
        if(args.length == 2) {
            createGroup(sender, args[0], args[1], defaultGroupLevel, defaultGroupColorCode);
            return true;
        }
        if(args.length == 3) {
            int level;
            try{
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(levelMustBeIntegerMessage);
                return true;
            }
            createGroup(sender, args[0], args[1], level, defaultGroupColorCode);
            return true;
        }
        int level;
        char colorCodeChar;
        try{
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(levelMustBeIntegerMessage);
            return true;
        }
        if(args[3].length() != 1) {
            sender.sendMessage(invalidColorCodeMessage);
            return true;
        }
        colorCodeChar = args[3].charAt(0);
        int colorCode;
        try{
            colorCode = colorCodeCharToInt(colorCodeChar);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(invalidColorCodeMessage);
            return true;
        }
        createGroup(sender, args[0], args[1], level, colorCode);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return new LinkedList<>();
    }

    // HELPER

    /**
     * Tries to create a group with the given arguments. Send an error message to the sender if creating the group goes wrong
     * @param sender the person trying to create the group
     * @param name the group's name
     * @param prefix the group's prefix
     * @param level the group's level
     * @param colorCode the group's color code
     */
    private void createGroup(CommandSender sender, String name, String prefix, int level, int colorCode){
        try {
            databaseManager.createGroup(name, prefix, level, colorCode);
            sender.sendMessage(groupCreatedMessage.replace("%name%", name));
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
            case "The given group name does already exist!" -> groupNameDoesExistMessage;
            case "Group names must only be 30 characters long!" -> groupNameTooLongMessage;
            case "Group prefixes must only be 10 characters long!" -> prefixTooLongMessage;
            case "The color codes must be within the range of 0 to 15" -> invalidColorCodeMessage;
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
