package com.bp3x.raidbot.commands.lfg;

import com.bp3x.raidbot.commands.lfg.util.Event;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reminder Command to ping selected players.
 */
public class RemindCommand extends Command {
    private final Logger log = LoggerFactory.getLogger(RemindCommand.class);
    public RemindCommand() {
        this.name = "remind";
        this.help = "Use to remind selected players";
        this.arguments = "<playerCategory> <eventID>";
        this.guildOnly = false;
        this.ownerCommand = false;
        this.category = new Category("General");
    }

    @Override
    protected void execute(CommandEvent event) {
        log.info("Reminder command by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator());
        log.info("Reminding for " + event.getArgs());

        String[] args = event.getArgs().split("\\s+");
        if (args.length == 2) {
            // Retrieve event ID data and then add the names of the users in the accepted list
            Event remindEvent = Event.getEventById(args[1]);
            StringBuilder reminderStringBuilder = new StringBuilder();

            if (remindEvent != null) {
                if (args[0].equalsIgnoreCase("accepted")) {
                    for (Member player : remindEvent.getAcceptedPlayers()) {
                        reminderStringBuilder.append("<@!");
                        reminderStringBuilder.append(player.getUser().getId());
                        reminderStringBuilder.append(">\n");
                        reminderStringBuilder.append(LFGConstants.ACCEPTED_EMOJI);
                        reminderStringBuilder.append("\n");
                    }
                } else if (args[0].equalsIgnoreCase("tentative")) {
                    for (Member player : remindEvent.getTentativePlayers()) {
                        reminderStringBuilder.append("<@!");
                        reminderStringBuilder.append(player.getUser().getId());
                        reminderStringBuilder.append(">");
                        reminderStringBuilder.append(LFGConstants.TENTATIVE_EMOJI);
                        reminderStringBuilder.append("\n");
                    }
                    event.getChannel().sendMessage("Players reminded:\n" + reminderStringBuilder).queue();
                } else {
                    event.getChannel().sendMessage("Usage: !remind " + remindEvent.getEventId() + " accepted or tentative").queue();
                }
            } else {
                event.getChannel().sendMessage("That event does not exist.").queue();
            }
        }
    }
}
