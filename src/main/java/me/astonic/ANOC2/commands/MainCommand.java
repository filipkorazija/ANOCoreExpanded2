package me.astonic.ANOC2.commands;

import me.astonic.ANOC2.ANOCore2;
import me.astonic.ANOC2.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {
    
    private final ANOCore2 plugin;
    
    public MainCommand(ANOCore2 plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ano.admin")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reloadConfig();
                plugin.getMessageManager().reloadMessages();
                sendMessage(sender, "reload-success");
                break;
                
            case "version":
                sendMessage(sender, "version-info", "{version}", plugin.getDescription().getVersion());
                sendMessage(sender, "author-info");
                break;
                
            default:
                sendHelpMessage(sender);
                break;
        }
        
        return true;
    }
    
    /**
     * Sends a configurable message with placeholder replacement
     */
    private void sendMessage(CommandSender sender, String messageKey, String... placeholders) {
        String fullPath = "anocore2." + messageKey;
        String message = plugin.getMessageManager().getMessage(fullPath, placeholders);
        MessageUtil.sendMessage(sender, message);
    }
    
    private void sendHelpMessage(CommandSender sender) {
        MessageUtil.sendMessage(sender, "");
        MessageUtil.sendMessage(sender, "&6&l=== ANOCore2 Help ===");
        MessageUtil.sendMessage(sender, "");
        MessageUtil.sendMessage(sender, "&e&lCore Features:");
        MessageUtil.sendMessage(sender, "&6/slime [radius] &7- Check for slime chunks");
        MessageUtil.sendMessage(sender, "&6/joindate [player] &7- View first join date");
        MessageUtil.sendMessage(sender, "&6/timber &7- Toggle timber mode");
        MessageUtil.sendMessage(sender, "");
        MessageUtil.sendMessage(sender, "&e&lBoost Features:");
        MessageUtil.sendMessage(sender, "&6/xpboost <start|end|check|reload> &7- Manage XP boosts");
        MessageUtil.sendMessage(sender, "&6/graceperiod <start|end|reload> &7- Manage grace periods");
        MessageUtil.sendMessage(sender, "");
        MessageUtil.sendMessage(sender, "&e&lPotion Effects:");
        MessageUtil.sendMessage(sender, "&6/speed, /jump_boost, /regeneration &7- Get potion effects");
        MessageUtil.sendMessage(sender, "&6/strength, /instant_health, /night_vision &7- And many more!");
        MessageUtil.sendMessage(sender, "");
        MessageUtil.sendMessage(sender, "&e&lUtility Features:");
        MessageUtil.sendMessage(sender, "&6/biometp &7- Teleport to biomes");
        MessageUtil.sendMessage(sender, "&7Double doors, dragon wings, teleport with entities");
        MessageUtil.sendMessage(sender, "");
        MessageUtil.sendMessage(sender, "&e&lAdmin Commands:");
        MessageUtil.sendMessage(sender, "&6/anocore2 reload &7- Reload configuration");
        MessageUtil.sendMessage(sender, "&6/anocore2 version &7- Show plugin version");
        MessageUtil.sendMessage(sender, "");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("help", "reload", "version");
        }
        return null;
    }
} 