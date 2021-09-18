package com.ap3xtbh.raidbot;

import com.ap3xtbh.raidbot.commands.LFGCommand;
import com.ap3xtbh.raidbot.util.Config;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class RaidBot {
    private static final Logger log = LoggerFactory.getLogger(RaidBot.class);

    private static final EventWaiter waiter = new EventWaiter();
    private static JDA jda;
    private static Config config;

    public static void main(String[] args) throws LoginException {
        log.info("Preparing to start RaidBot");

        config = new Config();
        config.load();

        CommandClientBuilder client = new CommandClientBuilder();

        // Default setup, type !help
        client.useDefaultGame();

        client.setOwnerId(config.getOwnerID());
        client.setCoOwnerIds(config.getCoOwnerID());
        client.setPrefix(config.getPrefix());
        log.info("Prefix is set to " + config.getPrefix());

        //add commands
        client.addCommands(
                new LFGCommand()
        );

        jda = JDABuilder.createDefault(config.getToken())
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.playing("Loading..."))

                .addEventListeners(waiter, client.build())

                .build();

    }
}
