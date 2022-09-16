package com.schnatz.groupplugin.commands;

import com.schnatz.groupplugin.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

/**
 * This command can be used to create a sign with information about a player
 * @author Henry Schnatz
 */
public class CommandSign extends DatabaseCommand {
    /**
     * The message the sender gets displayed when he has insufficient permission to perform the command
     */
    private final String insufficientPermissionMessage;
    /**
     * The message that explains the command's usage
     */
    private final String usageMessage;
    /**
     * The message that gets displayed when the command is not executed by a player
     */
    private final String onlyPlayersCanUseThisCommandMessage;
    /**
     * Initialising the missing constants with the given values
     *
     * @param databaseManager the database manager
     * @param config          the config
     */
    public CommandSign(DatabaseManager databaseManager, FileConfiguration config) {
        super(databaseManager, config);
        //TODO init Strings
        this.insufficientPermissionMessage = "You have insufficient permission to use this command!";
        this.usageMessage = "Please use as following: /sign <playername>]";
        this.onlyPlayersCanUseThisCommandMessage = "Only players can use this command!";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(onlyPlayersCanUseThisCommandMessage);
            return true;
        }
        if(!sender.hasPermission("sign")){
            sender.sendMessage(insufficientPermissionMessage);
            return true;
        }
        if(args.length > 1) {
            sender.sendMessage(usageMessage);
            return true;
        }
        if(args.length == 0) {
            //TODO summon sign at the player's position about himself
            return true;
        }
        //TODO summon sign at the player's position about the given player
        return true;



        //TODO:
        /*
        World world = Bukkit.getWorld("world");
        world.getBlockAt(-48, 68, -90).setType(Material.OAK_SIGN);
        Block block = world.getBlockAt(-48, 68, -90);
        Sign sign = (Sign) block.getState();
        sign.setLine(1, "test");
        sign.update();
         */
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
            for(OfflinePlayer p : Bukkit.getOfflinePlayers()) {
                list.add(p.getName());
            }}
        return list;
    }
}
