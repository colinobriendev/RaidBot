package com.bp3x.raidbot.commands.util;

import com.bp3x.raidbot.util.RaidBotRuntimeException;
import com.bp3x.raidbot.util.RaidBotUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Represents an event that can be scheduled in the LFG command.
 */
public class Event {
    private static final Logger log = LoggerFactory.getLogger(Event.class);

    // constant strings to retrieve json file entries for events
    private static final String LONG_NAME_KEY = "long_name";
    private static final String PLAYER_COUNT_KEY = "player_count";

    private static final HashMap<Event, Message> plannedEventsList = new HashMap<>();

    private final String shortName;
    private String longName;
    private int playerCount = 0;
    private final ArrayList<Member> acceptedPlayers = new ArrayList<>();
    private final ArrayList<Member> declinedPlayers = new ArrayList<>();
    private final ArrayList<Member> tentativePlayers = new ArrayList<>();
    private final String eventId;
    private final LocalDateTime time;

    public Event(String shortName) throws RaidBotRuntimeException {
        this.shortName = shortName;
        this.time = LocalDateTime.now();

        Random rand = new Random();
        this.eventId = String.valueOf(this.time.getMonthValue()) +
                this.time.getDayOfMonth() +
                rand.nextInt(10);

        load(shortName);

        log.info("Created Event \"" + shortName + "\" scheduled for " + this.time + " with event ID " + this.eventId);
    }

    public static HashMap<Event, Message> getPlannedEventsList() { return plannedEventsList; }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public ArrayList<Member> getAcceptedPlayers() { return acceptedPlayers; }

    public ArrayList<Member> getDeclinedPlayers() { return declinedPlayers; }

    public ArrayList<Member> getTentativePlayers() { return tentativePlayers; }

    public String getEventId() { return eventId; }

    public LocalDateTime getTime() { return time; }


    public enum EventPlayerStatus {
        ACCEPTED,
        DECLINED,
        TENTATIVE
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

    /**
     * Adds the event to the list of currently planned events
     * @param message The Discord message (embed post) associated with the event
     */
    public void registerEvent(Message message) {
        plannedEventsList.put(this, message);
    }

    /**
     * Registers a user's availability for the event, removing existing status if present
     * @param player - The user to RSVP for the event
     * @param status - The status to set the player as for the event
     */
    public void setPlayerStatus(Member player, EventPlayerStatus status) {
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
     * Returns the event within the planned events list that corresponds with the given ID
     * Returns null if an event with that ID does not exist
     * @param id - event id to search for
     */
    public static Event getEventById(String id) {
        for (int i = 0; i < plannedEventsList.size() - 1; i++) {
            Event[] eventsList = plannedEventsList.keySet().toArray(new Event[0]);
            if (eventsList[i].getEventId().equals(id)) {
                return eventsList[i];
            }
        }

        return null;
    }
}
