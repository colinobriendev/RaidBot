package com.bp3x.raidbot.commands.util;

import com.bp3x.raidbot.RaidBot;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

public class ShutdownButtonListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        event.deferReply(true).queue(hook -> {
            if (event.getComponentId().equals("shutdown")) {
                var owner = RaidBot.getConfig().getOwnerID();
                var coOwners = RaidBot.getConfig().getCoOwnerIDs();
                var allowedUsers = Arrays.asList(coOwners, owner);
                var userId = event.getUser().getId();
                
                if (allowedUsers.contains(userId)) {
                    ShutdownCommand.shutdown(hook);
                } else {
                    hook.editOriginal("You must be a co-owner or owner of the bot to do this.").queue();
                }
            } else if (event.getComponentId().equals("cancel")) {
                hook.editOriginal("Bot shutdown cancelled.").queue(); // update the message
            }
        });
    }
}