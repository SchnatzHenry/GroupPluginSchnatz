import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

/**
 * This class is used to add prefixes and color to the chat messages
 * @author Henry Schnatz
 */
public class GroupListener implements Listener {
    /**
     * The message format used when a player enters the server
     */
    private final String messageServerJoin;
    /**
     * The message format used when a player leaves the server
     */
    private final String messageServerLeave;
    /**
     * The message format used when a player writes in the chat
     */
    private final String messageChat;
    /**
     * The default group's name
     */
    private final String defaultGroupName;
    /**
     * The {@link DatabaseManager} used by the plugin
     */
    private final DatabaseManager databaseManager;

    /**
     * Initialises the missing variables with the given values
     * @param messageServerJoin the message that gets displayed when a player joins the server
     * @param messageServerLeave the message that gets displayed when a player leaves the server
     * @param messageChat the message that gets displayed when a player writes in the chat
     * @param defaultGroupName the default group's name
     * @param databaseManager the {@link DatabaseManager} used by the plugin
     */
    public GroupListener(String messageServerJoin, String messageServerLeave, String messageChat, String defaultGroupName, DatabaseManager databaseManager) {
        this.messageServerJoin = messageServerJoin;
        this.messageServerLeave = messageServerLeave;
        this.messageChat = messageChat;
        this.defaultGroupName = defaultGroupName;

        this.databaseManager = databaseManager;
    }

    /**
     * Catches messages send by the players and gives them the desired format and color
     * @param e the chat event triggered by a player writing a message
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String message = e.getMessage();
        String format = messageChat;
        format = replacePlaceMaker(format, player);
        format = format.replace("%message%", message);
        e.setFormat(format);
    }

    /**
     * Customizes the join message for each player. Adds the player to the default server group if he has none yet.
     * @param e the {@link PlayerJoinEvent} triggered by a player joining the server
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        try {
            if(databaseManager.getGroups(player.getUniqueId().toString()).isEmpty()){
                try {
                    databaseManager.addUserToGroup(player.getUniqueId().toString(), defaultGroupName);
                } catch (IllegalArgumentException|SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        String joinMessage = messageServerJoin;
        joinMessage = replacePlaceMaker(joinMessage, player);
        e.setJoinMessage(joinMessage);
    }

    /**
     * Customizes the quit message for each player.
     * @param e the {@link PlayerQuitEvent} triggered by a player quitting the server
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        String quitMessage = messageServerLeave;
        quitMessage = replacePlaceMaker(quitMessage, player);
        e.setQuitMessage(quitMessage);
    }

    /**
     * Replaces the following place-makers: %name%, %prefix% and %color% with the desired values.
     * @param text the text where the spacers will be replaced
     * @param player the player determining the values for the place-makers
     * @return the text with all place-makers replaced by the desired values
     */
    private String replacePlaceMaker(String text, Player player){
        text = text.replace("%name%", player.getName());
        text = text.replace("%prefix%", databaseManager.getPrefix(player.getUniqueId().toString()));
        text = text.replace("%color%", String.valueOf(databaseManager.getColorChar(player.getUniqueId().toString())));
        return text;
    }
}