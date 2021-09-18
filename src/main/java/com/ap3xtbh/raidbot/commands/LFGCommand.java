package com.ap3xtbh.raidbot.commands;

import com.ap3xtbh.raidbot.commands.util.LFGEmbedBuilder;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LFG Command to schedule events
 */
public class LFGCommand extends Command {
    private final Logger log = LoggerFactory.getLogger(LFGCommand.class);

    public LFGCommand() {
        this.name = "lfg";
        this.help = "Use to schedule a event";
        this.arguments = "<shortName>";
        this.guildOnly = false;
        this.ownerCommand = false;
        this.category = new Category("General");

    }

    @Override
    protected void execute(CommandEvent event) {
        log.info("LFG command by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator());
        log.info("Wants to schedule " + event.getArgs());

        LFGEmbedBuilder builder = new LFGEmbedBuilder(event);

        Message success = event.getChannel().sendMessage(builder.build()).complete();
    }
}
