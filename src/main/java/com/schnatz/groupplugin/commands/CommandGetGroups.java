package com.schnatz.groupplugin.commands;

import com.schnatz.groupplugin.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;

/**
 * This command can be used to get all groups a player is member of.
 * Furthermore, it shows how long the player is still a member of that group.
 * @author Henry Schnatz
 */
public class CommandGetGroups extends DatabaseCommand {
    /**
     * The message the sender gets displayed when he has insufficient permission to perform the command
     */
    private final String insufficientPermissionMessage;
    /**
     * The message that explains the command's usage
     */
    private final String usageMessage;
    /**
     * The message that gets displayed when the given player could not be found
     */
    private final String playerNotFoundMessage;
    /**
     * The message that gets displayed when something is wrong with the database connection
     */
    private final String sqlErrorMessage;
    /**
     * The message that gets displayed before listing all groups the user is member of
     */
    private final String memberOfFollowingGroupsMessage;
    /**
     * The message that gets displayed upon listing groups with unknown time left
     */
    private final String memberOfGroupUnknownTimeMessage;
    /**
     * The message that gets displayed upon listing groups with unlimited time left
     */
    private final String memberOfGroupForEverMessage;
    /**
     * The message that gets displayed upon listing groups with limited time including days, hours, minutes and seconds
     */
    private final String memberOfGroupForTimesMessage;
    /**
     * The message that gets displayed upon listing groups with limited time including hours, minutes and seconds
     */
    private final String memberOfGroupForTimeNoDaysMessage;
    /**
     * The message that gets displayed upon listing groups with limited time including minutes and seconds
     */
    private final String memberOfGroupForTimeNoHoursMessage;
    /**
     * The message that gets displayed upon listing groups with limited time including seconds
     */
    private final String memberOfGroupForTimeNoMinutesMessage;

    /**
     * Initialising the missing constants with the given values
     *
     * @param databaseManager the database manager
     * @param config          the config
     */
    public CommandGetGroups(DatabaseManager databaseManager, FileConfiguration config) {
        super(databaseManager, config);
        if(!(config.isString("CommandGetGroupsInsufficientPermissionMessage")
                && config.isString("CommandGetGroupsUsageMessage")
                && config.isString("CommandGetGroupsPlayerNotFoundMessage")
                && config.isString("CommandGetGroupsSqlErrorMessage")
                && config.isString("CommandGetGroupsMemberOfFollowingGroupsMessage")
                && config.isString("CommandGetGroupsMemberOfGroupUnknownTimeMessage")
                && config.isString("CommandGetGroupsMemberOfGroupForEverMessage")
                && config.isString("CommandGetGroupsMemberOfGroupForTimesMessage")
                && config.isString("CommandGetGroupsMemberOfGroupForTimeNoDaysMessage")
                && config.isString("CommandGetGroupsMemberOfGroupForTimeNoHoursMessage")
                && config.isString("CommandGetGroupsMemberOfGroupForTimeNoMinutesMessage")))
            throw new IllegalStateException();

        this.insufficientPermissionMessage = config.getString("CommandGetGroupsInsufficientPermissionMessage");
        this.usageMessage = config.getString("CommandGetGroupsUsageMessage");
        this.playerNotFoundMessage = config.getString("CommandGetGroupsPlayerNotFoundMessage");
        this.sqlErrorMessage = config.getString("CommandGetGroupsSqlErrorMessage");
        this.memberOfFollowingGroupsMessage = config.getString("CommandGetGroupsMemberOfFollowingGroupsMessage");
        this.memberOfGroupUnknownTimeMessage = config.getString("CommandGetGroupsMemberOfGroupUnknownTimeMessage");
        this.memberOfGroupForEverMessage = config.getString("CommandGetGroupsMemberOfGroupForEverMessage");
        this.memberOfGroupForTimesMessage = config.getString("CommandGetGroupsMemberOfGroupForTimesMessage");
        this.memberOfGroupForTimeNoDaysMessage = config.getString("CommandGetGroupsMemberOfGroupForTimeNoDaysMessage");
        this.memberOfGroupForTimeNoHoursMessage = config.getString("CommandGetGroupsMemberOfGroupForTimeNoHoursMessage");
        this.memberOfGroupForTimeNoMinutesMessage = config.getString("CommandGetGroupsMemberOfGroupForTimeNoMinutesMessage");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!sender.hasPermission("groupplugin.getgroups")){
            sender.sendMessage(insufficientPermissionMessage);
            return true;
        }
        if(args.length != 1) {
            sender.sendMessage(usageMessage);
            return true;
        }
        List<String> groups;
        Player player = Bukkit.getPlayer(args[0]);
        if(player == null) {
            sender.sendMessage(playerNotFoundMessage.replace("%user%", args[0]));
            return true;
        }
        String uuid = String.valueOf(player.getUniqueId());
        try{
            groups = databaseManager.getGroups(uuid);
        } catch (SQLException e) {
            sender.sendMessage(sqlErrorMessage);
            return true;
        }
        sender.sendMessage(memberOfFollowingGroupsMessage.replace("%user%", args[0]));
        for(String g : groups) {
            LocalDateTime expirationTime;
            try {
                expirationTime = databaseManager.groupTimeLeft(uuid, g);
            } catch (SQLException e) {
                sender.sendMessage(memberOfGroupUnknownTimeMessage.replace("%group%", g));
                continue;
            }
            if(expirationTime == null) {
                continue;
            }
            if(expirationTime.equals(LocalDateTime.MAX)) {
                sender.sendMessage(memberOfGroupForEverMessage.replace("%group%", g));
                continue;
            }

            LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Berlin"));

            Duration duration = Duration.between(now, expirationTime);

            long days = duration.toDays();
            long hours = duration.toHours();
            long minutes = duration.toMinutes();
            long seconds = duration.getSeconds() - minutes*60;

            minutes = minutes - hours*60;
            hours = hours - days*24;

            if(days != 0){
                sender.sendMessage(memberOfGroupForTimesMessage.replace("%group%", g).replace("%days%", String.valueOf(days)).replace("%hours%", String.valueOf(hours)).replace("%minutes%", String.valueOf(minutes)).replace("%seconds%", String.valueOf(seconds)));
            } else if(hours != 0){
                sender.sendMessage(memberOfGroupForTimeNoDaysMessage.replace("%group%", g).replace("%hours%", String.valueOf(hours)).replace("%minutes%", String.valueOf(minutes)).replace("%seconds%", String.valueOf(seconds)));
            } else if(minutes != 0){
                sender.sendMessage(memberOfGroupForTimeNoHoursMessage.replace("%group%", g).replace("%minutes%", String.valueOf(minutes)).replace("%seconds%", String.valueOf(seconds)));
            } else {
                sender.sendMessage(memberOfGroupForTimeNoMinutesMessage.replace("%group%", g).replace("%seconds%", String.valueOf(seconds)));
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> list = new LinkedList<>();
        if(args.length == 1) {
            for(Player p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
        }
        return list;
    }
}
