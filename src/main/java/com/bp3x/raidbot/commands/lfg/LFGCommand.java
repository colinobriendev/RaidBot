package com.bp3x.raidbot.commands.lfg;

import com.bp3x.raidbot.RaidBot;
import com.bp3x.raidbot.commands.lfg.util.Event;
import com.bp3x.raidbot.commands.lfg.util.LFGEmbedBuilder;
import com.bp3x.raidbot.util.RaidBotRuntimeException;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * LFG Command to schedule events
 */
public class LFGCommand extends SlashCommand {
    private final Logger log = LoggerFactory.getLogger(LFGCommand.class);
    
    public LFGCommand() {
        this.name = "lfg";
        this.help = "Schedule a new event.";
        this.guildOnly = true;
        this.ownerCommand = false;
        
        this.options = Arrays.asList(
                new OptionData(OptionType.STRING, "event", "The short name of the event you want to schedule.", true),
                new OptionData(OptionType.STRING, "date", "The date of the event in MM/dd/yy format. Example: 06/28/23", true),
                new OptionData(OptionType.STRING, "time", "The time of the event in hh:mma format. Example: 8:30pm", true)
        );
    }
    
    @Override
    protected void execute(SlashCommandEvent commandEvent) {
        String shortName = commandEvent.optString("event");
        String date = commandEvent.optString("date");
        String time = commandEvent.optString("time");
    
        var commandUser = commandEvent.getMember().getUser();
        log.info("LFG command by: " + commandUser.getName() + "#" + commandUser.getDiscriminator());
        log.info("Wants to schedule " + shortName + " " + date + " " + time);
        
        commandEvent.deferReply(true).queue(hook -> {
            // get and verify the user's timezone role for conversion
            ArrayList<Role> timezoneRoles = RaidBot.getConfig().getTimezoneRoles();
            Role userTimezoneRole = commandEvent.getMember().getRoles().stream().filter(timezoneRoles::contains).findFirst().orElse(null);
            if (userTimezoneRole == null) {
                hook.editOriginal("You do not have a timezone role assigned. Ask an admin to give you your role.").queue();
                return;
            }
            
            if (commandEvent.getChannelType().isThread()) {
                hook.editOriginal("This command cannot be used in a thread. Please try again in the event planning channel.").queue();
                return;
            }
            
            String dateTimeString = formatDateTimeInput(date, time);
            
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
                    hook.editOriginal("Cannot schedule an event in the past. Given timestamp: " + date + " " + time).queue();
                    return;
                }
            } catch (DateTimeParseException e) {
                hook.editOriginal("Invalid date and/or time given. Please review argument format or ask an admin for help.\n" +
                                          "Full parsing error: ||" + e.getMessage() + "||.").queue();
                return;
            }
            
            try {
                if (Event.eventExists(shortName)) {
                    Event plannedEvent = new Event(shortName, eventDateTime);
                    plannedEvent.setPlayerStatus(commandEvent.getMember(), Event.EventPlayerStatus.ACCEPTED);
                    LFGEmbedBuilder builder = new LFGEmbedBuilder(plannedEvent);
                    MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
                    messageCreateBuilder.addEmbeds(builder.build());
                    
                    ZonedDateTime finalEventDateTime = eventDateTime;
                    commandEvent.getChannel().sendMessage(messageCreateBuilder.build()).queue((m) -> {
                        RestAction<Void> reactWhiteCheckMark = m.addReaction(LFGConstants.ACCEPTED_EMOJI);
                        RestAction<Void> reactQuestion = m.addReaction(LFGConstants.TENTATIVE_EMOJI);
                        RestAction<Void> reactCross = m.addReaction(LFGConstants.DECLINED_EMOJI);
                        RestAction.allOf(reactWhiteCheckMark, reactQuestion, reactCross).queue();
                        
                        plannedEvent.registerEvent(m);
                        
                        hook.editOriginal("Event created successfully.").queue();
                        
                        m.createThreadChannel(plannedEvent.getLongName() + "-" + plannedEvent.getEventId()).queue(thread -> {
                            thread.sendMessage("Creating this thread to help you organize your event.").queue();
                            thread.addThreadMember(commandEvent.getMember()).queue();
                        });
                        
                        Event.scheduleEventDeletion(finalEventDateTime, commandEvent, m);
                    });
                    
                } else {
                    hook.editOriginal("That event type does not exist. You entered: `" + shortName + "`").queue();
                }
            } catch (RaidBotRuntimeException e) {
                hook.editOriginal("Error occurred while creating event. Try again or contact an admin.").queue();
            }
        });
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
}