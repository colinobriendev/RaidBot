package com.bp3x.raidbot.commands;

import com.bp3x.raidbot.commands.util.LFGEmbedBuilder;
import com.bp3x.raidbot.util.RaidBotRuntimeException;
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

        LFGEmbedBuilder builder = null;
        try {
            builder = new LFGEmbedBuilder(event);
            Message success = event.getChannel().sendMessage(builder.build()).complete();

        } catch (NullPointerException npe) {
            log.error("LFGEmbedBuilder was null, did not initialize", npe);
        } catch (RaidBotRuntimeException e) {
            log.error("Caught RaidBotRuntimeException while creating LFGEmbedBuilder.", e);
        }
    }
}
