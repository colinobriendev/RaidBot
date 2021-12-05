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
        this.setTimestamp(plannedEvent.getTime());

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
        this.addField(LFGConstants.ACCEPTED_PLAYERS, acceptedPlayersString, true);

        String tentativePlayersString = constructPlayersList(plannedEvent.getTentativePlayers());
        this.addField(LFGConstants.TENTATIVE_PLAYERS, tentativePlayersString, true);

        String declinedPlayersString = constructPlayersList(plannedEvent.getDeclinedPlayers());
        this.addField(LFGConstants.DECLINED_PLAYERS, declinedPlayersString, true);
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
