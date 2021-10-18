package com.bp3x.raidbot.commands.util;

import com.bp3x.raidbot.util.RaidBotEmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Custom embed builder used by LFG command to sign up for events
 */
public class LFGEmbedBuilder extends RaidBotEmbedBuilder {
    private final Logger log = LoggerFactory.getLogger(LFGEmbedBuilder.class);

    private static final String PLAYER_COUNT = "Player Count: ";
    private static final String ACCEPTED_PLAYERS = "Accepted Players:";
    private static final String DECLINED_PLAYERS = "Declined Players:";
    private static final String TENTATIVE_PLAYERS = "Tentative Players:";

    public LFGEmbedBuilder(Event plannedEvent) {
        this.setTitle(plannedEvent.getLongName());
        this.setTimestamp(plannedEvent.getTime());

        String playerCountStringBuilder = "(" +
                plannedEvent.getAcceptedPlayers().size() +
                "/" +
                plannedEvent.getPlayerCount() +
                ") accepted + " +
                plannedEvent.getTentativePlayers().size() +
                " tentative";
        this.addField(PLAYER_COUNT, playerCountStringBuilder, false);

        String acceptedPlayersString = constructPlayersList(plannedEvent.getAcceptedPlayers());
        this.addField(ACCEPTED_PLAYERS, acceptedPlayersString, true);

        String tentativePlayersString = constructPlayersList(plannedEvent.getTentativePlayers());
        this.addField(TENTATIVE_PLAYERS, tentativePlayersString, true);

        String declinedPlayersString = constructPlayersList(plannedEvent.getDeclinedPlayers());
        this.addField(DECLINED_PLAYERS, declinedPlayersString, true);
    }

    private String constructPlayersList(ArrayList<Member> playerList) {
        if (playerList.isEmpty()) {
            return ("[N/A]");
        }

        StringBuilder playerListBuilder = new StringBuilder();
        for (Member member : playerList) {
            playerListBuilder.append(member.getEffectiveName());
            playerListBuilder.append("\n");
        }

        return playerListBuilder.toString();
    }
}
