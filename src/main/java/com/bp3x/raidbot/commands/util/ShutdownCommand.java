package com.bp3x.raidbot.commands.util;

import com.bp3x.raidbot.RaidBot;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownCommand extends SlashCommand {
    private final EventWaiter waiter;

    static Logger log = LoggerFactory.getLogger(ShutdownCommand.class);

    public ShutdownCommand() {
        this.name = "shutdown";
        this.help = "Terminates RaidBot";
        this.arguments = "";
        this.guildOnly = false;
        this.ownerCommand = true;

        this.waiter = RaidBot.getWaiter();
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        var commandUser = event.getMember().getUser();
        log.info("Shutdown command by: " + commandUser.getName() + "#" + commandUser.getDiscriminator());
    
        event.reply("Confirm bot shutdown?").addActionRow(
                Button.primary("cancel", "Cancel"),
                Button.danger("shutdown", "Shutdown"))
                .queue();
    }

    protected static void shutdown(InteractionHook hook) {
        hook.editOriginal("Bot shutdown confirmed.").queue(m -> {
            try {
                RaidBot.getJDA().getPresence().setPresence(OnlineStatus.OFFLINE, true);
                Thread.sleep(50);
        
                // disconnect from websocket, exit program
                RaidBot.getJDA().shutdown();
                System.exit(0);
            } catch (InterruptedException e) {
                log.error(e.toString());
                m.editMessage("An error occurred while shutting down. Please wait a few moments and try again.").queue();
            }
        });
    }
}

