package com.bp3x.raidbot.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RaidBotGuildUtils {

    private static final Logger log = LoggerFactory.getLogger(RaidBotGuildUtils.class);

    // utility class, no constructor needed
    private RaidBotGuildUtils() { }

    /**
     * Creates a role with empty permissions in a guild, with a given name.
     * @param guild - The guild where the role is to be created
     * @param name - The name of the role
     * @return - The Role that was created. If a role with the name currently exists, returns that role.
     */
    public static Role tryCreateRole(Guild guild, String name) {
        if (name.isEmpty()) return null;

        List<Role> existingRolesWithName = guild.getRolesByName(name, true);
        if (!existingRolesWithName.isEmpty()) {
            log.info("Role with name " + name + " already exists, skipping creation.");
            return existingRolesWithName.get(0);
        }

        Role result = null;
        try {
            result = guild.createRole()
                    .setName(name)
                    .complete();

            if (result != null) {
                log.info("Successfully created new role " + name);
            }
        } catch (InsufficientPermissionException ex) {
            log.error("Could not create new role " + name + ", insufficient permissions. " +
                    "Some features will probably not function!");
        }
        return result;
    }
}
