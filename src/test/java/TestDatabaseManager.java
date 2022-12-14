import com.schnatz.groupplugin.DatabaseManager;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class is used to test class {@link DatabaseManager}
 * @author Henry Schnatz
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestDatabaseManager {
    /**
     * The database's port
     */
    private static final int PORT = 3306;
    /**
     * The database's ip adress
     */
    public static final String IPADRESS = "localhost";
    /**
     * The password used to connect to the database
     */
    private static final String PASSWORD = "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4";
    /**
     * The user used to connect to the database
     */
    private static final String USER = "root";
    /**
     * The database's name
     */
    private static final String DATABASE = "ServerGroupTest";

    /**
     * The database's short url
     */
    private static final String URL_SHORT = "jdbc:mysql://" + IPADRESS + ":" + PORT + "/";
    /**
     * The database's long url
     */
    private static final String URL_LONG = URL_SHORT + "ServerGroupTest";
    /**
     * The connection to the database
     */
    private Connection connection;
    /**
     * The database manager
     */
    private DatabaseManager dbManager;

    /**
     * Tests the constructor on an empty database
     */
    @Test
    @DisplayName("Constructor on empty database")
    void testConstructor1() {
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_SHORT, USER, PASSWORD));
        //deleting all existing databases
        assertDoesNotThrow(() -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("DROP DATABASE IF EXISTS servergrouptest")) {
                preparedStatement.executeUpdate();
            }
        });
        assertThrows(SQLException.class, () -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertDoesNotThrow(() -> {
            Field tableGroupsField = dbManager.getClass().getDeclaredField("TABLE_GROUPS");
            tableGroupsField.setAccessible(true);
            String tableGroups = (String) tableGroupsField.get(dbManager);

            Field defaultGroupNameField = dbManager.getClass().getDeclaredField("defaultGroupName");
            defaultGroupNameField.setAccessible(true);
            String defaultGroupName = (String) defaultGroupNameField.get(dbManager);

            Field defaultGroupPrefixField = dbManager.getClass().getDeclaredField("defaultGroupPrefix");
            defaultGroupPrefixField.setAccessible(true);
            String defaultGroupPrefix = (String) defaultGroupPrefixField.get(dbManager);

            try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + tableGroups)){
                ResultSet resultSet = preparedStatement.executeQuery();
                assertFalse(resultSet.isClosed());
                assertTrue(resultSet.next());
                assertEquals(resultSet.getString(1), defaultGroupName);
                assertEquals(resultSet.getString(2), defaultGroupPrefix);
            }
        });
    }

    /**
     * Tests the constructor on an existing database
     */
    @Test
    @DisplayName("Constructor on existing database")
    void testConstructor2() {
        testConstructor1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        assertDoesNotThrow(() -> {
            Field tableGroupsField = dbManager.getClass().getDeclaredField("TABLE_GROUPS");
            tableGroupsField.setAccessible(true);
            String tableGroups = (String) tableGroupsField.get(dbManager);

            Field defaultGroupNameField = dbManager.getClass().getDeclaredField("defaultGroupName");
            defaultGroupNameField.setAccessible(true);
            String defaultGroupName = (String) defaultGroupNameField.get(dbManager);

            Field defaultGroupPrefixField = dbManager.getClass().getDeclaredField("defaultGroupPrefix");
            defaultGroupPrefixField.setAccessible(true);
            String defaultGroupPrefix = (String) defaultGroupPrefixField.get(dbManager);

            try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + tableGroups)){
                ResultSet resultSet = preparedStatement.executeQuery();
                assertFalse(resultSet.isClosed());
                assertTrue(resultSet.next());
                assertEquals(resultSet.getString(1), defaultGroupName);
                assertEquals(resultSet.getString(2), defaultGroupPrefix);
            }
        });
    }

    /**
     * Tests the {@link DatabaseManager#createGroup(String, String, int, int)} method with a new group
     */
    @Test
    @DisplayName("createGroup() - new group")
    void testCreateGroup1() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertDoesNotThrow(() -> {
            Field tableGroupsField = dbManager.getClass().getDeclaredField("TABLE_GROUPS");
            tableGroupsField.setAccessible(true);
            String tableGroups = (String) tableGroupsField.get(dbManager);
            try(PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + tableGroups)){
                preparedStatement.executeUpdate();
            }
        });
        assertDoesNotThrow(() -> dbManager.createGroup("Group1", "Pref1", 1, 15));
        assertDoesNotThrow(() -> {
            Field tableGroupsField = dbManager.getClass().getDeclaredField("TABLE_GROUPS");
            tableGroupsField.setAccessible(true);
            String tableGroups = (String) tableGroupsField.get(dbManager);
            try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT groupname,groupprefix, grouplevel, groupcolorcode FROM " + tableGroups + " WHERE groupname = ?")){
                preparedStatement.setString(1, "Group1");
                ResultSet resultSet = preparedStatement.executeQuery();
                assertFalse(resultSet.isClosed());
                assertTrue(resultSet.next());
                assertEquals(resultSet.getString(1), "Group1");
                assertEquals(resultSet.getString(2), "Pref1");
                assertEquals(resultSet.getInt(3), 1);
                assertEquals(resultSet.getInt(4), 15);
            }
        });
    }

    /**
     * Tests the {@link DatabaseManager#createGroup(String, String, int, int)} method with a new group
     */
    @Test
    @DisplayName("createGroup() - existing group")
    void testCreateGroup2() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> dbManager.createGroup("Group1", "Pref1", 1, 15));
    }

    /**
     * Tests the {@link DatabaseManager#createGroup(String, String, int, int)} method with illegal arguments
     */
    @Test
    @DisplayName("createGroup() - illegal arguments")
    void testCreateGroup3() {
        testCreateGroup1();
        assertThrows(IllegalArgumentException.class, () -> dbManager.createGroup("", "Pref1", 1, 15));
        assertThrows(IllegalArgumentException.class, () -> dbManager.createGroup("Group2", "Pref2", 1, 16));
        assertThrows(IllegalArgumentException.class, () -> dbManager.createGroup("Group3", "Pref3", 1, -1));
        assertDoesNotThrow(() -> dbManager.createGroup("Group4", "Pref4", 1, 0));
        assertDoesNotThrow(() -> dbManager.createGroup("Group5", "Pref5", 1, 15));
        assertThrows(IllegalArgumentException.class, () -> dbManager.createGroup("TOOOOOOOOOOOOOOOOOOOOOOOOOOLONG", "TOOOOOOLONG", 1, 13));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupName(String, String)} method with an existing group
     */
    @Test
    @DisplayName("editGroupName() - existing group")
    void testEditGroupName1() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        testAddUserToGroup1();
        assertDoesNotThrow(() -> dbManager.editGroupName("Group1", "Group1Renamed"));
        assertDoesNotThrow(() -> {
            try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT groupname FROM servergroup WHERE groupname = 'Group1Renamed'")){
                ResultSet res = preparedStatement.executeQuery();
                assertTrue(res.next());
                assertEquals(res.getString(1), "Group1Renamed");
            }
        });
        assertDoesNotThrow(() -> {
            try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT useruuid FROM users WHERE groupname = 'Group1Renamed'")){
                ResultSet res = preparedStatement.executeQuery();
                List<String> actualUUIDs = new LinkedList<>();
                while(res.next()){
                    actualUUIDs.add(res.getString(1));
                }
                assertEquals(actualUUIDs.size(), 3);
                assertTrue(actualUUIDs.contains("useruuid4"));
                assertTrue(actualUUIDs.contains("useruuid5"));
                assertTrue(actualUUIDs.contains("useruuid6"));
            }
        });
    }

    /**
     * Tests the {@link DatabaseManager#editGroupName(String, String)} method with a non-existing group
     */
    @Test
    @DisplayName("editGroupName() - non-existing group")
    void testEditGroupName2() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupName("GroupNonExistent", "newName"));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupName(String, String)} method with illegal arguments
     */
    @Test
    @DisplayName("editGroupName() - illegal arguments")
    void testEditGroupName3() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupName("", "newName"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupName("", ""));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupName("oldName", ""));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupName("Group1", "TOOOOOOOOOOOOOOOOOOOOOOOOOOLONG"));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupName(String, String)} method with the default group
     */
    @Test
    @DisplayName("editGroupName() - default group")
    void testEditGroupName4() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupName("DefaultGroup", "newName"));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupPrefix(String, String)} method with an existing group
     */
    @Test
    @DisplayName("editGroupPrefix() - existing group")
    void testEditGroupPrefix1() {
        testCreateGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertDoesNotThrow(() -> dbManager.editGroupPrefix("Group1", "newPrefix"));
        assertDoesNotThrow(() -> {
            Field tableGroupsField = dbManager.getClass().getDeclaredField("TABLE_GROUPS");
            tableGroupsField.setAccessible(true);
            String tableGroups = (String) tableGroupsField.get(dbManager);
            try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT groupprefix FROM " + tableGroups + " WHERE groupname = ?")){
                preparedStatement.setString(1, "Group1");
                ResultSet res = preparedStatement.executeQuery();
                assertTrue(res.next());
                assertEquals(res.getString(1), "newPrefix");
            }
        });
    }

    /**
     * Tests the {@link DatabaseManager#editGroupPrefix(String, String)} method with a non-existing group
     */
    @Test
    @DisplayName("editGroupPrefix() - non-existing group")
    void testEditGroupPrefix2() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupPrefix("GroupNonExistent", "newPrefix"));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupPrefix(String, String)} method with illegal arguments
     */
    @Test
    @DisplayName("editGroupPrefix() - illegal arguments")
    void testEditGroupPrefix3() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupPrefix("", "newPrefix"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupPrefix("Group1", "TOOOOOOLONG"));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupColorCode(String, int)} with an existing group
     */
    @Test
    @DisplayName("editGroupColorCode() - existing group")
    void testEditGroupColorCode1() {
        testCreateGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertDoesNotThrow(() -> dbManager.editGroupColorCode("Group1", 10));
        assertDoesNotThrow(() -> {
            Field tableGroupsField = dbManager.getClass().getDeclaredField("TABLE_GROUPS");
            tableGroupsField.setAccessible(true);
            String tableGroups = (String) tableGroupsField.get(dbManager);
            try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT groupcolorcode FROM " + tableGroups + " WHERE groupname = ?")){
                preparedStatement.setString(1, "Group1");
                ResultSet res = preparedStatement.executeQuery();
                assertTrue(res.next());
                assertEquals(res.getInt(1), 10);
            }
        });
    }

    /**
     * Tests the {@link DatabaseManager#editGroupColorCode(String, int)} with a non-existing group
     */
    @Test
    @DisplayName("editGroupColorCode() - non-existing group")
    void testEditGroupColorCode2() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupColorCode("GroupNonExistent", -1));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupColorCode(String, int)} method with illegal arguments
     */
    @Test
    @DisplayName("editGroupColorCode() - illegal arguments")
    void testEditGroupColorCode3() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        testCreateGroup1();
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupColorCode("", 13));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupColorCode("Group1", -1));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupColorCode("Group1", 16));
        assertDoesNotThrow(() -> dbManager.editGroupColorCode("Group1", 0));
        assertDoesNotThrow(() -> dbManager.editGroupColorCode("Group1", 15));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupLevel(String, int)} with an existing group
     */
    @Test
    @DisplayName("editGroupLevel() - existing group")
    void testEditGroupLevel1() {
        testCreateGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertDoesNotThrow(() -> dbManager.editGroupLevel("Group1", -1));
        assertDoesNotThrow(() -> {
            Field tableGroupsField = dbManager.getClass().getDeclaredField("TABLE_GROUPS");
            tableGroupsField.setAccessible(true);
            String tableGroups = (String) tableGroupsField.get(dbManager);
            try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT grouplevel FROM " + tableGroups + " WHERE groupname = ?")){
                preparedStatement.setString(1, "Group1");
                ResultSet res = preparedStatement.executeQuery();
                assertTrue(res.next());
                assertEquals(res.getInt(1), -1);
            }
        });
    }

    /**
     * Tests the {@link DatabaseManager#editGroupLevel(String, int)} with a non-existing group
     */
    @Test
    @DisplayName("editGroupLevel() - non-existing group")
    void testEditGroupLevel2() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupLevel("GroupNonExistent", -1));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupLevel(String, int)} method with an empty groupname
     */
    @Test
    @DisplayName("editGroupLevel() - empty groupname")
    void testEditGroupLevel3() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupLevel("", -1));
    }

    /**
     * Tests the {@link DatabaseManager#getGroupsUsers(String)} method with an existing group
     */
    @Test
    @DisplayName("getGroupsUsers() - existing group")
    void testGetGroupsUsers1() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        testAddUserToGroup1();
        assertDoesNotThrow(() -> dbManager.createGroup("Group2", "g2pref", 1, 3));
        assertDoesNotThrow(() -> dbManager.createGroup("Group3", "g3pref", 2, 5));
        assertDoesNotThrow(() -> dbManager.addUserToGroup("useruuid1", "Group2"));
        List<String> expectedUsersGroup1 = new LinkedList<>();
        expectedUsersGroup1.add("useruuid4");
        expectedUsersGroup1.add("useruuid5");
        expectedUsersGroup1.add("useruuid6");
        List<String> expectedUsersGroup2 = new LinkedList<>();
        expectedUsersGroup2.add("useruuid1");

        List<String> actualUsersGroup1 = assertDoesNotThrow(() -> dbManager.getGroupsUsers("Group1"));
        List<String> actualUsersGroup2 = assertDoesNotThrow(() -> dbManager.getGroupsUsers("Group2"));
        List<String> actualUsersGroup3 = assertDoesNotThrow(() -> dbManager.getGroupsUsers("Group3"));
        assertEquals(actualUsersGroup1.size(), 3);
        expectedUsersGroup1.forEach(u -> assertTrue(actualUsersGroup1.contains(u)));
        assertEquals(actualUsersGroup2.size(), 1);
        expectedUsersGroup2.forEach(u -> assertTrue(actualUsersGroup2.contains(u)));
        assertTrue(actualUsersGroup3.isEmpty());
    }

    /**
     * Tests the {@link DatabaseManager#getGroupsUsers(String)} method with a non-existing group
     */
    @Test
    @DisplayName("getGroupsUsers() - non-existing group")
    void testGetGroupsUsers2() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> dbManager.getGroupsUsers("GroupNonExistent"));
    }

    /**
     * Tests the {@link DatabaseManager#getGroupsUsers(String)} method with an empty groupname
     */
    @Test
    @DisplayName("getGroupsUsers() - empty groupname")
    void testGetGroupsUsers3() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> dbManager.getGroupsUsers(""));
    }

    /**
     * Tests the {@link DatabaseManager#addUserToGroup(String, String)} method with an existing group
     */
    @Test
    @DisplayName("addUserToGroup() - unlimited - existing group")
    void testAddUserToGroup1() {
        testCreateGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertDoesNotThrow(() -> {
            Field tableUsersField = dbManager.getClass().getDeclaredField("TABLE_USERS");
            tableUsersField.setAccessible(true);
            String tableUsers = (String) tableUsersField.get(dbManager);
            try(PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + tableUsers)){
                preparedStatement.executeUpdate();
            }
        });
        assertDoesNotThrow(() -> dbManager.addUserToGroup("useruuid4", "Group1"));
        assertDoesNotThrow(() -> dbManager.addUserToGroup("useruuid5", "Group1"));
        assertDoesNotThrow(() -> dbManager.addUserToGroup("useruuid6", "Group1"));
        assertDoesNotThrow(() -> {
            try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT useruuid,expirationtime FROM users WHERE groupname = ?")){
                preparedStatement.setString(1, "Group1");
                ResultSet res = preparedStatement.executeQuery();
                assertTrue(res.next());
                List<String> remainingUsers = new LinkedList<>();
                remainingUsers.add("useruuid4");
                remainingUsers.add("useruuid5");
                remainingUsers.add("useruuid6");

                assertTrue(remainingUsers.contains(res.getString(1)));
                remainingUsers.remove(res.getString(1));
                assertNull(res.getObject(2));
                assertTrue(res.next());
                assertTrue(remainingUsers.contains(res.getString(1)));
                remainingUsers.remove(res.getString(1));
                assertNull(res.getObject(2));
                assertTrue(res.next());
                assertTrue(remainingUsers.contains(res.getString(1)));
                remainingUsers.remove(res.getString(1));
                assertNull(res.getObject(2));
                assertFalse(res.next());
            }
        });
    }

    /**
     * Tests the {@link DatabaseManager#addUserToGroup(String, String)} method with illegal arguments
     */
    @Test
    @DisplayName("addUserToGroup() - unlimited - illegal arguments")
    void testAddUserToGroup2() {
        testCreateGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid1", "GroupNonExistent"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid2", "GroupNonExistent"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid3", "GroupNonExistent"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("", "Group1"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid1", ""));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("", ""));
    }

    /**
     * Tests the {@link DatabaseManager#addUserToGroup(String, String, int, int, int, int)} method with an existing group
     */
    @Test
    @DisplayName("addUserToGroup() - limited - existing group")
    void testAddUserToGroup3() {
        int treshold = 5;

        testCreateGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertDoesNotThrow(() -> {
            Field tableUsersField = dbManager.getClass().getDeclaredField("TABLE_USERS");
            tableUsersField.setAccessible(true);
            String tableUsers = (String) tableUsersField.get(dbManager);
            try(PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + tableUsers)){
                preparedStatement.executeUpdate();
            }
        });
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Berlin"));
        assertDoesNotThrow(() -> dbManager.addUserToGroup("useruuid4", "Group1", 4,0,0,0));
        assertDoesNotThrow(() -> dbManager.addUserToGroup("useruuid5", "Group1", 4, 3, 2, 1));
        assertDoesNotThrow(() -> dbManager.addUserToGroup("useruuid6", "Group1", 366, 0, 0, 0));
        assertDoesNotThrow(() -> {
            try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT useruuid,expirationtime FROM users WHERE groupname = ?")){
                preparedStatement.setString(1, "Group1");
                ResultSet res = preparedStatement.executeQuery();
                assertTrue(res.next());
                Map<String, LocalDateTime> remainingEntries = new HashMap<>();
                remainingEntries.put("useruuid4", now.plusDays(4).plusHours(0).plusMinutes(0).plusSeconds(0));
                remainingEntries.put("useruuid5", now.plusDays(4).plusHours(3).plusMinutes(2).plusSeconds(1));
                remainingEntries.put("useruuid6", now.plusDays(366).plusHours(0).plusMinutes(0).plusSeconds(0));

                Duration duration;
                assertTrue(remainingEntries.containsKey(res.getString(1)));
                duration = Duration.between(remainingEntries.get(res.getString(1)), res.getTimestamp(2).toLocalDateTime());
                assertTrue(duration.toSeconds() < treshold);
                remainingEntries.remove(res.getString(1));
                assertTrue(res.next());

                assertTrue(remainingEntries.containsKey(res.getString(1)));
                duration = Duration.between(remainingEntries.get(res.getString(1)), res.getTimestamp(2).toLocalDateTime());
                assertTrue(duration.toSeconds() < treshold);
                remainingEntries.remove(res.getString(1));

                assertTrue(res.next());
                assertTrue(remainingEntries.containsKey(res.getString(1)));
                duration = Duration.between(remainingEntries.get(res.getString(1)), res.getTimestamp(2).toLocalDateTime());
                assertTrue(duration.toSeconds() < treshold);
                remainingEntries.remove(res.getString(1));

                assertFalse(res.next());
            }
        });
    }

    /**
     * Tests the {@link DatabaseManager#addUserToGroup(String, String, int, int, int, int)} method with illegal arguments
     */
    @Test
    @DisplayName("addUserToGroup() - limited - illegal arguments")
    void testAddUserToGroup4() {
        testCreateGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid1", "Group1", -1, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid1", "Group1", 0, -1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid1", "Group1", 0, 0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid1", "Group1", 0, 0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid1", "GroupNonExistent", 4, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid2", "GroupNonExistent", 4, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid3", "GroupNonExistent", 4, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("", "Group1", 4, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid1", "", 4, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("", "", 4, 0, 0, 0));
    }

    /**
     * Tests the {@link DatabaseManager#removeUserFromGroup(String, String)} method
     */
    @Test
    @DisplayName("removeUserFromGroup() - general")
    void testRemoveUserFromGroup1() {
        testAddUserToGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertDoesNotThrow(() -> {
            dbManager.createGroup("Group2", "g2Pref", 0, 0);
            dbManager.addUserToGroup("useruuid1", "Group1");
            dbManager.addUserToGroup("useruuid1", "Group2");
            dbManager.addUserToGroup("useruuid2", "Group1");
            assertTrue(dbManager.getGroups("useruuid1").contains("Group1"));
            assertTrue(dbManager.getGroups("useruuid1").contains("Group2"));
            assertTrue(dbManager.getGroups("useruuid2").contains("Group1"));
            dbManager.removeUserFromGroup("useruuid1", "Group1");
            assertFalse(dbManager.getGroups("useruuid1").contains("Group1"));
            assertTrue(dbManager.getGroups("useruuid1").contains("Group2"));
            assertTrue(dbManager.getGroups("useruuid2").contains("Group1"));
        });
    }

    /**
     * Tests the {@link DatabaseManager#removeUserFromGroup(String, String)} method on illegal inputs
     */
    @Test
    @DisplayName("removeUserFromGroup() - illegal inputs")
    void testRemoveUserFromGroup2() {
        testAddUserToGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> dbManager.removeUserFromGroup("useruuid1", "Group2"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.removeUserFromGroup("", "Group2"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.removeUserFromGroup("", ""));
        assertThrows(IllegalArgumentException.class, () -> dbManager.removeUserFromGroup("useruuid1", ""));
    }

    /**
     * Tests the {@link DatabaseManager#getGroups(String)} method
     */
    @Test
    @DisplayName("getGroups()")
    void testGetGroups() {
        testAddUserToGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertDoesNotThrow(() -> {
            List<String> actualUser4 = dbManager.getGroups("useruuid4");
            assertEquals(actualUser4.size(), 1);
            assertTrue(actualUser4.contains("Group1"));
            dbManager.createGroup("Group2", "g2pref", 0 , 0);
            dbManager.addUserToGroup("useruuid4", "Group2");
            actualUser4 = dbManager.getGroups("useruuid4");
            assertEquals(actualUser4.size(), 2);
            assertTrue(actualUser4.contains("Group1"));
            assertTrue(actualUser4.contains("Group2"));
            dbManager.removeUserFromGroup("useruuid4", "Group1");
            actualUser4 = dbManager.getGroups("useruuid4");
            assertEquals(actualUser4.size(), 1);
            assertTrue(actualUser4.contains("Group2"));
            dbManager.removeUserFromGroup("useruuid4", "Group2");
            actualUser4 = dbManager.getGroups("useruuid4");
            assertTrue(actualUser4.isEmpty());
            List<String> actualUser1 = dbManager.getGroups("useruuid1");
            assertTrue(actualUser1.isEmpty());
        });

        assertThrows(IllegalArgumentException.class, () -> dbManager.getGroups(""));
    }

    /**
     * Tests the {@link DatabaseManager#removeGroup(String)} method
     */
    @Test
    @DisplayName("removeGroup()")
    void testRemoveGroup() {
        testAddUserToGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> dbManager.removeGroup("DefaultGroup"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.removeGroup(""));
        assertThrows(IllegalArgumentException.class, () -> dbManager.removeGroup("Group2"));
        assertDoesNotThrow(() -> {
            assertTrue(dbManager.getGroups("useruuid4").contains("Group1"));
            assertTrue(dbManager.getGroups("useruuid5").contains("Group1"));
            assertTrue(dbManager.getGroups("useruuid6").contains("Group1"));
            dbManager.removeGroup("Group1");
            assertFalse(dbManager.getGroups("useruuid4").contains("Group1"));
            assertFalse(dbManager.getGroups("useruuid5").contains("Group1"));
            assertFalse(dbManager.getGroups("useruuid6").contains("Group1"));
        });
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid1", "Group1"));
    }

    /**
     * Tests the {@link DatabaseManager#getGroupLevel(String)} method
     */
    @Test
    @DisplayName("getGroupLevel()")
    void testGetGroupLevel() {
        testCreateGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertDoesNotThrow(() -> {
            assertEquals(dbManager.getGroupLevel("Group1"), 1);
            dbManager.editGroupLevel("Group1", 20);
            assertEquals(dbManager.getGroupLevel("Group1"), 20);
        });
        assertThrows(IllegalArgumentException.class, () -> dbManager.getGroupLevel(""));
        assertThrows(IllegalArgumentException.class, () -> dbManager.getGroupLevel("Group2"));
    }

    /**
     * Tests the {@link DatabaseManager#getGroupColorChar(String)} method
     */
    @Test
    @DisplayName("getGroupColorChar()")
    void testGetGroupColorChar() {
        testCreateGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertDoesNotThrow(() -> {
            assertEquals(dbManager.getGroupColorChar("Group1"), 'f');
            dbManager.editGroupColorCode("Group1", 14);
            assertEquals(dbManager.getGroupColorChar("Group1"), 'e');
            dbManager.editGroupColorCode("Group1", 13);
            assertEquals(dbManager.getGroupColorChar("Group1"), 'd');
            dbManager.editGroupColorCode("Group1", 12);
            assertEquals(dbManager.getGroupColorChar("Group1"), 'c');
            dbManager.editGroupColorCode("Group1", 11);
            assertEquals(dbManager.getGroupColorChar("Group1"), 'b');
            dbManager.editGroupColorCode("Group1", 10);
            assertEquals(dbManager.getGroupColorChar("Group1"), 'a');
            dbManager.editGroupColorCode("Group1", 9);
            assertEquals(dbManager.getGroupColorChar("Group1"), '9');
            dbManager.editGroupColorCode("Group1", 8);
            assertEquals(dbManager.getGroupColorChar("Group1"), '8');
            dbManager.editGroupColorCode("Group1", 7);
            assertEquals(dbManager.getGroupColorChar("Group1"), '7');
            dbManager.editGroupColorCode("Group1", 6);
            assertEquals(dbManager.getGroupColorChar("Group1"), '6');
            dbManager.editGroupColorCode("Group1", 5);
            assertEquals(dbManager.getGroupColorChar("Group1"), '5');
            dbManager.editGroupColorCode("Group1", 4);
            assertEquals(dbManager.getGroupColorChar("Group1"), '4');
            dbManager.editGroupColorCode("Group1", 3);
            assertEquals(dbManager.getGroupColorChar("Group1"), '3');
            dbManager.editGroupColorCode("Group1", 2);
            assertEquals(dbManager.getGroupColorChar("Group1"), '2');
            dbManager.editGroupColorCode("Group1", 1);
            assertEquals(dbManager.getGroupColorChar("Group1"), '1');
            dbManager.editGroupColorCode("Group1", 0);
            assertEquals(dbManager.getGroupColorChar("Group1"), '0');
        });
        assertThrows(IllegalArgumentException.class, () -> dbManager.getGroupColorChar(""));
        assertThrows(IllegalArgumentException.class, () -> dbManager.getGroupColorChar("Group2"));
    }

    /**
     * Tests the {@link DatabaseManager#getGroupPrefix(String)} method
     */
    @Test
    @DisplayName("getGroupPrefix()")
    void testGetGroupPrefix() {
        testCreateGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager(IPADRESS, PORT, USER, PASSWORD, DATABASE, "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection(URL_LONG, USER, PASSWORD));
        assertDoesNotThrow(() -> {
            assertEquals(dbManager.getGroupPrefix("Group1"), "Pref1");
            dbManager.editGroupPrefix("Group1", "testPref");
            assertEquals(dbManager.getGroupPrefix("Group1"), "testPref");
        });
        assertThrows(IllegalArgumentException.class, () -> dbManager.getGroupLevel(""));
        assertThrows(IllegalArgumentException.class, () -> dbManager.getGroupLevel("Group2"));
    }

    @AfterAll
    void closeConnection() {
        assertDoesNotThrow(() -> connection.close());
    }
}