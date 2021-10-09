package com.bp3x.raidbot.commands;

import com.bp3x.raidbot.commands.util.Event;
import com.bp3x.raidbot.commands.util.LFGEmbedBuilder;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * LFG Command to schedule events
 */
public class LFGCommand extends Command {
    private final Logger log = LoggerFactory.getLogger(LFGCommand.class);

    private static HashMap<Event, Message> plannedEventsList = new HashMap<>();

    public LFGCommand() {
        this.name = "lfg";
        this.help = "Use to schedule a event";
        this.arguments = "<shortName>";
        this.guildOnly = false;
        this.ownerCommand = false;
        this.category = new Category("General");
    }

    public HashMap<Event, Message> getPlannedEventsList() { return plannedEventsList; }

    @Override
    protected void execute(CommandEvent event) {
        log.info("LFG command by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator());
        log.info("Wants to schedule " + event.getArgs());

        String[] args = event.getArgs().split("\\s+");
        if (args.length > 0) {
            if (Event.eventExists(args[0])) {
                Event plannedEvent = new Event(args[0]);

                LFGEmbedBuilder builder = new LFGEmbedBuilder(plannedEvent);
                Message success = event.getChannel().sendMessage(builder.build()).complete();

                plannedEventsList.put(plannedEvent, success);
            } else {
                // no such event
            }
        } else {
            // insufficient arguments, send a msg to the user about it
        }
    }
}
