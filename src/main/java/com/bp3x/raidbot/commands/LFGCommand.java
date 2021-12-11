package com.bp3x.raidbot.commands;

import com.bp3x.raidbot.RaidBot;
import com.bp3x.raidbot.commands.util.Event;
import com.bp3x.raidbot.commands.util.LFGEmbedBuilder;
import com.bp3x.raidbot.util.RaidBotRuntimeException;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * LFG Command to schedule events
 */
public class LFGCommand extends Command {
    private final Logger log = LoggerFactory.getLogger(LFGCommand.class);

    // emoji constants for reaction "buttons"
    private static final String ACCEPTED_EMOJI = "✅";
    private static final String TENTATIVE_EMOJI = "❓";
    private static final String DECLINED_EMOJI = "❌";

    private static final String TIMESTAMP_PATTERN = "MM/dd/yy hh:mma";

    private static final HashMap<Event, Message> plannedEventsList = new HashMap<>();

    public LFGCommand() {
        this.name = "lfg";
        this.help = "Use to schedule a event";
        this.arguments = "<shortName> <date: MM/dd/yy> <time: hh:mma>";
        this.guildOnly = false;
        this.ownerCommand = false;
        this.category = new Category("General");
    }

    public HashMap<Event, Message> getPlannedEventsList() { return plannedEventsList; }

    @Override
    protected void execute(CommandEvent event) {
        log.info("LFG command by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator());
        log.info("Wants to schedule " + event.getArgs());

        // get and verify the user's timezone role for conversion
        ArrayList<Role> timezoneRoles = RaidBot.getConfig().getTimezoneRoles();
        Role userTimezoneRole = event.getMember().getRoles().stream().filter(timezoneRoles::contains).findFirst().orElse(null);
        if (userTimezoneRole == null) {
            event.getChannel().sendMessage("You do not have a timezone role assigned. Ask an admin to give you your role.").queue();
            return;
        }

        String[] args = event.getArgs().split("\\s+");
        if (args.length < 3) {
            event.getChannel().sendMessage("Missing activity and/or date+time argument(s)").queue();
            return;
        } else if (args.length > 3) {
            event.getChannel().sendMessage("Too many arguments. Should be in the format of: " + this.arguments).queue();
            return;
        }

        String activityString = args[0];
        String dateTimeString = args[1] + " " + args[2].toUpperCase();

        ZonedDateTime eventDateTime = null;
        try {
            // look up the full name for the zone id (ex: "PST" -> "America/Los_Angeles"
            // this ensures daylight savings is factored in
            String zoneId;
            HashMap<String, String> timezoneIdNameMapping = RaidBot.getConfig().getTimezones();
            zoneId = timezoneIdNameMapping.get(userTimezoneRole.getName());

            // parse timestamp provided by user, using their role to convert from their timezone to GMT
            DateTimeFormatter userTimestampFormatter = DateTimeFormatter
                    .ofPattern(TIMESTAMP_PATTERN)
                    .withZone(ZoneId.of(zoneId));
            eventDateTime = ZonedDateTime.from(userTimestampFormatter.parse(dateTimeString));
            eventDateTime = eventDateTime.withZoneSameInstant(ZoneId.of("GMT"));

            if (eventDateTime.isBefore(ZonedDateTime.now())) {
                event.getChannel().sendMessage("Cannot schedule an event in the past.").queue();
                return;
            }
        } catch (DateTimeParseException e) {
            event.getChannel().sendMessage("Invalid date/time argument.").queue();
            return;
        }

        try {
            if (Event.eventExists(activityString)) {
                Event plannedEvent = new Event(activityString, eventDateTime);
                plannedEvent.setPlayerStatus(event.getMember(), Event.EventPlayerStatus.ACCEPTED);
                LFGEmbedBuilder builder = new LFGEmbedBuilder(plannedEvent);

                Message success = event.getChannel().sendMessage(builder.build()).complete();

                RestAction<Void> reactWhiteCheckMark = success.addReaction(ACCEPTED_EMOJI);
                RestAction<Void> reactQuestion = success.addReaction(TENTATIVE_EMOJI);
                RestAction<Void> reactCross = success.addReaction(DECLINED_EMOJI);

                RestAction.allOf(reactWhiteCheckMark, reactQuestion, reactCross).queue();

                plannedEvent.registerEvent(success);
            } else {
                event.getChannel().sendMessage("That event does not exist").queue();
            }
        } catch (RaidBotRuntimeException e) {
            event.getChannel().sendMessage("Error occurred while creating event.").queue();
        }
    }
}
