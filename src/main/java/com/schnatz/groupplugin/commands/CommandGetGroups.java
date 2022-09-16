package com.schnatz.groupplugin.commands;

import com.schnatz.groupplugin.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.SQLException;
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
        //TRODO init Strings
        this.insufficientPermissionMessage = "You have insufficient permission to use this command!";
        this.usageMessage = "Please use as following: /getgroups <playername>";
        this.playerNotFoundMessage = "Could not find a player named %user%";
        this.sqlErrorMessage = "Something went wrong internally, please try again later";
        this.memberOfFollowingGroupsMessage = "%user% is member of the following groups:";
        this.memberOfGroupUnknownTimeMessage = "%group%: unknown time left";
        this.memberOfGroupForEverMessage = "%group%";
        this.memberOfGroupForTimesMessage = "%group%: %days% days, %hours% hours, %minutes% minutes and %seconds% seconds left";
        this.memberOfGroupForTimeNoDaysMessage = "%group%: %hours% hours, %minutes% minutes and %seconds% seconds left";
        this.memberOfGroupForTimeNoHoursMessage = "%group%: %minutes% minutes and %seconds% seconds left";
        this.memberOfGroupForTimeNoMinutesMessage = "%group%: %seconds% seconds left";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO ingame mit den Zeitlimites testen!
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
            int timeLeft;
            try {
                timeLeft = databaseManager.groupTimeLeft(uuid, g);
            } catch (SQLException e) {
                timeLeft = -1;
            }
            if(timeLeft == -1) {
                sender.sendMessage(memberOfGroupUnknownTimeMessage.replace("%group%", g));
                continue;
            }
            if(timeLeft == Integer.MAX_VALUE) {
                sender.sendMessage(memberOfGroupForEverMessage.replace("%group%", g));
                continue;
            }

            int finalSeconds = timeLeft%60;
            int minutes = (timeLeft-finalSeconds)/60;
            int finalMinutes = minutes%60;
            int hours = (minutes-finalMinutes)/60;
            int finalHours = hours%24;
            int finalDays = (hours-finalHours)/24;

            if(finalDays != 0){
                sender.sendMessage(memberOfGroupForTimesMessage.replace("%group%", g).replace("%days%", String.valueOf(finalDays)).replace("hours", String.valueOf(finalHours)).replace("minutes", String.valueOf(finalMinutes)).replace("seconds", String.valueOf(finalSeconds)));
            } else if(finalHours != 0){
                sender.sendMessage(memberOfGroupForTimeNoDaysMessage.replace("%group%", g).replace("hours", String.valueOf(finalHours)).replace("minutes", String.valueOf(finalMinutes)).replace("seconds", String.valueOf(finalSeconds)));
            } else if(finalMinutes != 0){
                sender.sendMessage(memberOfGroupForTimeNoHoursMessage.replace("%group%", g).replace("minutes", String.valueOf(finalMinutes)).replace("seconds", String.valueOf(finalSeconds)));
            } else {
                sender.sendMessage(memberOfGroupForTimeNoMinutesMessage.replace("%group%", g).replace("seconds", String.valueOf(finalSeconds)));
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new LinkedList<>();
        if(args.length == 1) {
            for(Player p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
        }
        return list;
    }
}
