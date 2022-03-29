package com.bp3x.raidbot.util;

import com.bp3x.raidbot.RaidBot;
import com.bp3x.raidbot.commands.lfg.util.Event;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import static com.bp3x.raidbot.commands.lfg.LFGConstants.TIMESTAMP_PATTERN;


public class RaidBotJsonUtils {

    private static final Logger log = LoggerFactory.getLogger(RaidBotJsonUtils.class);
    private static final Gson gson = new Gson();

    /* Utility class */
    private RaidBotJsonUtils() { }

    /**
     * Method to parse json config files
     *
     * @param key        - key value from json object being processed
     * @param jsonObject - json object
     * @return - string value
     */
    public static String getValueFromJSON(String key, JsonObject jsonObject) throws RaidBotRuntimeException {
        String toReturn;
        toReturn = jsonObject.get(key).getAsString();

        if (toReturn.isEmpty()) {
            throw new RaidBotRuntimeException("There was an issue parsing the JSON file provided with key " + key);
        } else {
            if (!key.equals("token")) {
                log.info("Key " + key + " loaded value " + toReturn + " successfully");
            } else {
                log.info("Loaded token successfully");
            }
        }
        return toReturn;
    }

    /**
     * Method to handle jsonArray objects stored in json files.
     *
     * @param key        - key value from json object
     * @param jsonObject - json object
     * @param size       - expected size of the json array
     * @return - String[] of values from JsonArray
     */
    public static String[] getArrayValueFromJSON(String key, JsonObject jsonObject, int size) {
        String[] toReturn = new String[size];
        JsonArray jsonArray = jsonObject.getAsJsonArray(key);
        for (int i = 0; i < jsonArray.size(); i++) {
            toReturn[i] = jsonArray.get(i).getAsString();
        }
        return toReturn;
    }

    public static void writeEventToJson(Event event, Message m, JsonWriter writer) throws RaidBotRuntimeException {
        try {
            writer.name(m.getId()).beginObject();
            writer.name("shortName").value(event.getShortName());
            writeEventPlayerListsToJson(event, writer);
            writer.name("eventId").value(event.getEventId());
            writer.name("time").value(zonedDateTimeToJsonString(event));
            writer.endObject();
        } catch (IOException ioException) {
            File f = new File("planned_events.json");
            if (f.exists()) {
                log.error("JSON backup file not found");
                throw new RaidBotRuntimeException("There was a fatal error in file IO; JSON backup file is not writable.");
            }
            log.error("JSON backup file not writable");
            throw new RaidBotRuntimeException("There was a fatal error in file IO; JSON backup file does not exist");
        }
    }

    /**
     * Write a given event's player lists (accepted, tentative, declined) to JSON backup
     *
     * @param event The event whose player lists to write to JSON
     * @param jsonWriter The JsonWriter to write to
     * @throws RaidBotRuntimeException
     */
    public static void writeEventPlayerListsToJson(Event event, JsonWriter jsonWriter) {
        try {
            writePlayerListToJson(event.getAcceptedPlayers(), "acceptedPlayers", jsonWriter);
            writePlayerListToJson(event.getTentativePlayers(), "tentativePlayers", jsonWriter);
            writePlayerListToJson(event.getDeclinedPlayers(), "declinedPlayers", jsonWriter);
        } catch (RaidBotRuntimeException e) {
            log.error("Error while writing to JSON backup! Backup likely did not complete successfully.");
            e.printStackTrace();
        }
    }

    /**
     * Write all players in a list (accepted, declined, or tentative) to the JSON backup
     *
     * @param players List of players to write to JSON
     * @param keyName JSON key string that corresponds to the list
     * @param writer JSON writer to write to
     */
    public static void writePlayerListToJson(ArrayList<Member> players, String keyName, JsonWriter writer) throws RaidBotRuntimeException {
        try {
            writer.name(keyName).beginArray();
            for (Member m : players) {
                writer.value(m.getId());
            }
            writer.endArray();
        } catch (IOException ioException) {
            File f = new File("planned_events.json");
            if (!f.exists()) {
                log.error("JSON backup file not found");
                throw new RaidBotRuntimeException("There was a fatal error in file IO; JSON backup file is not writable.");
            } else {
                log.error("JSON backup file not writable");
                throw new RaidBotRuntimeException("There was a fatal error in file IO; JSON backup file does not exist");
            }
        }
    }

    /**
     * Method to retrieve an event's timestamp as a formatted string
     */
    public static String zonedDateTimeToJsonString(Event raidBotEvent) {
       return DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN).format(raidBotEvent.getTime());
    }

    /**
     *  Method to retrieve a ZonedDateTime object from planned_events.json
     */
    public static ZonedDateTime jsonStringToDateTime(String dateTime) {
        DateTimeFormatter raidBotTimeFormatter = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN).withZone(ZoneId.of("GMT"));
        return ZonedDateTime.parse(dateTime, raidBotTimeFormatter);
    }

    /**
     * Method to retrieve an ArrayList of members from planned_events.json
     */
    public static ArrayList<Member> jsonArrayToMemberList(JsonArray jsonArray) {
        ArrayList<Member> memberList = new ArrayList<>();

        String guildId = RaidBot.getConfig().getGuildId();
        Guild guild = RaidBot.getJDA().getGuildById(guildId);

        if (guild != null) {
            for (JsonElement memberElement : jsonArray) {
                long memberId = memberElement.getAsLong();

                Member member = guild.retrieveMemberById(memberId).complete();
                if (member != null) {
                    memberList.add(member);
                }
            }
        }

        return memberList;
    }
}