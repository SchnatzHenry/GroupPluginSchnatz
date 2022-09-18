package com.schnatz.groupplugin.commands;

import com.schnatz.groupplugin.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * This command can be used to add a user to a group
 * @author Henry Schnatz
 */
public class CommandAddUserToGroup extends DatabaseCommand{
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
     * The message that gets displayed when there is no group with the given name
     */
    private final String groupNameDoesNotExistMessage;
    /**
     * The message that gets displayed when there is no user with the given name
     */
    private final String playerNotFoundMessage;
    /**
     * The message that gets displayed when the given user is already member of the given group
     */
    private final String userAlreadyMemberOfGroupMessage;
    /**
     * The message that gets displayed when the user was successfully added to the group
     */
    private final String addedUserToGroupMessage;
    /**
     * The message that gets displayed when a time specification is negative
     */
    private final String negativeTimeSpecificationMessage;
    /**
     * The message that gets displayed when the given time format is illegal
     */
    private final String illegalTimeFormatMessage;
    /**
     * The message that gets displayed when the given time specification is not a number
     */
    private final String timeSpecificationNotANumberMessage;
    /**
     * The message that gets displayed when the given time specification contains negative numbers
     */
    private final String timeSpecificationNegativeMessage;

    /**
     * Initialising the missing constants with the given values
     *
     * @param databaseManager the database manager
     * @param config          the config
     */
    public CommandAddUserToGroup(DatabaseManager databaseManager, FileConfiguration config) {
        super(databaseManager, config);
        if(!(config.isString("CommandAddUserToGroupInsufficientPermissionMessage")
                && config.isString("CommandAddUserToGroupUsageMessage")
                && config.isString("CommandAddUserToGroupSqlErrorMessage")
                && config.isString("CommandAddUserToGroupPlayerNotFoundMessage")
                && config.isString("CommandAddUserToGroupGroupNameDoesNotExistMessage")
                && config.isString("CommandAddUserToGroupUserAlreadyMemberOfGroupMessage")
                && config.isString("CommandAddUserToGroupAddedUserToGroupMessage")
                && config.isString("CommandAddUserToGroupNegativeTimeSpecificationMessage")
                && config.isString("CommandAddUserToGroupIllegalTimeFormatMessage")
                && config.isString("CommandAddUserToGroupTimeSpecificationNotANumberMessage")
                && config.isString("CommandAddUserToGroupTimeSpecificationNegativeMessage")))
            throw new IllegalStateException();

        this.insufficientPermissionMessage = config.getString("CommandAddUserToGroupInsufficientPermissionMessage");
        this.usageMessage = config.getString("CommandAddUserToGroupUsageMessage");
        this.sqlErrorMessage = config.getString("CommandAddUserToGroupSqlErrorMessage");
        this.groupNameDoesNotExistMessage = config.getString("CommandAddUserToGroupGroupNameDoesNotExistMessage");
        this.playerNotFoundMessage = config.getString("CommandAddUserToGroupPlayerNotFoundMessage");
        this.userAlreadyMemberOfGroupMessage = config.getString("CommandAddUserToGroupUserAlreadyMemberOfGroupMessage");
        this.addedUserToGroupMessage = config.getString("CommandAddUserToGroupAddedUserToGroupMessage");
        this.negativeTimeSpecificationMessage = config.getString("CommandAddUserToGroupNegativeTimeSpecificationMessage");
        this.illegalTimeFormatMessage = config.getString("CommandAddUserToGroupIllegalTimeFormatMessage");
        this.timeSpecificationNotANumberMessage = config.getString("CommandAddUserToGroupTimeSpecificationNotANumberMessage");
        this.timeSpecificationNegativeMessage = config.getString("CommandAddUserToGroupTimeSpecificationNegativeMessage");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("groupplugin.addusertogroup")) {
            sender.sendMessage(insufficientPermissionMessage);
            return true;
        }
        if (args.length == 0 || args.length > 3) {
            sender.sendMessage(usageMessage);
            return true;
        }
        String userName = args[0];
        Player p = Bukkit.getPlayer(userName);
        if(p == null) {
            sender.sendMessage(playerNotFoundMessage.replace("%name%", userName));
            return true;
        }
        String groupName = args[1];
        String playerUuid = p.getUniqueId().toString();
        try {
            if(args.length == 2)
                databaseManager.addUserToGroup(playerUuid, groupName);
            else {
                String timeSpecification = args[2];
                String[] times = timeSpecification.split(":");
                if(times.length > 4) {
                    sender.sendMessage(illegalTimeFormatMessage);
                    return true;
                }
                int days;
                int hours;
                int minutes;
                int seconds;
                try{
                    days = (times.length-4) < 0 ? 0 : Integer.parseInt(times[times.length-4]);
                    hours = (times.length-3) < 0 ? 0 : Integer.parseInt(times[times.length-3]);
                    minutes = (times.length-2) < 0 ? 0 : Integer.parseInt(times[times.length-2]);
                    seconds = (times.length-1) < 0 ? 0 : Integer.parseInt(times[times.length-1]);
                } catch (NumberFormatException e){
                    sender.sendMessage(timeSpecificationNotANumberMessage);
                    return true;
                }
                if(days < 0 || hours < 0 || minutes < 0 ||seconds < 0){
                    sender.sendMessage(timeSpecificationNegativeMessage);
                    return true;
                }
                databaseManager.addUserToGroup(playerUuid, groupName, days, hours, minutes, seconds);
            }
            sender.sendMessage(addedUserToGroupMessage.replace("%user%", userName).replace("%group%", groupName));
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
    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> list = new LinkedList<>();
        if(args.length == 1) {
            for(Player p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
        }
        if(args.length == 2) {
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
            case "The given group does not exist!" -> groupNameDoesNotExistMessage;
            case "The given times must be bigger than 0!" -> negativeTimeSpecificationMessage;
            case "The given user is already member of the given group!" -> userAlreadyMemberOfGroupMessage;
            default -> "Something went wrong!";
        };
    }
}
