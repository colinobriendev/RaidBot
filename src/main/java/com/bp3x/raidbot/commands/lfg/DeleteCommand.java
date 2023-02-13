package com.bp3x.raidbot.commands.lfg;

import com.bp3x.raidbot.commands.lfg.util.Event;
import com.bp3x.raidbot.util.MessageUtils;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
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

        if (args.length == 1) {
            final Event eventToDelete = Event.getEventById(args[0]);
            if (eventToDelete != null) {
                final Message messageToDelete = Event.findMessageFromEvent(eventToDelete);
                commandEvent.getChannel().retrieveMessageById(messageToDelete.getId()).queue(latestMessageInstance ->
                {
                    if (latestMessageInstance != null) {
                        ThreadChannel thread = latestMessageInstance.getStartedThread();
                        if (thread != null) {
                            thread.delete().queue();
                            messageToDelete.delete().queue();
                            MessageUtils.sendAutoDeletedMessage("Deleted event with ID: " + eventToDelete.getEventId() + " and its corresponding thread.", 300, commandEvent);
                        } else {
                            messageToDelete.delete().queue();
                            MessageUtils.sendAutoDeletedMessage("Deleted event with ID: " + eventToDelete.getEventId() + ". Please delete the thread manually, there was a problem deleting it.", 300, commandEvent);
                        }
                    } else {
                        MessageUtils.sendAutoDeletedMessage("There was a problem locating the message to delete the thread. Contact a mod.", 300, commandEvent);
                    }
                });
            } else {
                MessageUtils.sendAutoDeletedMessage("Unable to find event message to delete with provided ID, please recheck and try again.", 300, commandEvent);
            }
        } else {
            MessageUtils.sendAutoDeletedMessage("Invalid arguments. Use `!delete <eventID>`", 300, commandEvent);
        }
    }
}