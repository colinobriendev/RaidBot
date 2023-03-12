package com.bp3x.raidbot;

import com.bp3x.raidbot.commands.lfg.*;
import com.bp3x.raidbot.commands.lfg.util.Event;
import com.bp3x.raidbot.commands.util.ShutdownCommand;
import com.bp3x.raidbot.util.Config;
import com.bp3x.raidbot.util.RaidBotGuildUtils;
import com.bp3x.raidbot.util.RaidBotRuntimeException;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RaidBot extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(RaidBot.class);

    private static final EventWaiter waiter = new EventWaiter();
    private static JDA jda;
    private static Config config;

    // executor service for various scheduled tasks within bot
    public static final ScheduledExecutorService raidBotExecutorService = Executors.newScheduledThreadPool(5);

    public static EventWaiter getWaiter() { return waiter; }
    public static JDA getJDA() {
        return jda;
    }

    public static Config getConfig() { return config; }

    public static ScheduledExecutorService getRaidBotExecutorService() {
        return raidBotExecutorService;
    }

    /**
     * Handle coOwner id's which need to be separate Strings
     * @param builder - our CommandClientBuilder instance
     * @param config - local Config file containing
     */
    private static void handleCoOwnerIDs(CommandClientBuilder builder, Config config) {
        String[] coOwners = config.getCoOwnerIDs();
        if (coOwners.length == 2) {
            String coOwner1 = coOwners[0];
            String coOwner2 = coOwners[1];
            builder.setCoOwnerIds(coOwner1, coOwner2);
        } else {
            log.error("Problem initializing coOwner ID's!");
        }
    }

    public static void main(String[] args) throws RaidBotRuntimeException {
        log.info("Preparing to start RaidBot in JDA 5");

        final String LOADING = "Loading...";

        config = new Config();
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
                new RemindCommand(),
                new DeleteCommand(),
                new ShutdownCommand()
        );
        
        client.addSlashCommands(
                new LFGCommand()
        );

        // add client and waiter
        jda = JDABuilder.createDefault(config.getToken())
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.playing(LOADING))

                // explicit enabling required for 5.0
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)

                // add client and waiter
                .addEventListeners(
                        waiter,
                        client.build(),
                        new RaidBot())

                // add our event listeners
                .addEventListeners(new LFGReactionListener())

                .build();
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        /*
         * Ensure timezone roles exist or are created as needed
         * This cannot be done on config load, as it occurs before JDA init,
         * and role creation requires access to Guild data
         */
        Set<String> timezones = config.getTimezones().keySet();
        ArrayList<Role> timezoneRoles = new ArrayList<>();
        for (String id : timezones) {
            Guild guild = jda.getGuilds().get(0);
            Role newRole = RaidBotGuildUtils.tryCreateRole(guild, id);
            timezoneRoles.add(newRole);
        }
        config.setTimezoneRoles(timezoneRoles);

        try {
            Event.loadEventsFromJson();
        } catch (RaidBotRuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onShutdown(@Nonnull ShutdownEvent event) {
        try {
            Event.saveEventsToJson();
        } catch (RaidBotRuntimeException e) {
            e.printStackTrace();
        }
    }
}