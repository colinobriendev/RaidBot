package com.bp3x.raidbot.commands.lfg;

import com.bp3x.raidbot.commands.lfg.util.Event;
import com.bp3x.raidbot.commands.lfg.util.LFGEmbedBuilder;
import com.bp3x.raidbot.util.RaidBotRuntimeException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class LFGReactionListener extends ListenerAdapter {

    Logger log = LoggerFactory.getLogger(LFGReactionListener.class);

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent addEvent) {
       handleReactionEvent(addEvent);
    }

    /**
     * Method to handle either a MessageReactionAddEvent or MessageReactionRemoveEvent to update the embed then remove the reaction.
     * @param reactionEvent the MessageReaction event needing to be handled
     */
    private void handleReactionEvent(GenericMessageReactionEvent reactionEvent) {
        String reactionEmoji = reactionEvent.getReactionEmote().getName();

        // ignore bot reactions
        if (reactionEvent.getUser().isBot() || !LFGConstants.EMOJI_LIST.contains(reactionEmoji)) return;

        else {
            reactionEvent.getChannel().retrieveMessageById(reactionEvent.getMessageId()).submit()
                    .thenCompose((m) -> rebuildEmbed(m, reactionEmoji, reactionEvent.getMember()).submit())
                    .thenCompose((e) -> reactionEvent.getReaction().removeReaction(reactionEvent.getUser()).submit());
        }

        try {
            Event.saveEventsToJson();
        } catch (RaidBotRuntimeException e) {
            log.error("Could not save events to JSON backup!");
            e.printStackTrace();
        }
    }

    /**
     * Rebuilds the Embed based on the choice processed by handleReactionEvent
     * @param message - the message processed by the bot
     * @param reactionEmoji - the reaction used on the message
     * @param member - the user who did the reaction
     */
    private RestAction<Message> rebuildEmbed(Message message, String reactionEmoji, Member member) {
        HashMap<Message, Event> plannedEventsList = Event.getPlannedEventsList();
        Event event = plannedEventsList.get(message);
        setPlayerStatusEmbed(reactionEmoji, event, member);
        LFGEmbedBuilder embed = new LFGEmbedBuilder(event);
        return message.editMessage(embed.build());
    }

    /**
     * Handles setting user in the appropriate list for the event based on the reaction they chose in the embed
     * @param reactionEmoji - the reaction emoji used
     * @param event - our Event class instance
     * @param member - the user who did the reaction
     */
    private void setPlayerStatusEmbed(String reactionEmoji, Event event, Member member) {
        switch(reactionEmoji) {
            case LFGConstants.ACCEPTED_EMOJI:
                event.setPlayerStatus(member, Event.EventPlayerStatus.ACCEPTED);
                break;
            case LFGConstants.TENTATIVE_EMOJI:
                event.setPlayerStatus(member, Event.EventPlayerStatus.TENTATIVE);
                break;
            case LFGConstants.DECLINED_EMOJI:
                event.setPlayerStatus(member, Event.EventPlayerStatus.DECLINED);
                break;
        }
    }
}
