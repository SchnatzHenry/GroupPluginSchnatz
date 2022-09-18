package com.schnatz.groupplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class is used to build the default config
 * @author Henry Schnatz
 */

public class ConfigBuilder {
    /**
     * The plugin which the default config belongs to
     */
    private final JavaPlugin plugin;
    /**
     * The config that is edited.
     */
    private final FileConfiguration config;

    /**
     * Initializes variable {@link ConfigBuilder#plugin} with the given argument.
     * Initializes variable {@link ConfigBuilder#config} with the plugin's config.
     * And calls the method {@link ConfigBuilder#buildDefaultConfig}.
     *
     * @param plugin the plugin the default config will belong to
     */
    public ConfigBuilder(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        buildDefaultConfig();
    }

    /**
     * Builds and saves the default config.
     */
    public void buildDefaultConfig() {
        config.addDefault("DatabaseIpAddress", "localhost");
        config.addDefault("DatabasePort", 3306);
        config.addDefault("DatabaseUser", "root");
        config.addDefault("DatabasePassword", "Y#rJj1R-vojdE#i:9A:E!w1bt8_^fEP:E01=cN9M~PX2k2mE.z9om>Hz4@^-~uK4");
        config.addDefault("DatabaseName", "ServerGroups");

        config.addDefault("DefaultGroupName", "Player");
        config.addDefault("DefaultGroupPrefix", "P");
        config.addDefault("DefaultGroupLevel", 0);
        config.addDefault("DefaultGroupColorCode", 0);

        config.addDefault("MessageServerJoin", "\u00A77[\u00A7%color%%prefix%\u00A77]\u00A7%color%%name% \u00A7fjoined the Server");
        config.addDefault("MessageServerLeave", "\u00A77[\u00A7%color%%prefix%\u00A77]\u00A7%color%%name% \u00A7fleft the Server");
        config.addDefault("MessageChat", "\u00A77[\u00A7%color%%prefix%\u00A77]\u00A7%color%%name%\u00A7f: %message%");

        config.addDefault("CommandAddUserToGroupInsufficientPermissionMessage", "You have insufficient permission to use this command!");
        config.addDefault("CommandAddUserToGroupUsageMessage", "Please use as following: /addusertogroup <username> <groupname> <duration> (e.g. \"/addusertogroup Hans Admin 5:14:10:12\" will add Hans to group Admin for 5 days, 14 hours, 10 minutes and 12 seconds.");
        config.addDefault("CommandAddUserToGroupSqlErrorMessage", "Something went wrong internally, please try again later!");
        config.addDefault("CommandAddUserToGroupPlayerNotFoundMessage", "Could not find a player named %name%!");
        config.addDefault("CommandAddUserToGroupGroupNameDoesNotExistMessage", "There is no group with the given name!");
        config.addDefault("CommandAddUserToGroupUserAlreadyMemberOfGroupMessage", "The given user is already member of the given group!");
        config.addDefault("CommandAddUserToGroupAddedUserToGroupMessage", "Added %user% to group %group%.");
        config.addDefault("CommandAddUserToGroupNegativeTimeSpecificationMessage", "Time specifications must not be negative!");
        config.addDefault("CommandAddUserToGroupIllegalTimeFormatMessage", "The time must have one of the following formats: days:hours:minutes:seconds/hours:minutes:seconds/minutes:seconds/seconds");
        config.addDefault("CommandAddUserToGroupTimeSpecificationNotANumberMessage", "The given time is not a number!");
        config.addDefault("CommandAddUserToGroupTimeSpecificationNegativeMessage", "Negative time specifications are invalid!");

        config.addDefault("CommandCreateGroupInsufficientPermissionMessage", "You have insufficient permission to use this command!");
        config.addDefault("CommandCreateGroupUsageMessage", "Please use as following: /createGroup <name>(30 characters) [<prefix>(10 characters) <level> <colorcode>(0-9, a-f)]");
        config.addDefault("CommandCreateGroupDefaultGroupLevel", 0);
        config.addDefault("CommandCreateGroupDefaultGroupColorCode", 0);
        config.addDefault("CommandCreateGroupSqlErrorMessage", "Something went wrong internally, please try again later");
        config.addDefault("CommandCreateGroupGroupNameDoesExistMessage", "The given group name does already exist!");
        config.addDefault("CommandCreateGroupGroupNameTooLongMessage", "The given group name is too long (max: 30 characters)!");
        config.addDefault("CommandCreateGroupPrefixTooLongMessage", "The given prefix is too long (max: 10 characters)!");
        config.addDefault("CommandCreateGroupInvalidColorCodeMessage", "The given color code is invalid, please do only use the following 0-9 and a-f!");
        config.addDefault("CommandCreateGroupGroupCreatedMessage", "Group %name% created");
        config.addDefault("CommandCreateGroupLevelMustBeIntegerMessage", "The given level must be a number!");

        config.addDefault("CommandDeleteGroupInsufficientPermissionMessage", "You have insufficient permission to use this command!");
        config.addDefault("CommandDeleteGroupUsageMessage", "Please use as following: /deletegroup <name>");
        config.addDefault("CommandDeleteGroupSqlErrorMessage", "Something went wrong internally, please try again later");
        config.addDefault("CommandDeleteGroupGroupNameDoesNotExistMessage", "The given group name does not exist!");
        config.addDefault("CommandDeleteGroupGroupDeletedMessage", "Group %name% deleted");
        config.addDefault("CommandDeleteGroupDeletingDefaultGroupMessage", "The default group must not be deleted!");

        config.addDefault("CommandEditGroupColorCodeInsufficientPermissionMessage", "You have insufficient permission to use this command!");
        config.addDefault("CommandEditGroupColorCodeUsageMessage", "Please use as following: /editgroupcolorcode <groupname> <newpcolorcode>");
        config.addDefault("CommandEditGroupColorCodeSqlErrorMessage", "Something went wrong internally, please try again later!");
        config.addDefault("CommandEditGroupColorCodeGroupColorCodeEditedMessage", "Changed the color code of group %group% to %newcolorcode%!");
        config.addDefault("CommandEditGroupColorCodeGroupNameDoesNotExistMessage", "There is no group with the given name!");
        config.addDefault("CommandEditGroupColorCodeInvalidColorCodeMessage", "The given color code is invalid, please do only use the following 0-9 and a-f!");

        config.addDefault("CommandEditGroupLevelInsufficientPermissionMessage", "You have insufficient permission to use this command!");
        config.addDefault("CommandEditGroupLevelUsageMessage", "Please use as following: /editgrouplevel <groupname> <newlevel>");
        config.addDefault("CommandEditGroupLevelSqlErrorMessage", "Something went wrong internally, please try again later!");
        config.addDefault("CommandEditGroupLevelGroupLevelEditedMessage", "Changed the level of group %group% to %newlevel%!");
        config.addDefault("CommandEditGroupLevelGroupNameDoesNotExistMessage", "There is no group with the given name!");
        config.addDefault("CommandEditGroupLevelLevelMustBeIntegerMessage", "The given level must be an integer!");

        config.addDefault("CommandEditGroupNameInsufficientPermissionMessage", "You have insufficient permission to use this command!");
        config.addDefault("CommandEditGroupNameUsageMessage", "Please use as following: /editgroupname <oldname> <newname>");
        config.addDefault("CommandEditGroupNameSqlErrorMessage", "Something went wrong internally, please try again later!");
        config.addDefault("CommandEditGroupNameGroupNameEditedMessage", "Changed the groupname of group %oldname% to %newname%");
        config.addDefault("CommandEditGroupNameEditingNameDefaultGroupMessage", "The default group's name must not be edited!");
        config.addDefault("CommandEditGroupNameGroupNameDoesNotExistMessage", "There is no group with the given group name!");
        config.addDefault("CommandEditGroupNameNewGroupNameTooLongMessage", "Group names must not be longer than 30 characters!");
        config.addDefault("CommandEditGroupNameNewGroupNameDoesAlreadyExist", "There is already a group with the given name!");

        config.addDefault("CommandEditGroupPrefixInsufficientPermissionMessage", "You have insufficient permission to use this command!");
        config.addDefault("CommandEditGroupPrefixUsageMessage", "Please use as following: /editgroupprefix <groupname> <newprefix>");
        config.addDefault("CommandEditGroupPrefixSqlErrorMessage", "Something went wrong internally, please try again later!");
        config.addDefault("CommandEditGroupPrefixGroupPrefixEditedMessage", "Changed the prefix of group %group% to %newprefix%!");
        config.addDefault("CommandEditGroupPrefixNewGroupPrefixTooLongMessage", "Prefixes must not be longer than 10 characters!");
        config.addDefault("CommandEditGroupPrefixGroupNameDoesNotExistMessage", "There is no group with the given name!");

        config.addDefault("CommandGetGroupsInsufficientPermissionMessage", "You have insufficient permission to use this command!");
        config.addDefault("CommandGetGroupsUsageMessage", "Please use as following: /getgroups <playername>");
        config.addDefault("CommandGetGroupsPlayerNotFoundMessage", "Could not find a player named %user%");
        config.addDefault("CommandGetGroupsSqlErrorMessage", "Something went wrong internally, please try again later");
        config.addDefault("CommandGetGroupsMemberOfFollowingGroupsMessage", "%user% is member of the following groups:");
        config.addDefault("CommandGetGroupsMemberOfGroupUnknownTimeMessage", "%group%: unknown time left");
        config.addDefault("CommandGetGroupsMemberOfGroupForEverMessage", "%group%");
        config.addDefault("CommandGetGroupsMemberOfGroupForTimesMessage", "%group%: %days% days, %hours% hours, %minutes% minutes and %seconds% seconds left");
        config.addDefault("CommandGetGroupsMemberOfGroupForTimeNoDaysMessage", "%group%: %hours% hours, %minutes% minutes and %seconds% seconds left");
        config.addDefault("CommandGetGroupsMemberOfGroupForTimeNoHoursMessage", "%group%: %minutes% minutes and %seconds% seconds left");
        config.addDefault("CommandGetGroupsMemberOfGroupForTimeNoMinutesMessage", "%group%: %seconds% seconds left");

        config.addDefault("CommandGroupInfoInsufficientPermissionMessage", "You have insufficient permission to use this command!");
        config.addDefault("CommandGroupInfoUsageMessage", "Please use as following: /groupinfo <groupname>");
        config.addDefault("CommandGroupInfoSqlErrorMessage", "Something went wrong internally, please try again later");
        config.addDefault("CommandGroupInfoGeneralGroupInformationMessage", "Group %name% has prefix %prefix%, level %level% and color code %colorcode%. It features %usercount% users:");
        config.addDefault("CommandGroupInfoGeneralGroupInformationNoPrefixMessage", "Group %name% has no prefix, level %level% and color code %colorcode%. It features %usercount% users:");
        config.addDefault("CommandGroupInfoListGroupUsersMessage", "%name%");
        config.addDefault("CommandGroupInfoGroupNameDoesNotExistMessage", "There is no group with the given name!");

        config.addDefault("CommandRemoveUserFromGroupInsufficientPermissionMessage", "You have insufficient permission to use this command!");
        config.addDefault("CommandRemoveUserFromGroupUsageMessage", "Please use as following: /removeuserfromgroup <username> <groupname>");
        config.addDefault("CommandRemoveUserFromGroupSqlErrorMessage", "Something went wrong internally, please try again later!");
        config.addDefault("CommandRemoveUserFromGroupGroupNameDoesNotExistMessage", "There is no group with the given name!");
        config.addDefault("CommandRemoveUserFromGroupPlayerNotFoundMessage", "Could not find a player named %name%!");
        config.addDefault("CommandRemoveUserFromGroupUserNotMemberOfGroupMessage", "The given user is not member of that group!");
        config.addDefault("CommandRemoveUserFromGroupRemovedUserFromGroupMessage", "Removed %user% from group %group%.");

        config.addDefault("CommandSignInsufficientPermissionMessage", "You have insufficient permission to use this command!");
        config.addDefault("CommandSignUsageMessage", "Please use as following: /sign [<playername>]");
        config.addDefault("CommandSignOnlyPlayersCanUseThisCommandMessage", "Only players can use this command!");
        config.addDefault("CommandSignSqlErrorMessage", "Something went wrong internally, please try again later!");
        config.addDefault("CommandSignUserNotFoundMessage", "The given user could not be found!");
        config.addDefault("CommandSignSignMustReplaceAirMessage", "Signs must only replace air!");
        config.addDefault("CommandSignNotMemberOfGroupMessage", "The user is not member of a group!");
        config.addDefault("CommandSignMustStandOnSolidBlockMessage", "You must stand on a solid block to perform this command!");

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }
}