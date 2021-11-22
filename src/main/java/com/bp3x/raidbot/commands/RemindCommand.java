package com.bp3x.raidbot.commands;

import com.bp3x.raidbot.commands.util.Event;
import com.bp3x.raidbot.commands.util.LFGEmbedBuilder;
import com.bp3x.raidbot.util.RaidBotEmbedBuilder;
import com.bp3x.raidbot.util.RaidBotRuntimeException;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reminder Command to ping accepted players.
 */
public class RemindCommand extends Command {
    private final Logger log = LoggerFactory.getLogger(RemindCommand.class);



    public RemindCommand() {
        this.name = "remind";
        this.help = "Use to remind accepted players";
        this.arguments = "<eventID> <playerCategory>";
        this.guildOnly = false;
        this.ownerCommand = false;
        this.category = new Category("General");

    }

    @Override
    protected void execute(CommandEvent event) {
        log.info("Reminder command by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator());
        log.info("Reminding for " + event.getArgs());

        String[] args = event.getArgs().split("\\s+");
        if (args.length > 0) {
            try {
                // Retrieve event ID data and then add the names of the users in the accepted list
                Event remindEvent = Event.getEventById(args[0]);
                if (remindEvent != null) {
                    if (Event.eventExists(remindEvent.getShortName())) {
                        StringBuilder ReminderStringBuilder = new StringBuilder();
                        RaidBotEmbedBuilder pingEmbed = new RaidBotEmbedBuilder();
                        for (Member player : remindEvent.getAcceptedPlayers()) {
                            ReminderStringBuilder.append("<@!");
                            ReminderStringBuilder.append(player.getUser().getId());
                            ReminderStringBuilder.append(">\n");
                        }
                        pingEmbed.addField("Players Reminded", ReminderStringBuilder.toString(), true);
                        event.getChannel().sendMessage(pingEmbed.build()).queue();
                    }
                } else {
                    event.getChannel().sendMessage("That event does not exist.").queue();
                }
            } catch (RaidBotRuntimeException e) {
                event.getChannel().sendMessage("Critical failure occurred while creating event, " +
                        "shutting bot down because something is deeply wrong.").queue();
            }
        }


        //event.getChannel().sendMessage(builder.build()).complete();
    }
}
