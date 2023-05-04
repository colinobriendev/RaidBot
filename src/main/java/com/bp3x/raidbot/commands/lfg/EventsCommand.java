package com.bp3x.raidbot.commands.lfg;

import com.bp3x.raidbot.util.RaidBotRuntimeException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class EventsCommand extends SlashCommand {
    
    Logger log = LoggerFactory.getLogger(EventsCommand.class);
    
    public EventsCommand() {
        this.name = "events";
        this.help = "List all the possible events that can be scheduled.";
        this.arguments = "";
        this.guildOnly = false;
        this.ownerCommand = false;
    }
    
    @Override
    protected void execute(SlashCommandEvent event) {
        var commandUser = event.getMember().getUser();
        log.info("Events command by: " + commandUser.getName() + "#" + commandUser.getDiscriminator());
        
        event.deferReply(true).queue(hook -> {
            try {
                String eventsString = buildEventsListString();
                hook.editOriginal(eventsString).queue();
            } catch (RaidBotRuntimeException e) {
                hook.editOriginal("Something went wrong while trying to retrieve the events list. " +
                                          "Please contact an admin.").queue();
                log.error(e.toString());
            }
        });
    }
    
    /**
     * Construct String for when /events is called. Uses event.json to build out the message.
     *
     * @return - Help message for LFGCommand
     * @throws RaidBotRuntimeException - throw this to shut down the bot
     */
    private String buildEventsListString() throws RaidBotRuntimeException {
        StringBuilder eventsListString = new StringBuilder();
        final String CODE_FORMATTER = "`"; // back-tick for formatting in discord
        eventsListString.append("\n" + LFGConstants.LFG_HELP_START);
        JsonObject eventJson = getEventsJson();
        // for each short name in event.json, create a line for the help message to explain short name and what it represents
        for (String shortName: eventJson.keySet()) {
            JsonObject eventObject = eventJson.getAsJsonObject(shortName);
            eventsListString.append(CODE_FORMATTER)
                    .append(shortName)
                    .append(CODE_FORMATTER)
                    .append(":")
                    .append(" ")
                    .append(eventObject.get(LFGConstants.LONG_NAME_KEY))
                    .append("\n");
        }
        return eventsListString.toString();
    }
    
    /**
     * Retrieve event.json for use with building out /events message
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

