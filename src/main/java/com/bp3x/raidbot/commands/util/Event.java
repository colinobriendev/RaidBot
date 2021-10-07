package com.bp3x.raidbot.commands.util;

import com.bp3x.raidbot.util.RaidBotRuntimeException;
import com.bp3x.raidbot.util.RaidBotUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Represents an event that can be scheduled in the LFG command.
 */
public class Event {
    private final Logger log = LoggerFactory.getLogger(Event.class);

    // constant strings to retrieve json file entries for events
    private static final String LONG_NAME_KEY = "long_name";
    private static final String PLAYER_COUNT_KEY = "player_count";
    private String longName;
    private int playerCount = 0;

    public Event(String shortName) throws RaidBotRuntimeException {
        load(shortName);
    }

    public String getLongName() {
        return longName;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    /**
     * Load event information from event config json.
     *
     * @param shortName - event shortname to load from.
     */
    public void load(String shortName) throws RaidBotRuntimeException {
        try {
            JsonElement element = JsonParser.parseReader(new FileReader("event.json"));
            JsonObject eventConfig = element.getAsJsonObject();
            // need an additional object to hold the specific event and its values
            JsonObject eventAsJSON = eventConfig.getAsJsonObject(shortName);

            this.longName = RaidBotUtils.getValueFromJSON(LONG_NAME_KEY, eventAsJSON);
            this.playerCount = Integer.parseInt(RaidBotUtils.getValueFromJSON(PLAYER_COUNT_KEY, eventAsJSON));

        } catch (RuntimeException rte) {
            throw new RaidBotRuntimeException("There was an error parsing the event.json file. Shutting down the bot.");

        } catch (FileNotFoundException e) {
            throw new RaidBotRuntimeException("Unable to find event json file. Shutting down the bot.");
        }
    }
}
