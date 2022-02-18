package com.bp3x.raidbot.commands.lfg.util;

import com.bp3x.raidbot.RaidBot;
import com.bp3x.raidbot.commands.lfg.LFGConstants;
import com.bp3x.raidbot.util.RaidBotJsonUtils;
import com.bp3x.raidbot.util.RaidBotRuntimeException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.bp3x.raidbot.commands.lfg.LFGConstants.TIMESTAMP_PATTERN;
import static com.bp3x.raidbot.util.RaidBotJsonUtils.*;

/**
 * Represents an event that can be scheduled in the LFG command.
 */
public class Event {
    private static final Logger log = LoggerFactory.getLogger(Event.class);

    private static final HashMap<Message, Event> plannedEventsList = new HashMap<>();

    //// These fields are stored in JSON backup (planned_events.json) //////
    private final String shortName;
    private String longName;
    private int playerCount = 0;
    private final ArrayList<Member> acceptedPlayers = new ArrayList<>();
    private final ArrayList<Member> declinedPlayers = new ArrayList<>();
    private final ArrayList<Member> tentativePlayers = new ArrayList<>();
    private final String eventId;
    private final ZonedDateTime dateTime;

    public Event(String shortName, ZonedDateTime dateTime) throws RaidBotRuntimeException {
        this.shortName = shortName;
        this.dateTime = dateTime;

        Random rand = new Random();
        this.eventId = String.valueOf(this.dateTime.getMonthValue()) +
                this.dateTime.getDayOfMonth() +
                rand.nextInt(10);

        load(shortName);

        log.info("Created Event \"" + shortName + "\" scheduled for " + this.dateTime + " with event ID " + this.eventId);
    }

    /**
     * Event Constructor for reading from JSON backup on startup.
     */
    public Event(String shortName, ZonedDateTime dateTime, String eventId,
                 ArrayList<Member> acceptedPlayers, ArrayList<Member> declinedPlayers,
                 ArrayList<Member> tentativePlayers) throws RaidBotRuntimeException {

        this.shortName = shortName;
        this.dateTime = dateTime;
        this.eventId = eventId;
        this.acceptedPlayers.addAll(acceptedPlayers);
        this.tentativePlayers.addAll(tentativePlayers);
        this.declinedPlayers.addAll(declinedPlayers);
        load(shortName);
    }

    public static HashMap<Message, Event> getPlannedEventsList() { return plannedEventsList; }

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

    public ZonedDateTime getTime() { return dateTime; }

    public enum EventPlayerStatus {
        ACCEPTED,
        DECLINED,
        TENTATIVE
    }

    /**
     * Load event information from event config json.
     * @param shortName - event shortname to load from.
     */
    public void load(String shortName) throws RaidBotRuntimeException {
        try {
            JsonElement element = JsonParser.parseReader(new FileReader("event.json"));
            JsonObject eventConfig = element.getAsJsonObject();
            // need an additional object to hold the specific event and its values
            JsonObject eventAsJSON = eventConfig.getAsJsonObject(shortName);

            this.longName = RaidBotJsonUtils.getValueFromJSON(LFGConstants.LONG_NAME_KEY, eventAsJSON);
            this.playerCount = Integer.parseInt(RaidBotJsonUtils.getValueFromJSON(LFGConstants.PLAYER_COUNT_KEY, eventAsJSON));

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
        plannedEventsList.put(message, this);
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
    public static boolean eventExists(String shortName) throws RaidBotRuntimeException {
        try {
            JsonElement element = JsonParser.parseReader(new FileReader("event.json"));
            JsonObject eventConfig = element.getAsJsonObject();

            return eventConfig.has(shortName);
        } catch (FileNotFoundException e) {
            throw new RaidBotRuntimeException("Unable to find event json file.");
        }
    }

    /**
     * Returns the event within the planned events list that corresponds with the given ID
     * Returns null if an event with that ID does not exist
     * @param id - event id to search for
     */
    public static Event getEventById(String id) {
        for (int i = 0; i < plannedEventsList.size(); i++) {
            Event[] eventsList = plannedEventsList.values().toArray(new Event[0]);
            if (eventsList[i].getEventId().equals(id)) {
                return eventsList[i];
            }
        }
        return null;
    }
    /**
     * Method to write current event state to json.
     * @throws RaidBotRuntimeException if the designated backup file does not exist or if there
     * is a problem with writing to the file.
     */
    public static void saveEventsToJson() throws RaidBotRuntimeException {
        try {
            JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(new FileOutputStream("planned_events.json")));
            HashMap<Message, Event> eventMap = Event.getPlannedEventsList();

            jsonWriter.beginArray();
            for (Message m : eventMap.keySet()) {
                Event event = eventMap.get(m);
                jsonWriter.name(m.getId()).beginObject();
                jsonWriter.name("shortName").value(event.getShortName());
                playerListsToJson(event, jsonWriter);
                jsonWriter.name("eventId").value(event.getEventId());
                jsonWriter.beginObject().name("dateTime");
                jsonWriter.name("date").value(zonedDateTimeToJsonString(event));
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
        } catch (IOException ioException) {
            File f = new File("planned_events.json");
            if (f.exists()) {
                log.error("JSON backup file not found");
                throw new RaidBotRuntimeException("There was a fatal error in file IO; Planned events JSON backup file is not writable.");
            }
            log.error("JSON backup file not writable");
            throw new RaidBotRuntimeException("There was a fatal error in file IO; Planned events JSON backup file does not exist");
        }
    }

    /**
     * Method to read saved event data from JSON backup file on startup.
     */
    public static void loadEventsFromJson() throws RaidBotRuntimeException {
        log.info("Loading saved event data.");
        try {
            JsonElement eventsJson = JsonParser.parseReader(new FileReader("planned_events.json")).getAsJsonArray();

            // early exit if there isn't a backup file or the json is invalid
            if (eventsJson == null) {
                log.info("No event backup found.");
                return;
            }

            JsonArray eventFile = eventsJson.getAsJsonArray();
            for (JsonElement eventElement : eventFile) {
                JsonObject eventObject = eventElement.getAsJsonObject();
                // Create event object using event constructor --> build up array lists of players in helper functions
                String eventId = eventObject.get("eventId").getAsString();
                String shortName = eventObject.get("shortName").getAsString();
                ZonedDateTime dateTime = RaidBotJsonUtils.jsonStringToDateTime(eventObject.get("dateTime").getAsString());
                ArrayList<Member> acceptedPlayers = RaidBotJsonUtils.jsonArrayToMemberList(eventObject.getAsJsonArray("acceptedPlayers"));
                ArrayList<Member> tentativePlayers = RaidBotJsonUtils.jsonArrayToMemberList(eventObject.getAsJsonArray("tentativePlayers"));
                ArrayList<Member> declinedPlayers = RaidBotJsonUtils.jsonArrayToMemberList(eventObject.getAsJsonArray("declinedPlayers"));
            }
        } catch (RuntimeException rte) {
            throw new RaidBotRuntimeException("There was an error parsing the planned_event.json file. Shutting down the bot.");

        } catch (FileNotFoundException e) {
            throw new RaidBotRuntimeException("Unable to find planned event json file. Shutting down the bot.");
        }
    }
}
