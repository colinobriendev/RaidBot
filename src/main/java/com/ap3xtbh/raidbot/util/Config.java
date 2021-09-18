package com.ap3xtbh.raidbot.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Pull data from bot config json
 */
public class Config {
    private final Logger log = LoggerFactory.getLogger(Config.class);

    private String token = null;
    private String ownerID = null;
    private String coOwnerID = null;
    private String guildID = null;
    private String prefix = null;

    public String getToken() {
        return token;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String getCoOwnerID() {
        return coOwnerID;
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
            } else {
                log.info("Token loaded successfully");
            }

            this.ownerID = configFile.get("owner_id").getAsString();
            if (ownerID == null) {
                log.error("Owner id failed to load");
            } else {
                log.debug("Owner id loaded successfully");
            }

            this.coOwnerID = configFile.get("coowner_id").getAsString();
            if (coOwnerID == null) {
                log.error("Co-Owner failed to load");
            } else {
                log.debug("Co-Owner loaded successfully");
            }

            this.guildID = configFile.get("guild").getAsString();
            if (guildID == null) {
                log.error("Guild ID failed to load");
            } else {
                log.info("Guild ID loaded successfully");
            }

            this.prefix = configFile.get("prefix").getAsString();
            if (prefix == null) {
                log.error("Prefix failed to load");
            } else {
                log.debug("Prefix loaded successfully");
            }

        } catch (FileNotFoundException e) {
            log.error("Config json file not found", e);
        }
    }
}