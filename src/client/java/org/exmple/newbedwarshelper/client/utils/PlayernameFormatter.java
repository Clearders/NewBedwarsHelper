package org.exmple.newbedwarshelper.client.utils;

import net.minecraft.ChatFormatting;

public class PlayernameFormatter {
    public String cleanPlayerName(String rawName) {
        if (rawName == null || rawName.isEmpty()) {
            return "";
        }

        return ChatFormatting.stripFormatting(rawName);
    }
}
