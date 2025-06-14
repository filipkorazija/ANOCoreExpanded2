package me.astonic.ANOC2.features;

import me.astonic.ANOC2.ANOCore2;
import me.astonic.ANOC2.utils.MessageUtil;
import me.astonic.ANOC2.xpboost.ActiveBooster;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XPBoost implements Listener, CommandExecutor, TabCompleter {
    
    private static final int DAYS_IN_SECOND = 86400;
    private static final DecimalFormat decimalFormat = new DecimalFormat("###,###,###,###,###.##");
    
    private final ANOCore2 plugin;
    private final String prefix;
    private final List<ActiveBooster> boosters = new ArrayList<>();
    private BukkitTask task;
    
    public XPBoost(ANOCore2 plugin) {
        this.plugin = plugin;
        this.prefix = plugin.getConfigManager().getPrefix("xpboost");
        
        // Load existing boosters
        loadBoosters();
        
        // Start the boost task
        startBoostTask();
    }
    
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void registerCommands() {
        plugin.getCommand("xpboost").setExecutor(this);
        plugin.getCommand("xpboost").setTabCompleter(this);
    }
    
    private void loadBoosters() {
        // Load from ANOCore2's data manager using global data storage
        Object boosterData = plugin.getDataManager().getGlobalData("xpboost");
        if (boosterData instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> boosterList = (List<String>) boosterData;
            for (String boosterStr : boosterList) {
                String[] parts = boosterStr.split(":");
                if (parts.length == 2) {
                    try {
                        double multiplier = Double.parseDouble(parts[0]);
                        int duration = Integer.parseInt(parts[1]);
                        boosters.add(new ActiveBooster(multiplier, duration));
                        plugin.getLogger().info("Loaded XP booster: " + multiplier + "x for " + duration + " seconds");
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Invalid booster data: " + boosterStr);
                    }
                }
            }
        }
        
        if (!boosters.isEmpty()) {
            plugin.getLogger().info("Loaded " + boosters.size() + " active XP boosters from save data");
        }
    }
    
    private void saveBoosters() {
        List<String> boosterList = new ArrayList<>();
        for (ActiveBooster booster : boosters) {
            boosterList.add(booster.getMultiplier() + ":" + booster.getDuration());
        }
        plugin.getDataManager().setGlobalData("xpboost", boosterList);
        
        // Force save to disk immediately
        plugin.getDataManager().saveAllData();
        
        if (!boosters.isEmpty()) {
            plugin.getLogger().info("Saved " + boosters.size() + " active XP boosters to disk");
        }
    }
    
    private void startBoostTask() {
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (boosters.isEmpty()) {
                return;
            }

            boosters.get(0).decreaseDuration();
            boosters.removeIf(booster -> booster.getDuration() <= 0);

            // Send action bar to players
            String actionBar = plugin.getConfig().getString("xpboost.action-bar", "&a&lXP Boost: {multiplier}x &7({duration})")
                    .replace("{multiplier}", textFormat(getTotalMultiplier()))
                    .replace("{duration}", timeFormat(getTotalDuration()));
                    
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                    TextComponent.fromLegacyText(MessageUtil.colorize(actionBar)));
            }
        }, 0L, 20L);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        if (!boosters.isEmpty()) {
            double modified = event.getAmount() * getTotalMultiplier();
            event.setAmount((int) Math.round(modified));
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ano.xpboost")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "start":
                if (args.length != 3) {
                    MessageUtil.sendMessage(sender, prefix + "&cUsage: /xpboost start <multiplier> <duration>");
                    return true;
                }
                
                if (!boosters.isEmpty()) {
                    sendMessage(sender, "in-progress");
                    return true;
                }
                
                try {
                    double multiplier = Double.parseDouble(args[1]);
                    int duration = Integer.parseInt(args[2]);
                    
                    if (multiplier <= 0 || duration <= 0) {
                        MessageUtil.sendMessage(sender, prefix + "&cMultiplier and duration must be positive!");
                        return true;
                    }
                    
                    boosters.add(new ActiveBooster(multiplier, duration));
                    saveBoosters();
                    
                    List<String> startMessages = sendMessageList(sender, "start", 
                        "{multiplier}", String.valueOf(multiplier),
                        "{duration}", timeFormat(duration));
                    
                    // Broadcast to all players
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        sendMessageList(player, "start", 
                            "{multiplier}", String.valueOf(multiplier),
                            "{duration}", timeFormat(duration));
                    }
                    
                } catch (NumberFormatException e) {
                    sendMessage(sender, "not-int");
                }
                break;
                
            case "end":
                if (boosters.isEmpty()) {
                    sendMessage(sender, "no-active-boost");
                    return true;
                }
                
                boosters.clear();
                saveBoosters();
                sendMessage(sender, "end");
                break;
                
            case "check":
                if (boosters.isEmpty()) {
                    sendMessage(sender, "no-active-boost");
                    return true;
                }
                
                sendMessageList(sender, "check",
                    "{multiplier}", textFormat(getTotalMultiplier()),
                    "{duration}", timeFormat(getTotalDuration()));
                break;
                
            case "reload":
                plugin.reloadConfig();
                plugin.getMessageManager().reloadMessages();
                sendMessage(sender, "reload");
                break;
                
            default:
                sendHelpMessage(sender);
                break;
        }
        
        return true;
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sendMessageList(sender, "help");
    }
    
    /**
     * Sends a configurable message with placeholder replacement
     */
    private void sendMessage(CommandSender sender, String messageKey, String... placeholders) {
        // Create the full path to the message in messages.yml
        String fullPath = "xpboost." + messageKey;
        
        // Add prefix to placeholders
        String[] allPlaceholders = new String[placeholders.length + 2];
        allPlaceholders[0] = "{prefix}";
        allPlaceholders[1] = prefix;
        System.arraycopy(placeholders, 0, allPlaceholders, 2, placeholders.length);
        
        String message = plugin.getMessageManager().getMessage(fullPath, allPlaceholders);
        MessageUtil.sendMessage(sender, message);
    }
    
    /**
     * Sends a configurable message list with placeholder replacement
     */
    private List<String> sendMessageList(CommandSender sender, String messageKey, String... placeholders) {
        // Create the full path to the message in messages.yml
        String fullPath = "xpboost." + messageKey;
        
        // Add prefix to placeholders
        String[] allPlaceholders = new String[placeholders.length + 2];
        allPlaceholders[0] = "{prefix}";
        allPlaceholders[1] = prefix;
        System.arraycopy(placeholders, 0, allPlaceholders, 2, placeholders.length);
        
        List<String> messages = plugin.getMessageManager().getMessageList(fullPath, allPlaceholders);
        for (String message : messages) {
            MessageUtil.sendMessage(sender, message);
        }
        return messages;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "end", "check", "reload");
        }
        return new ArrayList<>();
    }
    
    public static String textFormat(Object o) {
        return decimalFormat.format(o);
    }
    
    public static String timeFormat(long remaining) {
        int days = toDays(remaining);
        int hours = toHours(remaining);
        int minutes = toMinutes(remaining);
        int seconds = toSeconds(remaining);
        StringBuilder builder = new StringBuilder();
        
        if (days != 0) {
            builder.append(days).append("d");
            if (hours != 0) builder.append(" ");
        }
        if (hours != 0) {
            builder.append(hours).append("h");
            if (minutes != 0) builder.append(" ");
        }
        if (minutes != 0) {
            builder.append(minutes).append("m");
            if (seconds != 0) builder.append(" ");
        }
        if (seconds != 0) {
            builder.append(seconds).append("s");
        }
        
        return builder.toString();
    }
    
    private static int toDays(long remaining) {
        return (int) (remaining / DAYS_IN_SECOND);
    }
    
    private static int toHours(long remaining) {
        return (int) ((remaining % DAYS_IN_SECOND) / 3600);
    }
    
    private static int toMinutes(long remaining) {
        return (int) (((remaining % DAYS_IN_SECOND) % 3600) / 60);
    }
    
    private static int toSeconds(long remaining) {
        return (int) (((remaining % DAYS_IN_SECOND) % 3600) % 60);
    }
    
    public double getTotalMultiplier() {
        double multiplier = 0;
        for (ActiveBooster booster : boosters) {
            multiplier += booster.getMultiplier();
        }
        return multiplier;
    }
    
    public int getTotalDuration() {
        int duration = 0;
        for (ActiveBooster booster : boosters) {
            duration += booster.getDuration();
        }
        return duration;
    }
    
    public void cleanup() {
        if (task != null) {
            task.cancel();
        }
        saveBoosters();
    }
} 