package com.bp3x.raidbot.commands.lfg;

import com.bp3x.raidbot.commands.lfg.util.Event;
import com.bp3x.raidbot.util.RaidBotRuntimeException;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Enable deleting of an LFG embed with event id.
 */
public class DeleteCommand extends Command {

    private final Logger log = LoggerFactory.getLogger(DeleteCommand.class);

    private static final String DELETE_HELP_EXAMPLE = "\nUse to Delete an event by providing the event ID.\n" + "\nExample:\n" +
            "`!delete 5241`";

    public DeleteCommand() {
        this.name = "delete";
        this.help = DELETE_HELP_EXAMPLE;
        this.arguments = "<eventID>>";
        this.guildOnly = false;
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {

        String[] args = commandEvent.getArgs().split("\\s+");

        if (args.length == 1) {
            Event eventToDelete = Event.getEventById(args[0]);
            HashMap<Message, Event> eventsList = Event.getPlannedEventsList();
            Message messageToDelete = null;

            // work backwards to find key from value
            for (Map.Entry<Message, Event> entry : eventsList.entrySet()) {
                if (entry.getValue().equals(eventToDelete)) {
                    messageToDelete = entry.getKey();
                }
            }

            if (messageToDelete != null)
            {
                commandEvent.getChannel().deleteMessageById(messageToDelete.getId()).queue();
                eventsList.remove(messageToDelete);
                try {
                    Event.saveEventsToJson();
                } catch (RaidBotRuntimeException e) {
                    log.error("Could not save events to JSON backup!", e);
                }
                commandEvent.getChannel().sendMessage("Deleted event with ID " + eventToDelete.getEventId()).queue();
            }
            else {
                commandEvent.getChannel().sendMessage("Unable to find message ID that matches input, please recheck and try again.").queue();
            }
        }
        else {
            commandEvent.getChannel().sendMessage("Invalid arguments. Use `!delete <eventID>`").queue();
        }
    }
}
