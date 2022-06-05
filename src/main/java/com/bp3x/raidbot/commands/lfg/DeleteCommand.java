package com.bp3x.raidbot.commands.lfg;

import com.bp3x.raidbot.commands.lfg.util.Event;
import com.bp3x.raidbot.util.MessageUtils;
import com.bp3x.raidbot.util.RaidBotRuntimeException;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
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
                MessageUtils.sendAutoDeletedMessage(new MessageBuilder().append("Unable to find event message to delete with provided ID, please recheck and try again.").build(), 15, commandEvent.getChannel());
                MessageUtils.deleteMessage(commandEvent.getMessage());
                return;
            }

            messageToDelete = Event.findMessageFromEvent(eventToDelete);

            try {
                Event.removeEvent(messageToDelete);
            } catch (ErrorResponseException ere) {
                log.error("Error response exception thrown when deleting message", ere);
                MessageUtils.sendAutoDeletedMessage(new MessageBuilder().append("The message that you tried to delete may have already been deleted, contact an admin").build(), 15, commandEvent.getChannel());
            } catch (RaidBotRuntimeException e) {
                log.error("Could not save events to JSON backup!", e);
            }

        } else {
            commandEvent.getChannel().sendMessage("Invalid arguments. Use `!delete <eventID>`").queue();
        }
        if (eventToDelete != null && messageToDelete != null) {
            commandEvent.getMessage().delete().queue();
            messageToDelete.delete().queue();
            MessageUtils.sendAutoDeletedMessage(new MessageBuilder().append("Deleted event with ID: ").append(eventToDelete.getEventId()).build(), 15, commandEvent.getChannel());
        }
    }
}
