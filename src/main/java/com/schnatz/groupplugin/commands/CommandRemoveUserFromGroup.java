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

public class CommandRemoveUserFromGroup extends DatabaseCommand{
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
     * The message that gets displayed when the user is not member of the group
     */
    private final String userNotMemberOfGroupMessage;
    /**
     * The message that gets displayed when the user was successfully removed from the group
     */
    private final String removedUserFromGroupMessage;

    /**
     * Initialising the missing constants with the given values
     *
     * @param databaseManager the database manager
     * @param config          the config
     */
    public CommandRemoveUserFromGroup(DatabaseManager databaseManager, FileConfiguration config) {
        super(databaseManager, config);
        if(!(config.isString("CommandRemoveUserFromGroupInsufficientPermissionMessage")
                && config.isString("CommandRemoveUserFromGroupUsageMessage")
                && config.isString("CommandRemoveUserFromGroupSqlErrorMessage")
                && config.isString("CommandRemoveUserFromGroupGroupNameDoesNotExistMessage")
                && config.isString("CommandRemoveUserFromGroupPlayerNotFoundMessage")
                && config.isString("CommandRemoveUserFromGroupUserNotMemberOfGroupMessage")
                && config.isString("CommandRemoveUserFromGroupRemovedUserFromGroupMessage")))
            throw new IllegalStateException();

        this.insufficientPermissionMessage = config.getString("CommandRemoveUserFromGroupInsufficientPermissionMessage");
        this.usageMessage = config.getString("CommandRemoveUserFromGroupUsageMessage");
        this.sqlErrorMessage = config.getString("CommandRemoveUserFromGroupSqlErrorMessage");
        this.groupNameDoesNotExistMessage = config.getString("CommandRemoveUserFromGroupGroupNameDoesNotExistMessage");
        this.playerNotFoundMessage = config.getString("CommandRemoveUserFromGroupPlayerNotFoundMessage");
        this.userNotMemberOfGroupMessage = config.getString("CommandRemoveUserFromGroupUserNotMemberOfGroupMessage");
        this.removedUserFromGroupMessage = config.getString("CommandRemoveUserFromGroupRemovedUserFromGroupMessage");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("groupplugin.removeuserfromgroup")) {
            sender.sendMessage(insufficientPermissionMessage);
            return true;
        }
        if (args.length != 2) {
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
            databaseManager.removeUserFromGroup(playerUuid, groupName);
            sender.sendMessage(removedUserFromGroupMessage.replace("%user%", userName).replace("%group%", groupName));
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
            case "The given user is not a member of the given group!" -> userNotMemberOfGroupMessage;
            default -> "Something went wrong!";
        };
    }
}
