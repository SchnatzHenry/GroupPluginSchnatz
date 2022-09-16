package com.schnatz.groupplugin;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is used to manage all database actions.
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
        connection.close();
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
        if (!existsGroup(defaultGroupName)) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO " + TABLE_GROUPS + "(" + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + ", " + TABLE_GROUPS_ATTRIBUTE_GROUPPREFIX + ", " + TABLE_GROUPS_ATTRIBUTE_GROUPLEVEL + ", " + TABLE_GROUPS_ATTRIBUTE_GROUPCOLORCODE + ") VALUES('" + defaultGroupName + "','" + defaultGroupPrefix + "','" + defaultGroupLevel + "'," + defaultGroupColorCode + ")");
            }
        }
        // creates the table that stores the users' groups if not existent
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "(" + TABLE_USERS_ATTRIBUTE_USERUUID + " VARCHAR(36) NOT NULL, " + TABLE_USERS_ATTRIBUTE_USERGROUP + " VARCHAR (30) NOT NULL, " + TABLE_USERS_ATTRIBUTE_EXPIRATIONTIME + " TIMESTAMP, PRIMARY KEY(" + TABLE_USERS_ATTRIBUTE_USERUUID + ", " + TABLE_USERS_ATTRIBUTE_USERGROUP + "))");
        }
    }

    /**
     * Creates a new group with the given parameters
     * @param name the group's name
     * @param prefix the group's prefix
     * @param level the group's level
     * @param colorCode the group's color code
     * @throws IllegalArgumentException if the given group name is empty/too long or the given group name already exists or the prefix is too long or the color code is not within the range of 0 to 15
     * @throws SQLException if something goes wrong with the database connection
     */
    public void createGroup(String name, String prefix, int level, int colorCode) throws IllegalArgumentException, SQLException {
        checkForEmptyString(name, "name");
        if(existsGroup(name))
            throw new IllegalArgumentException("The given group name does already exist!");
        if(name.length() > 30)
            throw new IllegalArgumentException("Group names must only be 30 characters long!");
        if(name.length() > 10)
            throw new IllegalArgumentException("Group prefixes must only be 10 characters long!");
        if(colorCode < 0 || colorCode > 15)
            throw new IllegalArgumentException("The color codes must be within the range of 0 to 15");
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO " + TABLE_GROUPS + "(" + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + ", " + TABLE_GROUPS_ATTRIBUTE_GROUPPREFIX + ", " + TABLE_GROUPS_ATTRIBUTE_GROUPLEVEL + ", " + TABLE_GROUPS_ATTRIBUTE_GROUPCOLORCODE + ") VALUES('" + name + "','" + prefix + "','" + level + "'," + colorCode + ")");
        }
    }

    /**
     * Changes the given group's name to the given new name
     * @param group the group's old name
     * @param newName the group's new name
     * @throws IllegalArgumentException if the given group name is empty or does not exist or is too long or the given new name already exists
     * @throws SQLException if something goes wrong with the database connection
     */
    public void editGroupName(String group, String newName) throws IllegalArgumentException, SQLException {
        checkForEmptyString(group, "group name");
        checkForEmptyString(newName, "new name");
        if(!existsGroup(group))
            throw new IllegalArgumentException("The given group does not exist!");
        if(group.length() > 30)
            throw new IllegalArgumentException("Group names must only be 30 characters long!");
        if(defaultGroupName.equals(group))
            throw new IllegalArgumentException("The default group must not be renamed!");
        if(existsGroup(newName))
            throw new IllegalArgumentException("The given new group name does already exist!");
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE " + TABLE_GROUPS + " SET " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + newName + "' WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + group + "'");
            statement.executeUpdate("UPDATE " + TABLE_USERS + " SET " + TABLE_USERS_ATTRIBUTE_USERGROUP + " = '" + newName + "' WHERE " + TABLE_USERS_ATTRIBUTE_USERGROUP + " = '" + group + "'");
        }
    }

    /**
     * Changes the given group's prefix to the given new prefix
     * @param group the group's name
     * @param prefix the group's new prefix
     * @throws IllegalArgumentException if the given group name is empty or does not exist or the prefix is too long
     * @throws SQLException if something goes wrong with the database connection
     */
    public void editGroupPrefix(String group, String prefix) throws IllegalArgumentException, SQLException {
        checkForEmptyString(group, "group name");
        if(!existsGroup(group))
            throw new IllegalArgumentException("The given group does not exist!");
        if(prefix.length() > 10)
            throw new IllegalArgumentException("Group prefixes must only be 10 characters long!");
        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE " + TABLE_GROUPS + " SET " + TABLE_GROUPS_ATTRIBUTE_GROUPPREFIX + " = '" + prefix + "' WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + group + "'");
        }
    }

    /**
     * Changes the given group's color code to the given new color code
     * @param group the group's name
     * @param colorCode the group's new color code
     * @throws IllegalArgumentException if the given group name is empty or does not exist or the color code is not within the range of 0 to 15
     * @throws SQLException if something goes wrong with the database connection
     */
    public void editGroupColorCode(String group, int colorCode) throws IllegalArgumentException, SQLException {
        checkForEmptyString(group, "group name");
        if(!existsGroup(group))
            throw new IllegalArgumentException("The given group does not exist!");
        if(colorCode < 0 || colorCode > 15)
            throw new IllegalArgumentException("The color codes must be within the range of 0 to 15");
        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE " + TABLE_GROUPS + " SET " + TABLE_GROUPS_ATTRIBUTE_GROUPCOLORCODE + " = " + colorCode + " WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + group + "'");
        }
    }

    /**
     * Changes the given group's level to the given new level
     * @param group the group's name
     * @param level the group's new level
     * @throws IllegalArgumentException if the given group name is empty or does not exist
     * @throws SQLException if something goes wrong with the database connection
     */
    public void editGroupLevel(String group, int level) throws IllegalArgumentException, SQLException {
        checkForEmptyString(group, "group name");
        if (!existsGroup(group))
            throw new IllegalArgumentException("The given group does not exist!");
        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE " + TABLE_GROUPS + " SET " + TABLE_GROUPS_ATTRIBUTE_GROUPLEVEL + " = " + level + " WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + group + "'");
        }
    }

    /**
     * Collects all users belonging to the given group
     * @param group the given group's name
     * @return a list of the group's members' uuids
     * @throws IllegalArgumentException if the given group name is empty or does not exist
     * @throws SQLException if something goes wrong with the database connection
     */
    public List<String> getGroupsUsers(String group) throws IllegalArgumentException, SQLException {
        checkForEmptyString(group, "group name");
        if(!existsGroup(group))
            throw new IllegalArgumentException("The given group does not exist!");
        List<String> list = new LinkedList<>();
        try(Statement statement = connection.createStatement()){
            ResultSet res = statement.executeQuery("SELECT " + TABLE_USERS_ATTRIBUTE_USERUUID + " FROM " + TABLE_USERS + " WHERE " + TABLE_USERS_ATTRIBUTE_USERGROUP + "='" + group + "'");
            while(!res.isClosed() && res.next()) {
                list.add(res.getString(1));
            }
        }
        return list;
    }

    /**
     * Deletes the given group unless it's the default group
     * @param group the group's name
     * @throws IllegalArgumentException if the given group name is empty or does not exist, or it's the default group's name
     * @throws SQLException if something goes wrong with the database connection
     */
    public void removeGroup(String group) throws IllegalArgumentException, SQLException {
        checkForEmptyString(group, "group name");
        if(defaultGroupName.equals(group))
            throw new IllegalArgumentException("The default group must not be deleted");
        if(!existsGroup(group))
            throw new IllegalArgumentException("The given group does not exist!");
        List<String> users = getGroupsUsers(group);
        for(String u : users) {
            removeUserFromGroup(u, group);
        }
        try(Statement statement = connection.createStatement()){
            statement.executeUpdate("DELETE FROM " + TABLE_GROUPS + " WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + group + "'");
        }
    }

    /**
     * Adds the given user to the given group
     * @param uuid the user's uuid
     * @param group the given group's name
     * @throws IllegalArgumentException if the given group name is empty or does not exist
     * @throws SQLException if something goes wrong with the database connection
     */
    public void addUserToGroup(String uuid, String group) throws IllegalArgumentException, SQLException {
        checkForEmptyString(group, "group name");
        checkForEmptyString(uuid, "uuid");
        if(!existsGroup(group))
            throw new IllegalArgumentException("The given group does not exist!");
        if(getGroups(uuid).contains(group))
            throw new IllegalArgumentException("The given user is already member of the given group!");
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO " + TABLE_USERS + "(" + TABLE_USERS_ATTRIBUTE_USERUUID + ", " + TABLE_USERS_ATTRIBUTE_USERGROUP + ", " + TABLE_USERS_ATTRIBUTE_EXPIRATIONTIME + ") VALUES('" + uuid + "','" + group + "', NULL)");
        }
        updateUser(uuid);
    }

    public void addUserToGroup(String uuid, String group, int hours, int minutes, int seconds) {
        //TODO
    }

    /**
     * Removes the given user from the given group
     * @param uuid the user's uuid
     * @param group the group's name
     * @throws IllegalArgumentException if at least one of the given parameters is an empty String or the given group does not exist or the given user is no member of the given group
     * @throws SQLException if something goes wrong with the database connection
     */
    public void removeUserFromGroup(String uuid, String group) throws IllegalArgumentException, SQLException{
        checkForEmptyString(uuid, "uuid");
        checkForEmptyString(group, "group name");
        if(!existsGroup(group))
            throw new IllegalArgumentException("The given group does not exist!");
        if(!getGroups(uuid).contains(group))
            throw new IllegalArgumentException("The given user is not a member of the given group!");
        try(Statement statement = connection.createStatement()){
            statement.executeUpdate("DELETE FROM " + TABLE_USERS + " WHERE " + TABLE_USERS_ATTRIBUTE_USERGROUP + " = '" + group + "' AND " + TABLE_USERS_ATTRIBUTE_USERUUID + " = '" + uuid + "'");
        }
        updateUser(uuid);
    }

    /**
     * Returns a list of all the user's groups' names
     * @param uuid the user's uuid
     * @return a list of all the groups' names the given user is member of
     * @throws IllegalArgumentException if the given uuid is an empty String
     * @throws SQLException if something goes wrong with the database connection
     */
    public List<String> getGroups(String uuid) throws IllegalArgumentException, SQLException {
        checkForEmptyString(uuid, "uuid");
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

    /**
     * Returns the given group's level
     * @param group the group's name
     * @return the given group's level
     * @throws IllegalArgumentException if the given group does not exist or is an empty String
     * @throws SQLException if something goes wrong with the database connection
     */
    public int getGroupLevel(String group) throws IllegalArgumentException, SQLException {
        checkForEmptyString(group, "group name");
        if(!existsGroup(group))
            throw new IllegalArgumentException("The given group does not exist");
        try(Statement statement = connection.createStatement()) {
            ResultSet res = statement.executeQuery("SELECT " + TABLE_GROUPS_ATTRIBUTE_GROUPLEVEL + " FROM " + TABLE_GROUPS + " WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + group + "'");
            res.next();
            return res.getInt(1);
        }
    }

    /**
     * Returns the given group's color char
     * @param group the group's name
     * @return the given group's color char
     * @throws IllegalArgumentException if the given group does not exist or is an empty String
     * @throws SQLException if something goes wrong with the database connection
     */
    public char getGroupColorChar(String group) throws IllegalArgumentException, SQLException {
        checkForEmptyString(group, "group name");
        if(!existsGroup(group))
            throw new IllegalArgumentException("The given group does not exist");
        int colorCode;
        try(Statement statement = connection.createStatement()) {
            ResultSet res = statement.executeQuery("SELECT " + TABLE_GROUPS_ATTRIBUTE_GROUPCOLORCODE + " FROM " + TABLE_GROUPS + " WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + group + "'");
            res.next();
            colorCode = res.getInt(1);
        }
        // coloCode -> char
        return Character.forDigit(colorCode, 16);
    }

    /**
     * Returns the given group's prefix
     * @param group the group's name
     * @return the given group's prefix
     * @throws IllegalArgumentException if the given group does not exist or is an empty String
     * @throws SQLException if something goes wrong with the database connection
     */
    public String getGroupPrefix(String group) throws IllegalArgumentException, SQLException {
        checkForEmptyString(group, "group name");
        if(!existsGroup(group))
            throw new IllegalArgumentException("The given group does not exist");
        try(Statement statement = connection.createStatement()) {
            ResultSet res = statement.executeQuery("SELECT " + TABLE_GROUPS_ATTRIBUTE_GROUPPREFIX + " FROM " + TABLE_GROUPS + " WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + group + "'");
            res.next();
            return res.getString(1);
        }
    }

    /**
     * Returns prefix for the given user - which is equal to the prefix of the group with the highest lvl the user is member of
     * @param uuid the user's uuid
     * @return the prefix for the given user
     * @throws IllegalArgumentException if the given uuid is an empty String or the user is not member of any groups
     * @throws SQLException if something goes wrong with the database connection
     */
    public String getUserPrefix(String uuid) throws IllegalArgumentException, SQLException {
        checkForEmptyString(uuid, "uuid");
        return getGroupPrefix(getUsersGroupWithHighestLevel(uuid));
    }

    /**
     * Returns the color char - which is equal to the color char of the group with the highest lvl the user is member of
     * @param uuid the user's uuid
     * @return the color char for the given user
     * @throws IllegalArgumentException if the given uuid is an empty String or the user is not member of any groups
     * @throws SQLException if something goes wrong with the database connection
     */
    public char getUserColorChar(String uuid) throws IllegalArgumentException, SQLException {
        checkForEmptyString(uuid, "uuid");
        return getGroupColorChar(getUsersGroupWithHighestLevel(uuid));
    }

    /**
     * Updates the given user's ingame appearance whenever necessary (e.g. tablists / scoreboards)
     * @param uuid the user's uuid
     */
    public void updateUser(String uuid) {
        //TODO
    }


    /**
     * Returns the time the user has left in that group in seconds
     * @param uuid the user's uuid
     * @param group the group's name
     * @return the time the user has left in that group in seconds
     * @throws IllegalArgumentException if the given groupname or username is an emty String
     * @throws SQLException if something goes wrong with the database connection
     */
    public int groupTimeLeft(String uuid, String group) throws IllegalArgumentException, SQLException {
        checkForEmptyString(uuid, "uuid");
        checkForEmptyString(group, "group");
        if(!getGroups(uuid).contains(group))
            return 0;
        //TODO Zeitlimits einfuegen
        return Integer.MAX_VALUE;
    }

    public List<String> getAllGroups() throws SQLException {
        try(Statement statement = connection.createStatement()){
            ResultSet res = statement.executeQuery("SELECT " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " FROM " + TABLE_GROUPS);
            List<String> list = new LinkedList<>();
            while(!res.isClosed() && res.next()) {
                list.add(res.getString(1));
            }
            return list;
        }
    }

    // HELPER
    /**
     * Checks whether the given groupname exists or not
     * @param groupname the given groupname
     * @return true if the given groupname does exist - false if the given groupname does not exist
     * @throws SQLException if something goes wrong with the database connection
     */
    private boolean existsGroup(String groupname) throws SQLException {
        try(Statement statement = connection.createStatement()) {
            ResultSet res = statement.executeQuery("SELECT " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " FROM " + TABLE_GROUPS + " WHERE " + TABLE_GROUPS_ATTRIBUTE_GROUPNAME + " = '" + groupname + "'");
            return res.isBeforeFirst();
        }
    }

    /**
     * Checks whether the given text is empty
     * @param text the given text
     * @throws IllegalArgumentException if the given text is empty
     */
    private void checkForEmptyString(String text, String variable) throws IllegalArgumentException {
        if(text.equals(""))
            throw new IllegalArgumentException("The " + variable + " must contain at least 1 character!");
    }

    /**
     * Returns the name of the group with the highest level that the given user is member of
     * @param uuid the user's uuid
     * @return the name of the group with the highest level that the given user is member of
     * @throws IllegalArgumentException if the user is not member of a group or the given uuid is an empty String
     * @throws SQLException if something goes wrong with the database connection
     */
    public String getUsersGroupWithHighestLevel(String uuid) throws IllegalArgumentException, SQLException {
        List<String> groups = getGroups(uuid);
        if(groups.isEmpty())
            throw new IllegalArgumentException("The given user is not member of a group!");
        // get the highest group level:
        int level = Integer.MIN_VALUE;
        for(String g : groups) {
            int glvl = getGroupLevel(g);
            if(glvl > level)
                level = glvl;
        }
        // pick the first group with that level:
        for(String g : groups) {
            if(getGroupLevel(g) == level)
                return g;
        }
        throw new IllegalStateException("Something went wrong while fetching the group with the highest level for user " + uuid + "!");
    }
}