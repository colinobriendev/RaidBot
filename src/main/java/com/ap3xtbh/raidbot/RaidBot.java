package com.ap3xtbh.raidbot;

import com.ap3xtbh.raidbot.commands.LFGCommand;
import com.ap3xtbh.raidbot.util.Config;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class RaidBot {
    private static final Logger log = LoggerFactory.getLogger(RaidBot.class);

    private static final EventWaiter waiter = new EventWaiter();

    public static void main(String[] args) throws LoginException {
        log.info("Preparing to start RaidBot");

        final String LOADING = "Loading...";

        Config config = new Config();
        config.load();

        CommandClientBuilder client = new CommandClientBuilder();

        // Default setup, type !help
        client.useDefaultGame();

        client.setOwnerId(config.getOwnerID());
        handleCoOwnerIDs(client, config);
        client.setPrefix(config.getPrefix());
        log.info("Prefix is set to " + config.getPrefix());

        //add commands
        client.addCommands(
                new LFGCommand()
        );

        // add client and waiter
        JDABuilder.createDefault(config.getToken())
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.playing(LOADING))

                // add client and waiter
                .addEventListeners(waiter, client.build())

                .build();

    }

    /**
     * Handle coOwner id's which need to be separate Strings
     * @param builder - our CommandClientBuilder instance
     * @param config - local Config file containing
     */
    private static void handleCoOwnerIDs(CommandClientBuilder builder, Config config) {
        String[] coOwners = config.getCoOwnerIDs();
        if (coOwners.length == 2)
        {
            String coOwner1 = coOwners[0];
            String coOwner2 = coOwners[1];
            builder.setCoOwnerIds(coOwner1, coOwner2);
        }
        else {
            log.error("Problem initializing coOwner ID's!");
        }
    }
}