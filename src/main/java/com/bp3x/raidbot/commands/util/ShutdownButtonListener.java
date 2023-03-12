package com.bp3x.raidbot.commands.util;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ShutdownButtonListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("shutdown")) {
            ShutdownCommand.shutdown(event);
        } else if (event.getComponentId().equals("cancel")) {
            event.editMessage("Bot shutdown cancelled.").queue(); // update the message
        }
    }
}