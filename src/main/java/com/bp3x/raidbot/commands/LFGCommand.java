package com.bp3x.raidbot.commands;

import com.bp3x.raidbot.commands.util.Event;
import com.bp3x.raidbot.commands.util.LFGEmbedBuilder;
import com.bp3x.raidbot.util.RaidBotRuntimeException;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LFG Command to schedule events
 */
public class LFGCommand extends Command {
    private final Logger log = LoggerFactory.getLogger(LFGCommand.class);

    public LFGCommand() {
        this.name = "lfg";
        this.help = "Use to schedule a event";
        this.arguments = "<shortName>";
        this.guildOnly = false;
        this.ownerCommand = false;
        this.category = new Category("General");
    }

    @Override
    protected void execute(CommandEvent event) {
        log.info("LFG command by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator());
        log.info("Wants to schedule " + event.getArgs());

        String[] args = event.getArgs().split("\\s+");
        if (args.length > 0) {
            if (Event.eventExists(args[0])) {
                try {
                    Event plannedEvent = new Event(args[0]);
                    plannedEvent.setPlayerStatus(event.getMember(), Event.EventPlayerStatus.ACCEPTED);

                    LFGEmbedBuilder builder = new LFGEmbedBuilder(plannedEvent);
                    Message success = event.getChannel().sendMessage(builder.build()).complete();

                    RestAction<Void> reactWhiteCheckMark = success.addReaction("✅");
                    RestAction<Void> reactQuestion = success.addReaction("❓");
                    RestAction<Void> reactCross = success.addReaction("❌");

                    RestAction.allOf(reactWhiteCheckMark, reactQuestion, reactCross).queue();

                    plannedEvent.registerEvent(success);
                } catch (RaidBotRuntimeException e) {
                    event.getChannel().sendMessage("Critical failure occurred while creating event, " +
                            "shutting bot down because something is deeply wrong.").queue();
                }
            } else {
                // no such event
            }
        } else {
            // insufficient arguments, send a msg to the user about it
        }
    }
}
