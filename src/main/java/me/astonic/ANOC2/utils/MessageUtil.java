package me.astonic.ANOC2.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtil {
    
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }
    
    public static void sendMessage(Player player, String message) {
        player.sendMessage(colorize(message));
    }
    
    public static void sendPrefixedMessage(CommandSender sender, String prefix, String message) {
        sender.sendMessage(colorize(prefix + message));
    }
    
    public static void sendPrefixedMessage(Player player, String prefix, String message) {
        player.sendMessage(colorize(prefix + message));
    }
    
    public static String formatMessage(String template, String... replacements) {
        String message = template;
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return message;
    }
} 