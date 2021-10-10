package com.bp3x.raidbot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.time.Instant;

/**
 * Custom embed builder used to relay information in an embed
 */
public class RaidBotEmbedBuilder extends EmbedBuilder {

    private final Logger log = LoggerFactory.getLogger(RaidBotEmbedBuilder.class);

    private static final Color raidBotColor = new Color(255, 0, 0);

    public RaidBotEmbedBuilder() {
        this.setColor(raidBotColor);
        this.setFooter("Bp3x RaidBot");
        this.setTimestamp(Instant.now());
    }
}
