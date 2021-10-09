package com.bp3x.raidbot.commands.util;

import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;

/**
 * Custom embed builder used by LFG command to sign up for events
 */
public class LFGEmbedBuilder extends EmbedBuilder {
    private final Logger log = LoggerFactory.getLogger(LFGEmbedBuilder.class);

    private static final String PLAYER_COUNT = "Player Count: ";
    private static final String PLAYERS_NEEDED = "Players Needed:";
    private static final String ACCEPTED_PLAYERS = "Accepted Players:";
    private static final String DECLINED_PLAYERS = "Declined Players:";
    private static final String TENTATIVE_PLAYERS = "Tentative Players:";

    public LFGEmbedBuilder(Event plannedEvent) {
        this.setTitle(plannedEvent.getLongName());
        this.setColor(new Color(255, 0, 0));
        this.setFooter("RaidBot");
        this.setTimestamp(Instant.now());

        this.addField(PLAYER_COUNT, String.valueOf(plannedEvent.getAcceptedPlayers().size()), false);
        this.addField(PLAYERS_NEEDED, String.valueOf(plannedEvent.getPlayerCount()), false);
        this.addField(ACCEPTED_PLAYERS, "", false);
        this.addField(DECLINED_PLAYERS, "", false);
        this.addField(TENTATIVE_PLAYERS, "", false);
    }
}
