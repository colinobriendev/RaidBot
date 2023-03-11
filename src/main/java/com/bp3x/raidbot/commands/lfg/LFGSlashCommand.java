package com.bp3x.raidbot.commands.lfg;

import com.bp3x.raidbot.util.RaidBotRuntimeException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class LFGSlashCommand extends SlashCommand {
    private final Logger log = LoggerFactory.getLogger(LFGSlashCommand.class);
    
    public LFGSlashCommand() throws RaidBotRuntimeException {
        this.name = "lfg";
        this.help = "Schedule a new event";
        this.guildOnly = false;
        this.ownerCommand = false;
        
        this.options = Arrays.asList(
            new OptionData(OptionType.STRING, "event", "The short name of the event you want to schedule.", true),
            new OptionData(OptionType.STRING, "date", "The date of the event in MM/dd/yy format. Example: 06/28/23", true),
            new OptionData(OptionType.STRING, "time", "The time of the event in hh:mma format. Example: 8:30pm", true)
        );
    }
    
    @Override
    protected void execute(SlashCommandEvent commandEvent) {
        User commandUser = commandEvent.getUser();
        String shortName = commandEvent.optString("event");
        String date = commandEvent.optString("date");
        String time = commandEvent.optString("time");
        
        commandEvent.deferReply().queue(
                hook -> hook.editOriginal("Success!").queueAfter(3, TimeUnit.SECONDS)
        );
    }
}