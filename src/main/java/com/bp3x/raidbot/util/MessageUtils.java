package com.bp3x.raidbot.util;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.concurrent.TimeUnit;

/**
 * Misc utils for messages
 */
public class MessageUtils {

    private MessageUtils() {
    }

    /**
     * Send a message and automatically delete it after provided time
     *
     * @param receivedMessage - the message
     * @param delay           - time delay in seconds before deletion
     * @param channel         - the channel to delete from
     */
    public static void sendAutoDeletedMessage(String receivedMessage, int delay, MessageChannel channel) {
        MessageCreateBuilder builder = new MessageCreateBuilder();
        MessageCreateData returnMessage = builder.setContent(receivedMessage).build();
        channel.sendMessage(returnMessage).queue(msg -> autoDeleteMessage(msg, delay));
    }

    /**
     * Use this function to delete both the target message and the commandEvent that spawned the message.
     * Ex: good for bot error responses that will also require cleanup of the original command.
     *
     * @param receivedMessage - the message
     * @param delay - time delay in seconds before deletion
     * @param commandEvent - the command event whose start message we wish to delete
     */
    public static void sendAutoDeletedMessage(String receivedMessage, int delay, CommandEvent commandEvent) {
        sendAutoDeletedMessage(receivedMessage, delay, commandEvent.getChannel());
        autoDeleteMessage(commandEvent.getMessage(), delay);
    }

    /**
     * Method to queue deleting a message after the provided delay time.
     *
     * @param message - message to delay
     * @param delay   - time in seconds for delay
     */
    public static void autoDeleteMessage(Message message, int delay) {
        message.delete().queueAfter(delay, TimeUnit.SECONDS);
    }
}
