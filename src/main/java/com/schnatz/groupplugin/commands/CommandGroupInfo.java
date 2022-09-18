package com.schnatz.groupplugin.commands;

import com.schnatz.groupplugin.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * This command can be used to request information about a group
 */
public class CommandGroupInfo extends DatabaseCommand {
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
     * The message that gets displayed when showing information about a group
     */
    private final String generalGroupInformationMessage;
    /**
     * The message that gets displayed when showing information about a group with no prefix
     */
    private final String generalGroupInformationNoPrefixMessage;
    /**
     * The message that gets displayed when listing all users of a group
     */
    private final String listGroupUsersMessage;
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
    public CommandGroupInfo(DatabaseManager databaseManager, FileConfiguration config) {
        super(databaseManager, config);
        if(!(config.isString("CommandGroupInfoInsufficientPermissionMessage")
                && config.isString("CommandGroupInfoUsageMessage")
                && config.isString("CommandGroupInfoSqlErrorMessage")
                && config.isString("CommandGroupInfoGeneralGroupInformationMessage")
                && config.isString("CommandGroupInfoGeneralGroupInformationNoPrefixMessage")
                && config.isString("CommandGroupInfoListGroupUsersMessage")
                && config.isString("CommandGroupInfoGroupNameDoesNotExistMessage")))
            throw new IllegalStateException();

        this.insufficientPermissionMessage = config.getString("CommandGroupInfoInsufficientPermissionMessage");
        this.usageMessage = config.getString("CommandGroupInfoUsageMessage");
        this.sqlErrorMessage = config.getString("CommandGroupInfoSqlErrorMessage");
        this.generalGroupInformationMessage = config.getString("CommandGroupInfoGeneralGroupInformationMessage");
        this.generalGroupInformationNoPrefixMessage = config.getString("CommandGroupInfoGeneralGroupInformationNoPrefixMessage");
        this.listGroupUsersMessage = config.getString("CommandGroupInfoListGroupUsersMessage");
        this.groupNameDoesNotExistMessage = config.getString("CommandGroupInfoGroupNameDoesNotExistMessage");
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
        if (args.length != 1) {
            sender.sendMessage(usageMessage);
            return true;
        }
        String groupName = args[0];
        try {
            String prefix = databaseManager.getGroupPrefix(groupName);
            int level = databaseManager.getGroupLevel(groupName);
            char colorCode = databaseManager.getGroupColorChar(groupName);
            List<String> users = databaseManager.getGroupsUsers(groupName);
            if(prefix.equals(""))
                sender.sendMessage(generalGroupInformationNoPrefixMessage.replace("%name%", groupName).replace("%colorcode%", String.valueOf(colorCode)).replace("%level%", String.valueOf(level)).replace("%usercount%", String.valueOf(users.size())));
            else
                sender.sendMessage(generalGroupInformationMessage.replace("%name%", groupName).replace("%prefix%", prefix).replace("%colorcode%", String.valueOf(colorCode)).replace("%level%", String.valueOf(level)).replace("%usercount%", String.valueOf(users.size())));
            for(String u : users)
                sender.sendMessage(listGroupUsersMessage.replace("%name%", Objects.requireNonNull(Bukkit.getPlayer(UUID.fromString(u))).getName()));
            return true;
        } catch (SQLException e) {
            sender.sendMessage(sqlErrorMessage);
            return true;
        } catch (IllegalArgumentException e) {
            sender.sendMessage(getErrorMessage(e));
            return true;
        }
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
     * Returns the message that should be displayed for the given exception
     * @param e the given exception
     * @return the message that should be displayed
     */
    private String getErrorMessage(Exception e) {
        return switch (e.getMessage()) {
            case "The given group does not exist", "The given group does not exist!" -> groupNameDoesNotExistMessage;
            default -> "Something went wrong!";
        };
    }
}
