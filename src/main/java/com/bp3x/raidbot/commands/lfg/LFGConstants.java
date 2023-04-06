package com.bp3x.raidbot.commands.lfg;

import net.dv8tion.jda.api.entities.emoji.Emoji;

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

    /* TODO: remove these constants and use Emoji below */
    public static final String ACCEPTED_EMOJI_STRING = "✅";
    public static final String TENTATIVE_EMOJI_STRING = "❓";
    public static final String DECLINED_EMOJI_STRING = "❌";

    public static final Emoji ACCEPTED_EMOJI = Emoji.fromUnicode("✅");
    public static final Emoji TENTATIVE_EMOJI = Emoji.fromUnicode("❓");
    public static final Emoji DECLINED_EMOJI = Emoji.fromUnicode("❌");



    /* list of strings containing our emoji constants */
    protected static final List<String> EMOJI_LIST = Arrays.asList(ACCEPTED_EMOJI_STRING, TENTATIVE_EMOJI_STRING, DECLINED_EMOJI_STRING);

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

    public static final String LFG_HELP_START = "**The below list of events can be used in the /lfg command:**\n\n";
}
