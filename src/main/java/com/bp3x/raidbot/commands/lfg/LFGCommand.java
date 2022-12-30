package com.bp3x.raidbot.commands.lfg;

import com.bp3x.raidbot.RaidBot;
import com.bp3x.raidbot.commands.lfg.util.*;
import com.bp3x.raidbot.util.MessageUtils;
import com.bp3x.raidbot.util.RaidBotRuntimeException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
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

    public LFGCommand() throws RaidBotRuntimeException {
        this.name = "lfg";
        this.help = buildHelpMessage();
        this.arguments = "<shortName> <date: MM/dd/yy> <time: hh:mma>";
        this.guildOnly = false;
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        log.info("LFG command by: " + commandEvent.getAuthor().getName() + "#" + commandEvent.getAuthor().getDiscriminator());
        log.info("Wants to schedule " + commandEvent.getArgs());

        // get and verify the user's timezone role for conversion
        ArrayList<Role> timezoneRoles = RaidBot.getConfig().getTimezoneRoles();
        Role userTimezoneRole = commandEvent.getMember().getRoles().stream().filter(timezoneRoles::contains).findFirst().orElse(null);
        if (userTimezoneRole == null) {
            MessageUtils.sendAutoDeletedMessage("You do not have a timezone role assigned. Ask an admin to give you your role.", 300, commandEvent);
            return;
        }

        String[] args = commandEvent.getArgs().split("\\s+");
        if (args.length < 3) {
            MessageUtils.sendAutoDeletedMessage("Missing activity and/or date+time argument(s)", 300, commandEvent);
            return;
        } else if (args.length > 3) {
            MessageUtils.sendAutoDeletedMessage("Too many arguments. Should be in the format of: " + this.arguments, 300, commandEvent);
            return;
        }

        String activityString = args[0];

        String dateTimeString = formatDateTimeInput(args[1], args[2]);

        ZonedDateTime eventDateTime;
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
                MessageUtils.sendAutoDeletedMessage("Cannot schedule an event in the past.", 300, commandEvent);
                return;
            }
        } catch (DateTimeParseException e) {
            MessageUtils.sendAutoDeletedMessage("Invalid date/time argument.", 300, commandEvent);
            return;
        }

        try {
            if (Event.eventExists(activityString)) {
                Event plannedEvent = new Event(activityString, eventDateTime);
                plannedEvent.setPlayerStatus(commandEvent.getMember(), Event.EventPlayerStatus.ACCEPTED);
                LFGEmbedBuilder builder = new LFGEmbedBuilder(plannedEvent);
                MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
                messageCreateBuilder.addEmbeds(builder.build());

                Message success = commandEvent.getChannel().sendMessage(messageCreateBuilder.build()).complete();

                RestAction<Void> reactWhiteCheckMark = success.addReaction(LFGConstants.ACCEPTED_EMOJI);
                RestAction<Void> reactQuestion = success.addReaction(LFGConstants.TENTATIVE_EMOJI);
                RestAction<Void> reactCross = success.addReaction(LFGConstants.DECLINED_EMOJI);

                RestAction.allOf(reactWhiteCheckMark, reactQuestion, reactCross).queue();

                plannedEvent.registerEvent(success);

                Event.scheduleEventDeletion(eventDateTime, commandEvent, success);

                MessageUtils.autoDeleteMessage(commandEvent.getMessage(), 300);

            } else {
                MessageUtils.sendAutoDeletedMessage("That event does not exist", 300, commandEvent);
            }
        } catch (RaidBotRuntimeException e) {
            MessageUtils.sendAutoDeletedMessage("Error occurred while creating event.", 300, commandEvent);
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

        if (Integer.parseInt(dateSplit[2]) < 2000) {
            dateStringBuilder.append("20");
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

    /**
     * Construct String for when !help is called. Uses event.json to build out the message.
     *
     * @return - Help message for LFGCommand
     * @throws RaidBotRuntimeException - throw this to shut down the bot
     */
    private String buildHelpMessage() throws RaidBotRuntimeException {
        StringBuilder helpMessage = new StringBuilder();
        final String CODE = "`"; // back quote for formatting in discord
        helpMessage.append("\n" + LFGConstants.LFG_HELP_START);
        JsonObject eventJson = getEventsJson();
        // for each short name in event.json, create a line for the help message to explain short name and what it represents
        for (String shortName: eventJson.keySet()) {
            JsonObject eventObject = eventJson.getAsJsonObject(shortName);
            helpMessage.append(CODE).append(shortName).append(":").append(CODE).append(" ").append(eventObject.get(LFGConstants.LONG_NAME_KEY)).append("\n");
        }
        return helpMessage.toString();
    }

    /**
     * Retrieve event.json for use with building out help messages
     * @return JsonObject for event.json file
     * @throws RaidBotRuntimeException - throw this to shut down the bot
     */
    private JsonObject getEventsJson() throws RaidBotRuntimeException {

        JsonObject eventsJson;
        try {
            eventsJson = JsonParser.parseReader(new FileReader(LFGConstants.EVENT_JSON)).getAsJsonObject();
        } catch (FileNotFoundException e) {
            throw new RaidBotRuntimeException("Caught file not found exception, shutting down bot");
        }
        return eventsJson;
    }
}
