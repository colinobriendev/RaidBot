package com.bp3x.raidbot.commands.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Represents an event that can be scheduled in the LFG command.
 */
public class Event {
    private static final Logger log;

    static {
        log = LoggerFactory.getLogger(Event.class);
    }

    // constant strings to retrieve json file entries for events
    private static final String JSON_LONG_NAME = "long_name";
    private static final String JSON_TYPE = "type";
    private static final String JSON_PLAYER_COUNT = "player_count";
    private static final String JSON_EXACT_PLAYER_COUNT = "exact_player_count";
    private String shortName;
    private String longName;
    private String type;
    private int playerCount = 0;
    private boolean exactPlayerCount = false;
    private java.util.ArrayList<User> acceptedPlayers;
    private ArrayList<User> declinedPlayers;
    private ArrayList<User> tentativePlayers;

    public Event(String shortName) {
        this.shortName = shortName;
        load(shortName);
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public String getType() {
        return type;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public boolean isExactPlayerCount() {
        return exactPlayerCount;
    }

    public ArrayList<User> getAcceptedPlayers() { return acceptedPlayers; }

    public ArrayList<User> getDeclinedPlayers() { return declinedPlayers; }

    public ArrayList<User> getTentativePlayers() { return tentativePlayers; }

    public String getJSON_LONG_NAME() {
        return JSON_LONG_NAME;
    }

    public String getJSON_TYPE() {
        return JSON_TYPE;
    }

    public String getJSON_PLAYER_COUNT() {
        return JSON_PLAYER_COUNT;
    }

    public String getJSON_EXACT_PLAYER_COUNT() {
        return JSON_EXACT_PLAYER_COUNT;
    }

    /**
     * Load event information from event config json.
     * @param shortName - event shortname to load from.
     */
    public void load(String shortName) {
        try {
            JsonElement element = JsonParser.parseReader(new FileReader("event.json"));
            JsonObject eventConfig = element.getAsJsonObject();
            JsonObject eventAsJSON = eventConfig.getAsJsonObject(shortName);

            this.longName = eventAsJSON.get(JSON_LONG_NAME).getAsString();
            if (this.longName == null) {
                log.error("longName failed to load.");
            } else {
                log.info("longName loaded successfully.");
            }

            this.type = eventAsJSON.get(JSON_TYPE).getAsString();
            if (this.type == null) {
                log.error("longName failed to load.");
            } else {
                log.info("longName loaded successfully.");
            }

            this.playerCount = eventAsJSON.get(JSON_PLAYER_COUNT).getAsInt();
            if (this.playerCount == 0) {
                log.error("playerCount failed to load.");
            } else {
                log.info("playerCount loaded successfully.");
            }

            this.exactPlayerCount = eventAsJSON.get(JSON_EXACT_PLAYER_COUNT).getAsBoolean();

        } catch (FileNotFoundException e) {
            log.error("Unable to find event json file.", e);
        }
    }

    /**
     * Checks whether a given event is currently defined in event config json.
     * @param shortName - event shortname to test existence for
     */
    public static boolean eventExists(String shortName) {
        try {
            JsonElement element = JsonParser.parseReader(new FileReader("event.json"));
            JsonObject eventConfig = element.getAsJsonObject();

            return eventConfig.has(shortName);
        } catch (FileNotFoundException e) {
            log.error("Unable to find event json file.", e);
        }
        return false;
    }

    /**
     * Registers a user's availability for the event, removing existing status if present
     * @param player - The user to RSVP for the event
     * @param status - The status to set the player as for the event
     */
    public void setPlayerStatus(User player, EventPlayerStatus status) {
        acceptedPlayers.remove(player);
        declinedPlayers.remove(player);
        tentativePlayers.remove(player);

        switch(status) {
            case ACCEPTED:
                acceptedPlayers.add(player);
                break;
            case DECLINED:
                declinedPlayers.add(player);
                break;
            case TENTATIVE:
                tentativePlayers.add(player);
                break;
        }
    }

    public enum EventPlayerStatus {
        ACCEPTED,
        DECLINED,
        TENTATIVE
    }
}
