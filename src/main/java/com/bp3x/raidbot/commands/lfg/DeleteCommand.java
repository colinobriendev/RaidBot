package com.bp3x.raidbot.commands.lfg;

import com.bp3x.raidbot.commands.lfg.util.Event;
import com.bp3x.raidbot.util.MessageUtils;
import com.bp3x.raidbot.util.RaidBotRuntimeException;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Event eventToDelete = null;
        Message messageToDelete = null;

        if (args.length == 1) {
            eventToDelete = Event.getEventById(args[0]);
            if (eventToDelete == null) {
                MessageUtils.sendAutoDeletedMessage("Unable to find event message to delete with provided ID, please recheck and try again.", 300, commandEvent);
                return;
            }

            messageToDelete = Event.findMessageFromEvent(eventToDelete);

            try {
                Event.removeEvent(messageToDelete);
            } catch (ErrorResponseException ere) {
                log.error("Error response exception thrown when deleting message", ere);
                MessageUtils.sendAutoDeletedMessage("The message that you tried to delete may have already been deleted, contact an admin", 300, commandEvent);
            } catch (RaidBotRuntimeException e) {
                log.error("Could not save events to JSON backup!", e);
            }

        } else {
           MessageUtils.sendAutoDeletedMessage("Invalid arguments. Use `!delete <eventID>`", 300, commandEvent);
        }
        if (eventToDelete != null && messageToDelete != null) {
            ThreadChannel thread = messageToDelete.getStartedThread();
            if (thread != null)
                thread.delete().queue();
            else
                log.error("Thread was null!");
            messageToDelete.delete().queue();
            MessageUtils.sendAutoDeletedMessage("Deleted event with ID: " + eventToDelete.getEventId() + ". Please manually delete the thread. We're working on a fix to automatically delete it.", 300, commandEvent);
        }
    }
}
