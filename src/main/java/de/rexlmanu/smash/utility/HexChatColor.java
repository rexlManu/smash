package de.rexlmanu.smash.utility;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexChatColor {

    private static final Pattern pattern = Pattern.compile("#(?:[A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})");

    public static String format(String message) {
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            if(!color.startsWith("#")) continue;

            message = message.replace(color, ChatColor.of(color).toString());
        }

        return message;
    }

}
