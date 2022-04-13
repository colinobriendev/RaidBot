package com.bp3x.raidbot.commands.lfg;

import com.bp3x.raidbot.RaidBot;
import com.bp3x.raidbot.commands.lfg.util.*;
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

    public LFGCommand() {
        this.name = "lfg";
        this.help = "Use to schedule a event";
        this.arguments = "<shortName> <date: MM/dd/yy> <time: hh:mma>";
        this.guildOnly = false;
        this.ownerCommand = false;
        this.category = new Category("General");
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        log.info("LFG command by: " + commandEvent.getAuthor().getName() + "#" + commandEvent.getAuthor().getDiscriminator());
        log.info("Wants to schedule " + commandEvent.getArgs());

        // get and verify the user's timezone role for conversion
        ArrayList<Role> timezoneRoles = RaidBot.getConfig().getTimezoneRoles();
        Role userTimezoneRole = commandEvent.getMember().getRoles().stream().filter(timezoneRoles::contains).findFirst().orElse(null);
        if (userTimezoneRole == null) {
            commandEvent.getChannel().sendMessage("You do not have a timezone role assigned. Ask an admin to give you your role.").queue();
            return;
        }

        String[] args = commandEvent.getArgs().split("\\s+");
        if (args.length < 3) {
            commandEvent.getChannel().sendMessage("Missing activity and/or date+time argument(s)").queue();
            return;
        } else if (args.length > 3) {
            commandEvent.getChannel().sendMessage("Too many arguments. Should be in the format of: " + this.arguments).queue();
            return;
        }

        String activityString = args[0];

        String dateTimeString = formatDateTimeInput(args[1], args[2]);

        ZonedDateTime eventDateTime = null;
        try {
            // look up the full name for the zone id (ex: "PST" -> "America/Los_Angeles"
            // this ensures daylight savings is factored in
            String zoneId;
            HashMap<String, String> timezoneIdNameMapping = RaidBot.getConfig().getTimezones();
            zoneId = timezoneIdNameMapping.get(userTimezoneRole.getName());

            // parse timestamp provided by user, using their role to convert from their timezone to GMT
            DateTimeFormatter userTimestampFormatter = DateTimeFormatter
                    .ofPattern(LFGConstants.TIMESTAMP_PATTERN)
                    .withZone(ZoneId.of(zoneId));
            eventDateTime = ZonedDateTime.from(userTimestampFormatter.parse(dateTimeString));
            eventDateTime = eventDateTime.withZoneSameInstant(ZoneId.of("GMT"));

            if (eventDateTime.isBefore(ZonedDateTime.now())) {
                commandEvent.getChannel().sendMessage("Cannot schedule an event in the past.").queue();
                return;
            }
        } catch (DateTimeParseException e) {
            commandEvent.getChannel().sendMessage("Invalid date/time argument.").queue();
            return;
        }

        try {
            if (Event.eventExists(activityString)) {
                Event plannedEvent = new Event(activityString, eventDateTime);
                plannedEvent.setPlayerStatus(commandEvent.getMember(), Event.EventPlayerStatus.ACCEPTED);
                LFGEmbedBuilder builder = new LFGEmbedBuilder(plannedEvent);

                Message success = commandEvent.getChannel().sendMessage(builder.build()).complete();

                RestAction<Void> reactWhiteCheckMark = success.addReaction(LFGConstants.ACCEPTED_EMOJI);
                RestAction<Void> reactQuestion = success.addReaction(LFGConstants.TENTATIVE_EMOJI);
                RestAction<Void> reactCross = success.addReaction(LFGConstants.DECLINED_EMOJI);

                RestAction.allOf(reactWhiteCheckMark, reactQuestion, reactCross).queue();

                plannedEvent.registerEvent(success);

                Event.scheduleEventDeletion(eventDateTime, commandEvent, success);

            } else {
                commandEvent.getChannel().sendMessage("That event does not exist").queue();
            }
        } catch (RaidBotRuntimeException e) {
            commandEvent.getChannel().sendMessage("Error occurred while creating event.").queue();
        }
    }
    
    /**
     * Add leading zeros to date and time when needed
     */
    String formatDateTimeInput(String date, String time) {
        String result;
        
        StringBuilder dateStringBuilder = new StringBuilder();
        String[] dateSplit = date.split("/");
        for (int i = 0; i < dateSplit.length - 1; i++) {
            String d = dateSplit[i];
            if (Integer.parseInt(d) < 10 && !d.contains("0")) {
                dateStringBuilder.append("0").append(d);
            } else {
                dateStringBuilder.append(d);
            }
            dateStringBuilder.append("/");
        }
        dateStringBuilder.append(dateSplit[2]);

        StringBuilder timeStringBuilder = new StringBuilder();
        String[] timeSplit = time.split(":");
        String hour = timeSplit[0];
        if (Integer.parseInt(hour) < 10 && !hour.contains("0")) {
            timeStringBuilder.append("0")
                    .append(hour)
                    .append(":")
                    .append(timeSplit[1]);
        } else {
            timeStringBuilder = new StringBuilder(time);
        }

        result = (dateStringBuilder + " " + timeStringBuilder).toUpperCase();
        return result;
    }
}
