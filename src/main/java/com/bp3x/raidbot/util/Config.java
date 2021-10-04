package com.bp3x.raidbot.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;


/**
 * Pull data from bot config json
 */
public class Config {
    private final Logger log = LoggerFactory.getLogger(Config.class);

    private String token = null;
    private String ownerID = null;
    private final String[] coOwnerIDs = new String[2];
    private String guildID = null;
    private String prefix = null;
    private final JDA jda;

    public Config(JDA passedJDA) {
        jda = passedJDA;
    }

    public String getToken() {
        return token;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String[] getCoOwnerIDs() {
        return coOwnerIDs;
    }

    public String getGuildID() {
        return guildID;
    }

    public String getPrefix() {
        return prefix;
    }

    public void load() {
        log.info("Loading Bot configuration.");

        try {
            JsonElement element = JsonParser.parseReader(new FileReader("config.json"));
            JsonObject configFile = element.getAsJsonObject();

            this.token = configFile.get("token").getAsString();
            if (token == null) {
                log.error("Token failed to load.");
                jda.shutdown();
            } else {
                log.info("Token loaded successfully");
            }

            this.ownerID = configFile.get("owner_id").getAsString();
            if (ownerID == null) {
                log.error("Owner id failed to load");
                jda.shutdown();
            } else {
                log.debug("Owner id loaded successfully");
            }

            JsonArray coownerArray = configFile.getAsJsonArray("coowner_ids");
            for (int i = 0; i < coownerArray.size(); i++) {
                    coOwnerIDs[i] = coownerArray.get(i).getAsString();
            }
           if (coOwnerIDs.length != 2){
                log.error("CoOwners ID's is wrong size, ID's loaded are " + Arrays.toString(coOwnerIDs));
                jda.shutdown();
            }
            else {
                log.info("Co-Owners loaded successfully ");
            }

            this.guildID = configFile.get("guild").getAsString();
            if (guildID == null) {
                log.error("Guild ID failed to load");
                jda.shutdown();
            } else {
                log.info("Guild ID loaded successfully");
            }

            this.prefix = configFile.get("prefix").getAsString();
            if (prefix == null) {
                log.error("Prefix failed to load");
                jda.shutdown();
            } else {
                log.debug("Prefix loaded successfully");
            }

        } catch (FileNotFoundException e) {
            log.error("Config json file not found", e);
            jda.shutdown();
        }
    }
}