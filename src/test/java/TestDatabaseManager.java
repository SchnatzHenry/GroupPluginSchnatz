import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestDatabaseManager {
    Connection connection;
    DatabaseManager dbManager;


    /**
     * Tests the constructor on an empty database
     */
    @Test
    @DisplayName("Constructor on empty database")
    void testConstructor1() {
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        //deleting all existing databases
        assertDoesNotThrow(() -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("DROP DATABASE IF EXISTS servergrouptest")) {
                preparedStatement.executeUpdate();
            }
        });
        assertThrows(SQLException.class, () -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
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
                assertTrue(!resultSet.isClosed());
                assertTrue(resultSet.next());
                assertTrue(resultSet.getString(1).equals(defaultGroupName));
                assertTrue(resultSet.getString(2).equals(defaultGroupPrefix));

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
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
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
                assertTrue(!resultSet.isClosed());
                assertTrue(resultSet.next());
                assertTrue(resultSet.getString(1).equals(defaultGroupName));
                assertTrue(resultSet.getString(2).equals(defaultGroupPrefix));

            }
        });
    }

    /**
     * Tests the {@link DatabaseManager#createGroup(String, String, int, int)} method with a new group
     */
    @Test
    @DisplayName("createGroup() - new group")
    void testCreateGroup1() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
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
                assertTrue(!resultSet.isClosed());
                assertTrue(resultSet.next());
                assertTrue(resultSet.getString(1).equals("Group1"));
                assertTrue(resultSet.getString(2).equals("Pref1"));
                assertTrue(resultSet.getInt(3) == 1);
                assertTrue(resultSet.getInt(4) == 15);
            }
        });
    }

    /**
     * Tests the {@link DatabaseManager#createGroup(String, String, int, int)} method with a new group
     */
    @Test
    @DisplayName("createGroup() - existing group")
    void testCreateGroup2() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.createGroup("Group1", "Pref1", 1, 15));
    }

    /**
     * Tests the {@link DatabaseManager#createGroup(String, String, int, int)} method with empty group name
     */
    @Test
    @DisplayName("createGroup() - empty group name")
    void testCreateGroup3() {
        testCreateGroup1();
        assertThrows(IllegalArgumentException.class, () -> dbManager.createGroup("", "Pref1", 1, 15));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupName(String, String)} method with an existing group
     */
    @Test
    @DisplayName("editGroupName() - existing group")
    void testEditGroupName1() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        testCreateGroup1();
        assertDoesNotThrow(() -> dbManager.editGroupName("Group1", "Group1Renamed"));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupName(String, String)} method with a non-existing group
     */
    @Test
    @DisplayName("editGroupName() - non-existing group")
    void testEditGroupName2() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupName("GroupNonExistent", "newName"));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupName(String, String)} method with empty String parameters
     */
    @Test
    @DisplayName("editGroupName() - empty Strings")
    void testEditGroupName3() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupName("", "newName"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupName("", ""));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupName("oldName", ""));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupName(String, String)} method with the default group
     */
    @Test
    @DisplayName("editGroupName() - default group")
    void testEditGroupName4() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupName("DefaultGroup", "newName"));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupPrefix(String, String)} method with an existing group
     */
    @Test
    @DisplayName("editGroupPrefix() - existing group")
    void testEditGroupPrefix1() {
        testCreateGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertDoesNotThrow(() -> dbManager.editGroupPrefix("Group1", "newPrefix"));
        assertDoesNotThrow(() -> {
            Field tableGroupsField = dbManager.getClass().getDeclaredField("TABLE_GROUPS");
            tableGroupsField.setAccessible(true);
            String tableGroups = (String) tableGroupsField.get(dbManager);
            try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT groupprefix FROM " + tableGroups + " WHERE groupname = ?")){
                preparedStatement.setString(1, "Group1");
                ResultSet res = preparedStatement.executeQuery();
                assertTrue(res.next());
                assertTrue(res.getString(1).equals("newPrefix"));
            }
        });
    }

    /**
     * Tests the {@link DatabaseManager#editGroupPrefix(String, String)} method with a non-existing group
     */
    @Test
    @DisplayName("editGroupPrefix() - non-existing group")
    void testEditGroupPrefix2() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupPrefix("GroupNonExistent", "newPrefix"));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupPrefix(String, String)} method with an empty groupname
     */
    @Test
    @DisplayName("editGroupPrefix() - empty groupname")
    void testEditGroupPrefix3() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupPrefix("", "newPrefix"));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupColorCode(String, int)} with an existing group
     */
    @Test
    @DisplayName("editGroupColorCode() - existing group")
    void testEditGroupColorCode1() {
        testCreateGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertDoesNotThrow(() -> dbManager.editGroupColorCode("Group1", -1));
        assertDoesNotThrow(() -> {
            Field tableGroupsField = dbManager.getClass().getDeclaredField("TABLE_GROUPS");
            tableGroupsField.setAccessible(true);
            String tableGroups = (String) tableGroupsField.get(dbManager);
            try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT groupcolorcode FROM " + tableGroups + " WHERE groupname = ?")){
                preparedStatement.setString(1, "Group1");
                ResultSet res = preparedStatement.executeQuery();
                assertTrue(res.next());
                assertTrue(res.getInt(1) == -1);
            }
        });
    }

    /**
     * Tests the {@link DatabaseManager#editGroupColorCode(String, int)} with a non-existing group
     */
    @Test
    @DisplayName("editGroupColorCode() - non-existing group")
    void testEditGroupColorCode2() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupColorCode("GroupNonExistent", -1));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupColorCode(String, int)} method with an empty groupname
     */
    @Test
    @DisplayName("editGroupColorCode() - empty groupname")
    void testEditGroupColorCode3() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupColorCode("", -1));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupLevel(String, int)} with an existing group
     */
    @Test
    @DisplayName("editGroupLevel() - existing group")
    void testEditGroupLevel1() {
        testCreateGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertDoesNotThrow(() -> dbManager.editGroupLevel("Group1", -1));
        assertDoesNotThrow(() -> {
            Field tableGroupsField = dbManager.getClass().getDeclaredField("TABLE_GROUPS");
            tableGroupsField.setAccessible(true);
            String tableGroups = (String) tableGroupsField.get(dbManager);
            try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT grouplevel FROM " + tableGroups + " WHERE groupname = ?")){
                preparedStatement.setString(1, "Group1");
                ResultSet res = preparedStatement.executeQuery();
                assertTrue(res.next());
                assertTrue(res.getInt(1) == -1);
            }
        });
    }

    /**
     * Tests the {@link DatabaseManager#editGroupLevel(String, int)} with a non-existing group
     */
    @Test
    @DisplayName("editGroupLevel() - non-existing group")
    void testEditGroupLevel2() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupLevel("GroupNonExistent", -1));
    }

    /**
     * Tests the {@link DatabaseManager#editGroupLevel(String, int)} method with an empty groupname
     */
    @Test
    @DisplayName("editGroupLevel() - empty groupname")
    void testEditGroupLevel3() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.editGroupLevel("", -1));
    }

    /**
     * Tests the {@link DatabaseManager#getGroupsUsers(String)} method with an existing group
     */
    @Test
    @DisplayName("getGroupsUsers() - existing group")
    void testGetGroupsUsers1() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
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
        assertTrue(actualUsersGroup1.size() == 3);
        expectedUsersGroup1.forEach(u -> assertTrue(actualUsersGroup1.contains(u)));
        assertTrue(actualUsersGroup2.size() == 1);
        expectedUsersGroup2.forEach(u -> assertTrue(actualUsersGroup2.contains(u)));
        assertTrue(actualUsersGroup3.isEmpty());
    }

    /**
     * Tests the {@link DatabaseManager#getGroupsUsers(String)} method with a non-existing group
     */
    @Test
    @DisplayName("getGroupsUsers() - non-existing group")
    void testGetGroupsUsers2() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.getGroupsUsers("GroupNonExistent"));
    }

    /**
     * Tests the {@link DatabaseManager#getGroupsUsers(String)} method with an empty groupname
     */
    @Test
    @DisplayName("getGroupsUsers() - empty groupname")
    void testGetGroupsUsers3() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.getGroupsUsers(""));
    }

    /**
     * Tests the {@link DatabaseManager#addUserToGroup(String, String)} method with an existing group
     */
    @Test
    @DisplayName("addUserToGroup() - unlimited - existing group")
    void testAddUserToGroup1() {
        testCreateGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
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
               assertTrue(res.getObject(2) == null);
               assertTrue(res.next());
               assertTrue(remainingUsers.contains(res.getString(1)));
               remainingUsers.remove(res.getString(1));
               assertTrue(res.getObject(2) == null);
               assertTrue(res.next());
               assertTrue(remainingUsers.contains(res.getString(1)));
               remainingUsers.remove(res.getString(1));
               assertTrue(res.getObject(2) == null);
               assertFalse(res.next());
           }
        });
    }

    /**
     * Tests the {@link DatabaseManager#addUserToGroup(String, String)} method with a non-existing group
     */
    @Test
    @DisplayName("addUserToGroup() - unlimited - non-existing group")
    void testAddUserToGroup2() {
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid1", "GroupNonExistent"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid2", "GroupNonExistent"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid3", "GroupNonExistent"));
    }

    /**
     * Tests the {@link DatabaseManager#addUserToGroup(String, String)} method with an empty groupname and/or uuid
     */
    @Test
    @DisplayName("addUserToGroup() - unlimited - empty groupname and/or uuid")
    void testAddUserToGroup3() {
        testCreateGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("", "Group1"));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("useruuid1", ""));
        assertThrows(IllegalArgumentException.class, () -> dbManager.addUserToGroup("", ""));
    }

    /**
     * Tests the {@link DatabaseManager#addUserToGroup(String, String, int, int, int)} method with an existing group
     */
    @Test
    @DisplayName("addUserToGroup() - limited - existing group")
    void testAddUserToGroup4() {
        //TODO
        assertTrue(false);
    }

    /**
     * Tests the {@link DatabaseManager#addUserToGroup(String, String, int, int, int)} method with a non-existing group
     */
    @Test
    @DisplayName("addUserToGroup() - limited - non-existing group")
    void testAddUserToGroup5() {
        //TODO
        assertTrue(false);
    }

    /**
     * Tests the {@link DatabaseManager#addUserToGroup(String, String, int, int, int)} method with an empty groupname and/or uuid
     */
    @Test
    @DisplayName("addUserToGroup() - limited - empty groupname and/or uuidp")
    void testAddUserToGroup6() {
        //TODO
        assertTrue(false);
    }

    /**
     * Tests the {@link DatabaseManager#removeUserFromGroup(String, String)} method
     */
    @Test
    @DisplayName("removeUserFromGroup() - general")
    void testRemoveUserFromGroup1() {
        testAddUserToGroup1();
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
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
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
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
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
        assertDoesNotThrow(() -> {
            List<String> actualUser4 = dbManager.getGroups("useruuid4");
            assertTrue(actualUser4.size() == 1);
            assertTrue(actualUser4.contains("Group1"));
            dbManager.createGroup("Group2", "g2pref", 0 , 0);
            dbManager.addUserToGroup("useruuid4", "Group2");
            actualUser4 = dbManager.getGroups("useruuid4");
            assertTrue(actualUser4.size() == 2);
            assertTrue(actualUser4.contains("Group1"));
            assertTrue(actualUser4.contains("Group2"));
            dbManager.removeUserFromGroup("useruuid4", "Group1");
            actualUser4 = dbManager.getGroups("useruuid4");
            assertTrue(actualUser4.size() == 1);
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
        dbManager = assertDoesNotThrow(() -> new DatabaseManager("localhost", 3306, "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4", "ServerGroupTest", "DefaultGroup", "defPrefix", 0, 3));
        connection = assertDoesNotThrow(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerGroupTest", "root", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4"));
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

    @AfterAll
    void closeConnection() {
        assertDoesNotThrow(() -> connection.close());
    }
}
