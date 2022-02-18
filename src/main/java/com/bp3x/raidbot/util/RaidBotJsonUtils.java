package com.bp3x.raidbot.util;

import com.bp3x.raidbot.commands.lfg.LFGConstants;
import com.bp3x.raidbot.commands.lfg.util.Event;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.time.format.DateTimeFormatter;
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



    public static void playerListsToJson(Event raidBotEvent, JsonWriter jsonWriter) throws RaidBotRuntimeException {
        try {
            jsonWriter.beginArray().name("acceptedPlayers");
            for (Member m : raidBotEvent.getAcceptedPlayers()) {
                jsonWriter.name("userId").value(m.getId());
            }
            jsonWriter.endArray();
            jsonWriter.beginArray().name("tentativePlayers");
            for (Member m : raidBotEvent.getTentativePlayers()) {
                jsonWriter.name("userId").value(m.getId());
            }
            jsonWriter.endArray();
            jsonWriter.beginArray().name("declinedPlayers");
            for (Member m : raidBotEvent.getDeclinedPlayers()) {
                jsonWriter.name("userId").value(m.getId());
            }
            jsonWriter.endArray();
        } catch (IOException ioException) {
            File f = new File(LFGConstants.PLANNED_EVENTS_JSON);
            if (f.exists()) {
                log.error("JSON backup file not found");
                throw new RaidBotRuntimeException("There was a fatal error in file IO; JSON backup file is not writable.");
            }
            log.error("JSON backup file not writable");
            throw new RaidBotRuntimeException("There was a fatal error in file IO; JSON backup file does not exist");
        }
    }

    public static String zonedDateTimeToJsonString(Event raidBotEvent) {
       return DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN).format(raidBotEvent.getTime());
    }


}

