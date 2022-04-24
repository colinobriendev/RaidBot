package com.bp3x.raidbot.util;

import com.bp3x.raidbot.commands.lfg.LFGConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * POJO class to parse config.json file
 */
public class Config {
    private final Logger log = LoggerFactory.getLogger(Config.class);
    private String token = null;
    private String ownerID = null;
    private String[] coOwnerIDs;
    private String guildId;
    private String lfgChannelId = null;
    private String prefix = null;
    private final HashMap<String, String> timezones = new HashMap<>();
    private ArrayList<Role> timezoneRoles = new ArrayList<>();

    // keys from config.json file
    private static final String TOKEN_KEY = "token";
    private static final String OWNER_KEY = "owner_id";
    private static final String CO_OWNER_KEY = "coowner_ids";
    private static final String GUILD_ID_KEY = "guild_id";
    private static final String LFG_CHANNEL_ID = "lfg_channel_id";
    private static final String PREFIX_KEY = "prefix";
    private static final String TIMEZONES_KEY = "timezones";
    private static final String TIMEZONES_ID_KEY = "id";
    private static final String TIMEZONES_FULL_NAME_KEY = "full_name";

    public String getToken() {
        return token;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String[] getCoOwnerIDs() {
        return coOwnerIDs;
    }

    public String getGuildId() { return guildId; }

    public String getLfgChannelId() { return lfgChannelId; }

    public String getPrefix() {
        return prefix;
    }

    public HashMap<String, String> getTimezones() { return timezones; }

    public void setTimezoneRoles(ArrayList<Role> roles) { this.timezoneRoles = roles; }

    public ArrayList<Role> getTimezoneRoles() { return timezoneRoles; }

    public void load() {
        log.info("Loading Bot configuration.");

        try {
            JsonElement element = JsonParser.parseReader(new FileReader(LFGConstants.CONFIG_JSON));
            JsonObject configFile = element.getAsJsonObject();

            this.token = RaidBotJsonUtils.getValueFromJSON(TOKEN_KEY, configFile);
            this.ownerID = RaidBotJsonUtils.getValueFromJSON(OWNER_KEY, configFile);
            this.coOwnerIDs = RaidBotJsonUtils.getArrayValueFromJSON(CO_OWNER_KEY, configFile, 2);
            this.guildId = RaidBotJsonUtils.getValueFromJSON(GUILD_ID_KEY, configFile);
            this.lfgChannelId = RaidBotJsonUtils.getValueFromJSON(LFG_CHANNEL_ID, configFile);
            this.prefix = RaidBotJsonUtils.getValueFromJSON(PREFIX_KEY, configFile);

            // parse timezones as a hashmap
            JsonArray timezonesJsonArray = configFile.get(TIMEZONES_KEY).getAsJsonArray();
            for (JsonElement tzElement : timezonesJsonArray) {
                JsonObject tzObject = tzElement.getAsJsonObject();

                String id = tzObject.get(TIMEZONES_ID_KEY).getAsString();
                String fullName = tzObject.get(TIMEZONES_FULL_NAME_KEY).getAsString();

                if (id.isBlank() || fullName.isBlank()) {
                    log.error("Parsed incomplete timezone information.");
                    throw new RaidBotRuntimeException("Incomplete timezone information, there are errors in the config file.");
                }

                timezones.put(tzObject.get(TIMEZONES_ID_KEY).getAsString(), tzObject.get(TIMEZONES_FULL_NAME_KEY).getAsString());
            }
        } catch (RaidBotRuntimeException rte) {
            log.error("There was an error parsing the json file. Exiting.", rte);

        // must throw System.exit because JDA isn't initialized before Config parsing
        } catch (FileNotFoundException e) {
            log.error("Config json file not found. Exiting.", e);
            System.exit(1);
        }
    }
}