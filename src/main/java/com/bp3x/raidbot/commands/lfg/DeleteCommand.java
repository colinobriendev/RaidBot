package com.bp3x.raidbot.commands.lfg;

import com.bp3x.raidbot.commands.lfg.util.Event;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Enable deleting of an LFG embed with event id.
 */
public class DeleteCommand extends SlashCommand {

    private final Logger log = LoggerFactory.getLogger(DeleteCommand.class);

    public DeleteCommand() {
        this.name = "delete";
        this.help = "\nUse to delete an event by providing the event ID.\n\nExample:\n`/delete 5241`";
        this.guildOnly = true;
        this.ownerCommand = false;
        
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "event-id", "The ID # of the event you want to delete.", true));
    }

    @Override
    protected void execute(SlashCommandEvent commandEvent) {
        commandEvent.deferReply(true).queue(hook -> {
            var eventId = commandEvent.optString("event-id");
            
            final Event eventToDelete = Event.getEventById(eventId);
            if (eventToDelete != null) {
                final Message messageToDelete = Event.findMessageFromEvent(eventToDelete);
                commandEvent.getChannel().retrieveMessageById(messageToDelete.getId()).queue(latestMessageInstance -> {
                     if (latestMessageInstance != null) {
                         ThreadChannel thread = latestMessageInstance.getStartedThread();
                         if (thread != null) {
                             thread.delete().queue();
                             messageToDelete.delete().queue();
                             hook.editOriginal("Deleted event with ID: " + eventToDelete.getEventId() + " and its corresponding thread.").queue();
                         } else {
                             messageToDelete.delete().queue();
                             hook.editOriginal("Deleted event with ID: " + eventToDelete.getEventId() + ". Please delete the thread manually, there was a problem deleting it.").queue();
                         }
                     } else {
                         hook.editOriginal("There was a problem locating the message to delete the thread. Contact an admin.").queue();
                     }
                 });
            } else {
                hook.editOriginal("Unable to find event message to delete with provided ID, please recheck and try again.").queue();
            }
        });
    }
}