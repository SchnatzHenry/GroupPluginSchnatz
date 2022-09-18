package com.schnatz.groupplugin.commands;

import com.schnatz.groupplugin.DatabaseManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;

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
     * The message that gets displayed when something is wrong with the database connection
     */
    private final String sqlErrorMessage;
    /**
     * The message that gets displayed when a sign should replace a block
     */
    private final String signMustReplaceAirMessage;
    /**
     * The message that gets displayed when a user could not be found
     */
    private final String userNotFoundMessage;
    /**
     * The message that gets displayed when the user is not member of a group
     */
    private final String notMemberOfGroupMessage;
    /**
     * The message taht gets displayed when the player is not standing on a solid block
     */
    private final String mustStandOnSolidBlockMessage;

    /**
     * A map mapping a player's uuid on the list of his signs
     */
    private static final Map<String, List<Block>> SIGNS;

    /*
     * initialising the static attributes
     */
    static {
        SIGNS = new HashMap<>();
    }

    /**
     * Initialising the missing constants with the given values
     *
     * @param databaseManager the database manager
     * @param config          the config
     */
    public CommandSign(DatabaseManager databaseManager, FileConfiguration config) {
        super(databaseManager, config);
        if(!(config.isString("CommandSignInsufficientPermissionMessage")
                && config.isString("CommandSignUsageMessage")
                && config.isString("CommandSignOnlyPlayersCanUseThisCommandMessage")
                && config.isString("CommandSignSqlErrorMessage")
                && config.isString("CommandSignUserNotFoundMessage")
                && config.isString("CommandSignSignMustReplaceAirMessage")
                && config.isString("CommandSignNotMemberOfGroupMessage")
                && config.isString("CommandSignMustStandOnSolidBlockMessage")))
            throw new IllegalStateException();

        this.insufficientPermissionMessage = config.getString("CommandSignInsufficientPermissionMessage");
        this.usageMessage = config.getString("CommandSignUsageMessage");
        this.onlyPlayersCanUseThisCommandMessage = config.getString("CommandSignOnlyPlayersCanUseThisCommandMessage");
        this.sqlErrorMessage = config.getString("CommandSignSqlErrorMessage");
        this.userNotFoundMessage = config.getString("CommandSignUserNotFoundMessage");
        this.signMustReplaceAirMessage = config.getString("CommandSignSignMustReplaceAirMessage");
        this.notMemberOfGroupMessage = config.getString("CommandSignNotMemberOfGroupMessage");
        this.mustStandOnSolidBlockMessage = config.getString("CommandSignMustStandOnSolidBlockMessage");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(onlyPlayersCanUseThisCommandMessage);
            return true;
        }
        if (!sender.hasPermission("sign")) {
            sender.sendMessage(insufficientPermissionMessage);
            return true;
        }
        if (args.length > 1) {
            sender.sendMessage(usageMessage);
            return true;
        }
        World world = player.getWorld();
        Block block = world.getBlockAt(player.getLocation());
        if (!world.getBlockAt(player.getLocation().add(0, -1, 0)).getType().isSolid()) {
            sender.sendMessage(mustStandOnSolidBlockMessage);
            return true;
        }
        if(!block.getType().isAir()){
            sender.sendMessage(signMustReplaceAirMessage);
            return true;
        }

        String playerName = player.getName();
        Player p = player;
        if (args.length == 1) {
            playerName = args[0];
            p = Bukkit.getPlayer(playerName);
            if (p == null) {
                sender.sendMessage(userNotFoundMessage);
                return true;
            }
        }

        try {
            String highestGroupName = databaseManager.getUsersGroupWithHighestLevel(p.getUniqueId().toString());

            block.setType(Material.OAK_SIGN);
            Sign sign = (Sign) block.getState();
            sign.setLine(0, "name:");
            sign.setLine(1, playerName);
            sign.setLine(2, "(highest) group:");
            sign.setLine(3, highestGroupName);
            sign.update();

            addSign(block, p.getUniqueId().toString());
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
        if(args.length == 1) {
            for(Player p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
            for(OfflinePlayer p : Bukkit.getOfflinePlayers()) {
                list.add(p.getName());
            }}
        return list;
    }

    // HELPER

    /**
     * Returns the message that should be displayed for the given exception
     * @param e the given exception
     * @return the message that should be displayed
     */
    private String getErrorMessage(Exception e) {
        if(e.getMessage().equals("The given user is not member of a group!"))
            return notMemberOfGroupMessage;
        else
            return "Something went wrong!";
    }

    /**
     * Adds the sign to the list of all signs
     * @param sign the sign to add
     * @param uuid the uuid of the player belonging to the sign
     */
    private static void addSign(Block sign, String uuid) {
        List<Block> playersSigns = SIGNS.get(uuid);
        if(playersSigns == null) {
            playersSigns = new LinkedList<>();
            playersSigns.add(sign);
            SIGNS.put(uuid, playersSigns);
        } else {
            playersSigns.add(sign);
            SIGNS.replace(uuid, playersSigns);
        }
    }

    /**
     * Removes all existing signs
     */
    public static void removeAllSigns() {
        for(List<Block> blocks : SIGNS.values()){
            for(Block block : blocks) {
                block.setType(Material.AIR);
            }
        }
        SIGNS.clear();
    }

    /**
     * Updates all signs belonging to the given player
     */
    public static void updateSigns(String uuid, String highestGroupName) {
        List<Block> playersSigns = SIGNS.get(uuid);
        if(playersSigns == null)
            return;
        for(Block block : playersSigns) {
            Sign sign = (Sign) block.getState();
            sign.setLine(3, highestGroupName);
            sign.update();
        }
    }
}