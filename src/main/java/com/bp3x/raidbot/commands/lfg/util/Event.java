package com.bp3x.raidbot.commands.lfg.util;

import com.bp3x.raidbot.RaidBot;
import com.bp3x.raidbot.commands.lfg.LFGConstants;
import com.bp3x.raidbot.util.RaidBotJsonUtils;
import com.bp3x.raidbot.util.RaidBotRuntimeException;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

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

        try {
            Event.saveEventsToJson();
        } catch (RaidBotRuntimeException e) {
            log.warn("Error occurred while saving events to JSON. Events may not be backed up properly.");
            e.printStackTrace();
        }
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
            FileOutputStream fos = new FileOutputStream("planned_events.json");
            JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(fos));
            jsonWriter.setIndent("\t");

            HashMap<Message, Event> eventMap = Event.getPlannedEventsList();

            jsonWriter.beginObject();
            for (Message message : eventMap.keySet()) {
                RaidBotJsonUtils.writeEventToJson(eventMap.get(message), message, jsonWriter);
            }
            jsonWriter.endObject();
            jsonWriter.close();
        } catch (IOException ioException) {
            File f = new File("planned_events.json");
            if (!f.exists()) {
                log.error("JSON backup file not found");
                throw new RaidBotRuntimeException("There was a fatal error in file IO; Planned events JSON backup file doesn't exist");
            } else {
                log.error("JSON backup file not writable");
                throw new RaidBotRuntimeException("There was a fatal error in file IO; Planned events JSON backup file does not exist");
            }
        }
    }

    /**
     * Method to read saved event data from JSON backup file on startup.
     */
    public static void loadEventsFromJson() throws RaidBotRuntimeException {
        log.info("Loading saved event data.");
        try {
            JsonObject eventsBackupJson = JsonParser.parseReader(new FileReader("planned_events.json")).getAsJsonObject();

            // early exit if there isn't a backup file or the json is invalid
            if (eventsBackupJson == null || eventsBackupJson.size() == 0) {
                log.info("No event backup found.");
                return;
            }

            log.info("Found " + eventsBackupJson.size() + " events in JSON backup file.");
            for (String messageId : eventsBackupJson.keySet()) {
                JsonObject eventObject = eventsBackupJson.getAsJsonObject(messageId);

                // Create event object using event constructor --> build up array lists of players in helper functions
                String eventId = eventObject.get("eventId").getAsString();
                String shortName = eventObject.get("shortName").getAsString();
                ZonedDateTime dateTime = RaidBotJsonUtils.jsonStringToDateTime(eventObject.get("time").getAsString());
                ArrayList<Member> acceptedPlayers = RaidBotJsonUtils.jsonArrayToMemberList(eventObject.get("acceptedPlayers").getAsJsonArray());
                ArrayList<Member> tentativePlayers = RaidBotJsonUtils.jsonArrayToMemberList(eventObject.get("tentativePlayers").getAsJsonArray());
                ArrayList<Member> declinedPlayers = RaidBotJsonUtils.jsonArrayToMemberList(eventObject.get("declinedPlayers").getAsJsonArray());

                Event newEvent = new Event(shortName, dateTime, eventId, acceptedPlayers, tentativePlayers, declinedPlayers);

                var guild = RaidBot.getJDA().getGuildById(RaidBot.getConfig().getGuildId());
                if (guild != null) {
                    var lfgChannel = guild.getTextChannelById(RaidBot.getConfig().getLfgChannelId());

                    if (lfgChannel != null) {
                        lfgChannel.retrieveMessageById(messageId).queue(message -> {
                            plannedEventsList.put(message, newEvent);
                            log.info("Successfully loaded event: " + newEvent.getShortName() + " " + RaidBotJsonUtils.zonedDateTimeToJsonString(newEvent));
                        });
                    }
                }
            }
        } catch (RuntimeException rte) {
            throw new RaidBotRuntimeException("There was an error parsing the planned_event.json file. Shutting down the bot.");

        } catch (FileNotFoundException e) {
            log.warn("Unable to find planned event json file. Proceeding without loading from backup.");
        }
    }
}
