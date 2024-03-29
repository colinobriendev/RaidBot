package com.bp3x.raidbot.commands.lfg.util;

import com.bp3x.raidbot.RaidBot;
import com.bp3x.raidbot.commands.lfg.LFGConstants;
import com.bp3x.raidbot.util.RaidBotJsonUtils;
import com.bp3x.raidbot.util.RaidBotRuntimeException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents an event that can be scheduled in the LFG command.
 */
public class Event {
    private static final Logger log = LoggerFactory.getLogger(Event.class);
    private static final HashMap<Message, Event> plannedEventsList = new HashMap<>();
    // executor service for scheduling event deletion in background
    private final String shortName;
    private String longName;
    private int playerCount = 0;
    private final ArrayList<Member> acceptedPlayers = new ArrayList<>();
    private final ArrayList<Member> declinedPlayers = new ArrayList<>();
    private final ArrayList<Member> tentativePlayers = new ArrayList<>();
    private final String eventId;
    private final ZonedDateTime dateTime;

    private static final String CRYING_FACE_EMOJI = "\uD83D\uDE2D";

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

    public static HashMap<Message, Event> getPlannedEventsList() {
        return plannedEventsList;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public ArrayList<Member> getAcceptedPlayers() {
        return acceptedPlayers;
    }

    public ArrayList<Member> getDeclinedPlayers() {
        return declinedPlayers;
    }

    public ArrayList<Member> getTentativePlayers() {
        return tentativePlayers;
    }

    public String getEventId() {
        return eventId;
    }

    public ZonedDateTime getTime() {
        return dateTime;
    }

    public enum EventPlayerStatus {
        ACCEPTED,
        DECLINED,
        TENTATIVE
    }

    /**
     * Return true if accepted or tentative participants exist on the event, false otherwise
     *
     * @return - boolean
     */
    public boolean hasActiveParticipants() {
        log.info("Accepted player size = " + getAcceptedPlayers().size() + " and tentative size = " + getTentativePlayers().size());
        return !getAcceptedPlayers().isEmpty() || !getTentativePlayers().isEmpty();
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
     *
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
     *
     * @param player - The user to RSVP for the event
     * @param status - The status to set the player as for the event
     */
    public void setPlayerStatus(Member player, EventPlayerStatus status) {
        acceptedPlayers.remove(player);
        declinedPlayers.remove(player);
        tentativePlayers.remove(player);
        switch (status) {
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
     *
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
     *
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
     * Calculate the minutes between our planned event and now so the runnable knows when to execute
     */
    public static void saveEventsToJson() throws RaidBotRuntimeException {


        try (FileOutputStream fos = new FileOutputStream(LFGConstants.PLANNED_EVENTS_JSON);
             JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(fos))) {

            jsonWriter.setIndent("\t");

            HashMap<Message, Event> eventMap = Event.getPlannedEventsList();

            jsonWriter.beginObject();
            for (Message message : eventMap.keySet()) {
                RaidBotJsonUtils.writeEventToJson(eventMap.get(message), message, jsonWriter);
            }
            jsonWriter.endObject();

        } catch (IOException ioException) {
            File f = new File(LFGConstants.PLANNED_EVENTS_JSON);
            if (!f.exists()) {
                log.error("JSON backup file not found");
                throw new RaidBotRuntimeException("There was a fatal error in file IO; Planned events JSON backup file doesn't exist");
            } else {
                log.error("JSON backup file not writable");
                throw new RaidBotRuntimeException("There was a fatal error in file IO; Planned events JSON backup file does not exist");
            }
        }
    }

    private static long calculateMinutesBetween(ZonedDateTime eventTime) {
        ZonedDateTime now = ZonedDateTime.now();
        return Duration.between(now, eventTime).toMinutes();
    }

    /**
     * Schedule the event to be deleted from the plannedEventsList and LFGConstants.PLANNED_EVENTS_JSON using a runnable
     */
    public static void loadEventsFromJson() throws RaidBotRuntimeException {
        log.info("Loading saved event data.");
        try {
            JsonObject eventsBackupJson = JsonParser.parseReader(new FileReader(LFGConstants.PLANNED_EVENTS_JSON)).getAsJsonObject();

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

    public static void scheduleEventDeletion(ZonedDateTime eventTime, SlashCommandEvent commandEvent, Message embedMessage) {
        // schedule a message that will turn into removing the event after a time
        long minutesBetween = calculateMinutesBetween(eventTime);
        Runnable taskTest = () -> {
            try {
                handleEventDeletionAndNotifyPlayers(commandEvent, embedMessage);
            } catch (RaidBotRuntimeException e) {
                throw new RuntimeException(e);
            }
        };

        RaidBot.getRaidBotExecutorService().schedule(taskTest, minutesBetween, TimeUnit.MINUTES);

        if (log.isDebugEnabled()) {
            log.debug("There are " + minutesBetween + " minutes between event time and now");
        }
    }

    /**
     * Handle the deletion of the event from plannedEventsList and from the planned_events.json
     *
     * @param commandEvent - the command event
     * @param embedMessage - the message from the bot for the event
     */
    public static void handleEventDeletionAndNotifyPlayers(SlashCommandEvent commandEvent, Message embedMessage) throws RaidBotRuntimeException {
        Event event = plannedEventsList.get(embedMessage);
        StringBuilder builder = appendPlayerNames(event);
        // send a notification to accepted and potentially tentative players
        var eventsChannel = commandEvent.getChannel();
        eventsChannel.retrieveMessageById(embedMessage.getId()).queue(latestMessageInstance -> {
            var eventThread = latestMessageInstance.getStartedThread();
            if (eventThread != null) {
                eventThread.sendMessage(builder).queue();
            } else {
                eventsChannel.sendMessage(builder).queue();
            }
        });

        // remove the emojis because the event is starting
        embedMessage.clearReactions().queue();

        // remove the event from the internal list and update the json
        removeEvent(embedMessage);
    }

    /**
     * Append a list of accepted players to notify them when the event begins. Will also notify tentative if we have less than max count on accepted.
     *
     * @param event - the RaidBot Event
     * @return StringBuilder - names of players that have accepted or are tentative for the event
     */
    public static StringBuilder appendPlayerNames(Event event) {
        ArrayList<Member> acceptedPlayers = event.getAcceptedPlayers();
        ArrayList<Member> tentativePlayers = event.getTentativePlayers();
        int eventPlayerCount = event.getPlayerCount();
        StringBuilder message = new StringBuilder();
        if (!acceptedPlayers.isEmpty()) {
            message.append("Pinging accepted members: ");
            for (Member member : acceptedPlayers) {
                message.append(member.getAsMention()).append(" ");
            }
            if (!tentativePlayers.isEmpty() && acceptedPlayers.size() < eventPlayerCount) {
                message.append("\nPinging tentative members because we don't have enough accepted: ");
                for (Member member : tentativePlayers) {
                    message.append(member.getAsMention()).append(" ");
                }
            }
            message.append("\n").append(event.getLongName()).append(" is starting now!");
        } else {
            message.append(event.getLongName()).append(" is starting but has no participants ").append(CRYING_FACE_EMOJI);
        }
        return message;
    }

    /**
     * Remove an event from the plannedEventsList, save the backup json so its updated, and delete the message
     *
     * @param eventMessage - the message key corresponding to the value Event we wish to remove
     */
    public static void removeEvent(Message eventMessage) throws RaidBotRuntimeException {
        if (eventMessage != null) {
            plannedEventsList.remove(eventMessage);
            saveEventsToJson();

        }
    }

    /**
     * Find a Message from the provided input Event in the plannedEventsList HashMap
     *
     * @param event - event to find corresponding message for
     * @return corresponding message to event
     */
    public static Message findMessageFromEvent(Event event) {
        // work backwards to find key from value
        Message returnMessage = null;
        for (Map.Entry<Message, Event> entry : plannedEventsList.entrySet()) {
            if (entry.getValue().equals(event)) {
                returnMessage = entry.getKey();
            }
        }
        return returnMessage;
    }
}