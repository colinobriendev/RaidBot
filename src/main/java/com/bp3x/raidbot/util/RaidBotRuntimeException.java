package com.bp3x.raidbot.util;

import com.bp3x.raidbot.RaidBot;

/**
 * Exception to report the error related to configuration and shut down the bot.
 */
public class RaidBotRuntimeException extends Exception {

    public RaidBotRuntimeException(String message) {
        super(message);
        // this can be thrown before JDA is initialized
        if (RaidBot.getJDA() != null) {
            RaidBot.getJDA().shutdown();
        } else {
            System.exit(1);
        }
    }
}
