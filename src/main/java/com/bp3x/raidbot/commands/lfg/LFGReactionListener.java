package com.bp3x.raidbot.commands.lfg;

import com.bp3x.raidbot.commands.lfg.util.Event;
import com.bp3x.raidbot.commands.lfg.util.LFGEmbedBuilder;
import com.bp3x.raidbot.util.RaidBotRuntimeException;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;

public class LFGReactionListener extends ListenerAdapter {

    Logger log = LoggerFactory.getLogger(LFGReactionListener.class);

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent addEvent) {
       handleReactionEvent(addEvent);
    }

    /**
     * Method to handle either a MessageReactionAddEvent or MessageReactionRemoveEvent to update the embed then remove the reaction.
     * @param reactionEvent the MessageReaction event needing to be handled
     */
    private void handleReactionEvent(GenericMessageReactionEvent reactionEvent) {

        String reactionEmoji = reactionEvent.getEmoji().getName();

        // ignore bot reactions
        if (reactionEvent.getUser().isBot() || !LFGConstants.EMOJI_LIST.contains(reactionEmoji)) return;

        else {
            Message message = reactionEvent.getChannel().retrieveMessageById(reactionEvent.getMessageId()).complete();
            rebuildEmbed(message, reactionEmoji, reactionEvent.getMember()).submit();
            reactionEvent.getReaction().removeReaction(reactionEvent.getUser()).submit();

            if (LFGConstants.ACCEPTED_EMOJI_STRING.equals(reactionEmoji) || LFGConstants.TENTATIVE_EMOJI_STRING.equals(reactionEmoji)) {
                message.getStartedThread().addThreadMember(reactionEvent.getUser()).queue();
            }

            try {
                Event.saveEventsToJson();
            } catch (RaidBotRuntimeException e) {
                log.error("Could not save events to JSON backup!");
                e.printStackTrace();
            }
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
        MessageEditBuilder editBuilder = new MessageEditBuilder();
        editBuilder.setEmbeds(embed.build());
        // after updating we also need to update the message within the thread context or the thread doesn't display it properly
        message.getStartedThread().editMessageById(message.getStartedThread().getId(), editBuilder.build()).queue();
        return message.editMessage(editBuilder.build());
    }

    /**
     * Handles setting user in the appropriate list for the event based on the reaction they chose in the embed
     * @param reactionEmoji - the reaction emoji used
     * @param event - our Event class instance
     * @param member - the user who did the reaction
     */
    private void setPlayerStatusEmbed(String reactionEmoji, Event event, Member member) {
        switch(reactionEmoji) {
            case LFGConstants.ACCEPTED_EMOJI_STRING:
                event.setPlayerStatus(member, Event.EventPlayerStatus.ACCEPTED);
                break;
            case LFGConstants.TENTATIVE_EMOJI_STRING:
                event.setPlayerStatus(member, Event.EventPlayerStatus.TENTATIVE);
                break;
            case LFGConstants.DECLINED_EMOJI_STRING:
                event.setPlayerStatus(member, Event.EventPlayerStatus.DECLINED);
                break;
            default:
                log.error("We uh shouldn't reach this, but, adding a default because its a good practice");
        }
    }
}
