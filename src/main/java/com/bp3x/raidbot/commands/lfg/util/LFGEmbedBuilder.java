package com.bp3x.raidbot.commands.lfg.util;

import com.bp3x.raidbot.commands.lfg.LFGConstants;
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

    public LFGEmbedBuilder(Event plannedEvent) {
        this.setTitle(plannedEvent.getLongName());

        String timestamp = "<t:" +
                plannedEvent.getTime().toEpochSecond() +
                ":F>";
        this.addField("Time", timestamp, false);

        String playerCountStringBuilder = "(" +
                plannedEvent.getAcceptedPlayers().size() +
                "/" +
                plannedEvent.getPlayerCount() +
                ") accepted + " +
                plannedEvent.getTentativePlayers().size() +
                " tentative";
        this.addField(LFGConstants.PLAYER_COUNT, playerCountStringBuilder, false);

        this.addField(LFGConstants.EVENT_ID, plannedEvent.getEventId(), false);

        String acceptedPlayersString = constructPlayersList(plannedEvent.getAcceptedPlayers());
        this.addField(LFGConstants.ACCEPTED_PLAYERS, acceptedPlayersString, false);
        this.addField("", "", false);
        String tentativePlayersString = constructPlayersList(plannedEvent.getTentativePlayers());
        this.addField(LFGConstants.TENTATIVE_PLAYERS, tentativePlayersString, false);
        this.addField("","",false);

        String declinedPlayersString = constructPlayersList(plannedEvent.getDeclinedPlayers());
        this.addField(LFGConstants.DECLINED_PLAYERS, declinedPlayersString, false);
    }

    /**
     * Generate a player list for the embed. This method ensures each player name is on a new line.
     * @param playerList - The list of players that have voted for a particular option as Member objects
     * @return - A string for the embed that shows each player's name on a new line
     */
    private String constructPlayersList(ArrayList<Member> playerList) {
        if (playerList.isEmpty()) {
            if (log.isDebugEnabled()) log.debug("Player list is empty");
            return (LFGConstants.BLANK_PLAYER_LIST);
        }

        StringBuilder playerListBuilder = new StringBuilder();
        for (Member member : playerList) {
            playerListBuilder.append(member.getEffectiveName());
            playerListBuilder.append("\n");
        }

        return playerListBuilder.toString();
    }
}
