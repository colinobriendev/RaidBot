package com.bp3x.raidbot.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

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
     * @param message - the message
     * @param delay   - time delay in minutes before deletion
     * @param channel - channel to send to
     */
    public static void sendAutoDeletedMessage(Message message, int delay, MessageChannel channel) {
        channel.sendMessage(message).queue(msg -> autoDeleteMessage(msg, delay));
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

    /**
     * Delete a message 15 seconds after execution
     *
     * @param message - the message to delete
     */
    public static void deleteMessage(Message message) {
        message.delete().queueAfter(15, TimeUnit.SECONDS);
    }
}
