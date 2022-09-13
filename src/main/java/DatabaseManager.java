import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is used to manage all database actions (database queries).
 * @author Henry Schnatz
 */
public class DatabaseManager {
    /**
     * The name of the table used to store the server groups
     */
    private static final String TABLE_GROUPS = "servergroup";
    /**
     * The name of the table used to store the users' attributes
     */
    private static final String TABLE_USERS  = "users";
    /**
     * The name of the column used to store the group name
     */
    private static final String TABLE_GROUPS_ATTRIBUTE_GROUPNAME = "groupname";
    /**
     * The name of the column used to store the group prefix
     */
    private static final String TABLE_GROUPS_ATTRIBUTE_GROUPPREFIX = "groupprefix";
    /**
     * The name of the column used to store the group level
     */
    private static final String TABLE_GROUPS_ATTRIBUTE_GROUPLEVEL = "grouplevel";
    /**
     * The name of the column used to store the group color code
     */
    private static final String TABLE_GROUPS_ATTRIBUTE_GROUPCOLORCODE = "groupcolorcode";
    /**
     * The name of the column used to store the user's uuid
     */
    private static final String TABLE_USERS_ATTRIBUTE_USERUUID = "useruuid";
    /**
     * The name of the column used to store the user's group
     */
    private static final String TABLE_USERS_ATTRIBUTE_USERGROUP = "groupname";
    /**
     * The name of the column used to store the expiration time of the user's group
     */
    private static final String TABLE_USERS_ATTRIBUTE_EXPIRATIONTIME = "expirationtime";
    /**
     * The IP address which is used to connect to the database
     */
    private final String ipAddress;
    /**
     * The port which is used to connect to the database
     */
    private final int port;
    /**
     * The user which is used when connecting to the database
     */
    private final String user;
    /**
     * The password that is used when connecting to the database
     */
    private final String password;
    /**
     * The name of the database to connect to
     */
    private final String databaseName;
    /**
     * The default group's name
     */
    private final String defaultGroupName;
    /**
     * The default group's prefix
     */
    private final String defaultGroupPrefix;
    /**
     * The default group's level
     */
    private final int defaultGroupLevel;
    /**
     * The default group's color code
     */
    private final int defaultGroupColorCode;
    /**
     * The {@link Connection} to the database
     */
    private Connection connection;

    /**
     * Initialises the missing variables with the given values
     * @param ipAddress the ip address used to access the database
     * @param port the port used to access the database
     * @param user the username used to access the database
     * @param password the password used to access the database
     * @param databaseName the name of the database
     * @param defaultGroupName the default group's name
     * @param defaultGroupPrefix the default group's prefix
     * @param defaultGroupLevel the default group's level
     * @param defaultGroupColorCode the default group's color code
     * @throws SQLException if connecting to the database or initialising it goes wrong
     */
    public DatabaseManager(String ipAddress, int port, String user, String password, String databaseName, String defaultGroupName, String defaultGroupPrefix, int defaultGroupLevel, int defaultGroupColorCode) throws SQLException {
        // initializing the missing class attributes
        this.ipAddress = ipAddress;
        this.port = port;
        this.user = user;
        this.password = password;
        this.databaseName = databaseName;
        this.defaultGroupName = defaultGroupName;
        this.defaultGroupPrefix = defaultGroupPrefix;
        this.defaultGroupLevel = defaultGroupLevel;
        this.defaultGroupColorCode = defaultGroupColorCode;

        // connecting to the database
        connect();

        // initializing tables
        initTables();
    }

