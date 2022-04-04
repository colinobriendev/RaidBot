
package com.bp3x.raidbot.util;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class RaidBotJsonUtils {
    private static final Logger log = LoggerFactory.getLogger(RaidBotJsonUtils.class);

    /* Utility class */
    private RaidBotJsonUtils() {
    }

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
}