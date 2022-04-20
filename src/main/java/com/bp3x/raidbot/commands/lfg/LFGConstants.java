package com.bp3x.raidbot.commands.lfg;

import java.util.Arrays;
import java.util.List;

/**
 *  a class of constants for the LFG command
 */
public class LFGConstants {

    private LFGConstants(){}

    /* file names */
    public static final String PLANNED_EVENTS_JSON = "planned_events.json";
    public static final String CONFIG_JSON = "config.json";
    public static final String EVENT_JSON = "event.json";

    /* emoji constants for reactions on LFG embed */
    public static final String ACCEPTED_EMOJI = "✅";
    public static final String TENTATIVE_EMOJI = "❓";
    public static final String DECLINED_EMOJI = "❌";

    /* list of strings containing our emoji constants */
    protected static final List<String> EMOJI_LIST = Arrays.asList(ACCEPTED_EMOJI, TENTATIVE_EMOJI, DECLINED_EMOJI);

    /* timestamp pattern for LFG event */
    public static final String TIMESTAMP_PATTERN = "MM/dd/yyyy hh:mma";

    /* constant strings to retrieve json file entries for events */
    public static final String LONG_NAME_KEY = "long_name";
    public static final String PLAYER_COUNT_KEY = "player_count";

    /* constants for LFG Embed builder */
    public static final String PLAYER_COUNT = "Player Count: ";
    public static final String EVENT_ID = "Event ID: ";
    public static final String ACCEPTED_PLAYERS = "Accepted Players:";
    public static final String DECLINED_PLAYERS = "Declined Players:";
    public static final String TENTATIVE_PLAYERS = "Tentative Players:";
    public static final String BLANK_PLAYER_LIST = "[N/A]";
}