    /**
     * Sets the {@link DatabaseManager#connection} to the database
     * @throws SQLException if a database access error occurs
     */
    private void connect() throws SQLException {
        // connecting to the database
        String url = "jdbc:mysql://" + ipAddress + ":" + port + "/";
        connection = DriverManager.getConnection(url, user, password);
        // creating the plugin's database if it does not exist yet
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
        }
        // connecting to the plugin's database
        url = "jdbc:mysql://" + ipAddress + ":" + port + "/" + databaseName;
        connection = DriverManager.getConnection(url, user, password);
    }

    /**
     * Creates the needed tables if they do not exist.
     * Fill's the tables with needed default values.
     * ATTENTION: If the tables already exist this method does not check whether they have the right columns!
     * @throws SQLException if an operation on the database goes wrong
     */
    private void initTables() throws SQLException {
        // creates the table that stores the groups if not existent
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_GROUPS + "(" + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " VARCHAR (30) NOT NULL, " + TABLE_GROUPS_ATTRIBUTE_GROUPPREFIX + " VARCHAR (10) NOT NULL, " + TABLE_GROUPS_ATTRIBUTE_GROUPLEVEL + " INTEGER NOT NULL, " + TABLE_GROUPS_ATTRIBUTE_GROUPCOLORCODE + " INTEGER NOT NULL, PRIMARY KEY(" + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + "))");
        }
        // insert the default group into the table if not existent
        boolean exists;
        try (Statement statement = connection.createStatement()) {
            ResultSet res = statement.executeQuery("SELECT " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " FROM " + TABLE_GROUPS + " WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + defaultGroupName + "'");
            exists = res.isBeforeFirst();
        }
        if (!exists) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO " + TABLE_GROUPS + "(" + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + ", " + TABLE_GROUPS_ATTRIBUTE_GROUPPREFIX + ", " + TABLE_GROUPS_ATTRIBUTE_GROUPLEVEL + ", " + TABLE_GROUPS_ATTRIBUTE_GROUPCOLORCODE + ") VALUES('" + defaultGroupName + "','" + defaultGroupPrefix + "','" + defaultGroupLevel + "'," + defaultGroupColorCode + ")");
            }
        }
        // creates the table that stores the users' groups if not existent
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "(" + TABLE_USERS_ATTRIBUTE_USERUUID + " VARCHAR(36) NOT NULL, " + TABLE_USERS_ATTRIBUTE_USERGROUP + " VARCHAR (30) NOT NULL, " + TABLE_USERS_ATTRIBUTE_EXPIRATIONTIME + " TIMESTAMP, PRIMARY KEY(" + TABLE_USERS_ATTRIBUTE_USERUUID + ", " + TABLE_USERS_ATTRIBUTE_USERGROUP + "))");
        }
    }

    public void createGroup(String name, String prefix, int level, int colorCode) throws IllegalArgumentException, SQLException {
        //TODO
        try (Statement statement = connection.createStatement()) {
            ResultSet res = statement.executeQuery("SELECT " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " FROM " + TABLE_GROUPS + " WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + name + "'");
            if(!res.isBeforeFirst())
                throw new IllegalArgumentException("The given group name does already exist!");
            statement.executeUpdate("INSERT INTO " + TABLE_GROUPS + "(" + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + ", " + TABLE_GROUPS_ATTRIBUTE_GROUPPREFIX + ", " + TABLE_GROUPS_ATTRIBUTE_GROUPLEVEL + ", " + TABLE_GROUPS_ATTRIBUTE_GROUPCOLORCODE + ") VALUES('" + name + "','" + prefix + "','" + level + "'," + colorCode + ")");
        }
    }

    public void renameGroup(String group, String newName) throws SQLException {
        //TODO
        try (Statement statement = connection.createStatement()) {
            ResultSet res = statement.executeQuery("SELECT " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " FROM " + TABLE_GROUPS + " WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + newName + "'");
            if(!res.isBeforeFirst())
                throw new IllegalArgumentException("The given group name does already exist!");
            statement.executeUpdate("UPDATE " + TABLE_GROUPS + " SET " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + newName + "' WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + group + "'");
        }
    }

    public void editGroupPrefix(String group, String prefix) throws SQLException {
        //TODO
        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE " + TABLE_GROUPS + " SET " + TABLE_GROUPS_ATTRIBUTE_GROUPPREFIX + " = '" + prefix + "' WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + group + "'");
        }
    }

    public void editGroupColorCode(String group, int colorCode) throws SQLException {
        //TODO
        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE " + TABLE_GROUPS + " SET " + TABLE_GROUPS_ATTRIBUTE_GROUPCOLORCODE + " = " + colorCode + " WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + group + "'");
        }
    }

    public void changeGroupLevel(String group, int level) throws SQLException {
        //TODO
        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE " + TABLE_GROUPS + " SET " + TABLE_GROUPS_ATTRIBUTE_GROUPLEVEL + " = " + level + " WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + group + "'");
        }
    }

    public void removeGroup(String group) {
        //TODO
    }

    public void addUserToGroup(String uuid, String group) throws IllegalArgumentException, SQLException {
        //TODO
        try (Statement statement = connection.createStatement()) {
            ResultSet res = statement.executeQuery("SELECT " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " FROM " + TABLE_GROUPS + " WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + group + "'");
            if(!res.isBeforeFirst())
                throw new IllegalArgumentException("The given group does not exist!");
            statement.executeUpdate("INSERT INTO " + TABLE_USERS + "(" + TABLE_USERS_ATTRIBUTE_USERUUID + ", " + TABLE_USERS_ATTRIBUTE_USERGROUP + ", " + TABLE_USERS_ATTRIBUTE_EXPIRATIONTIME + ") VALUES('" + uuid + "','" + group + "', NULL)");
        }
        updateUser(uuid);
    }

    public void addUserToGroup(String uuid, String group, int hours, int minutes, int seconds) {
        //TODO
    }

    private void removeUserFromGroup(String uuid, String group) {
        //TODO
    }

    public List<String> getGroups(String uuid) throws SQLException {
        //TODO
        List<String> list = new LinkedList<>();
        try(Statement statement = connection.createStatement()) {
            ResultSet res = statement.executeQuery("SELECT " + TABLE_USERS_ATTRIBUTE_USERGROUP + " FROM " + TABLE_USERS + " WHERE " + TABLE_USERS_ATTRIBUTE_USERUUID + " = '" + uuid + "'");
            while (!res.isClosed() && res.next()) {
                String groupName = res.getString(TABLE_USERS_ATTRIBUTE_USERGROUP);
                list.add(groupName);
            }
        }
        return list;
    }

    public int getGroupLevel(String group) {
        //TODO
        return 0;
    }

    public String getPrefix(String uuid) {
        //TODO
        return "prefix";
    }

    public char getColorChar(String uuid) {
        //TODO
        return 'c';
    }

    private void updateUser(String uuid) {
        //TODO
    }
}