package com.bp3x.raidbot.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileNotFoundException;
import java.io.FileReader;


/**
 * POJO class to parse config.json file
 */
public class Config {
    private final Logger log = LoggerFactory.getLogger(Config.class);
    private String token = null;
    private String ownerID = null;
    private String prefix = null;
    private String[] coOwnerIDs;
    //keys from config.json file
    private static final String TOKEN_KEY = "token";
    private static final String OWNER_KEY = "owner_id";
    private static final String CO_OWNER_KEY = "coowner_ids";
    private static final String PREFIX_KEY = "prefix";

    public String getToken() {
        return token;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String[] getCoOwnerIDs() {
        return coOwnerIDs;
    }

    public String getPrefix() {
        return prefix;
    }

    public void load() {
        log.info("Loading Bot configuration.");

        try {
            JsonElement element = JsonParser.parseReader(new FileReader("config.json"));
            JsonObject configFile = element.getAsJsonObject();

            this.token = RaidBotUtils.getValueFromJSON(TOKEN_KEY, configFile);
            this.ownerID = RaidBotUtils.getValueFromJSON(OWNER_KEY, configFile);
            this.coOwnerIDs = RaidBotUtils.getArrayValueFromJSON(CO_OWNER_KEY, configFile, 2);
            this.prefix = RaidBotUtils.getValueFromJSON(PREFIX_KEY, configFile);


        } catch (RaidBotRuntimeException rte) {
            log.error("There was an error parsing the json file. Exiting.", rte);

        // must throw System.exit because JDA isn't initialized before Config parsing
        } catch (FileNotFoundException e) {
            log.error("Config json file not found. Exiting.", e);
            System.exit(1);
        }
    }
}