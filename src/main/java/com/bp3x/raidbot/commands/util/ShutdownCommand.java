package com.bp3x.raidbot.commands.util;

import com.bp3x.raidbot.RaidBot;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ShutdownCommand extends Command {
    private final EventWaiter waiter;

    Logger log = LoggerFactory.getLogger(ShutdownCommand.class);

    public ShutdownCommand() {
        this.name = "shutdown";
        this.help = "Terminates RaidBot";
        this.arguments = "";
        this.guildOnly = false;
        this.ownerCommand = true;

        this.waiter = RaidBot.getWaiter();
    }

    @Override
    protected void execute(CommandEvent event) {
        log.info("Shutdown command by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator());
        event.reply("Are you sure you would like to shut down RaidBot? Type `" + RaidBot.getConfig().getPrefix() + "confirm`.");

        // wait for 1 minutes, check for user to confirm shutdown
        waiter.waitForEvent(MessageReceivedEvent.class,
                // Check if same author and if they typed !confirm
                e -> e.getAuthor().equals(event.getAuthor()) && e.getMessage().getContentRaw().equalsIgnoreCase(RaidBot.getConfig().getPrefix().concat("confirm")),
                // Shutdown
                e -> shutdown(event),
                // Waiter times out after one minute
                1, TimeUnit.MINUTES, () -> event.reply("Sorry, you took too long."));
    }

    private void shutdown(CommandEvent event) {
        try {
            log.info("Shutdown CONFIRMED by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator());

            event.reactWarning();
            RaidBot.getJDA().getPresence().setPresence(OnlineStatus.OFFLINE, true);
            Thread.sleep(50);

            // disconnect from websocket, exit program
            RaidBot.getJDA().shutdown();
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
            event.replyError("An error occurred while shutting down. Please wait a few moments and try again.");
        }
    }
}
