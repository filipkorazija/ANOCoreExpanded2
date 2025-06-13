package me.astonic.ANOC2.features;

import me.astonic.ANOC2.ANOCore2;
import me.astonic.ANOC2.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class JoinDateTracker implements CommandExecutor, Listener {
    
    private final ANOCore2 plugin;
    private final String prefix;
    private final SimpleDateFormat dateFormat;
    
    public JoinDateTracker(ANOCore2 plugin) {
        this.plugin = plugin;
        this.prefix = plugin.getConfigManager().getPrefix("joindate");
        
        String formatString = plugin.getConfig().getString("joindate.date-format", "yyyy-MM-dd HH:mm:ss");
        this.dateFormat = new SimpleDateFormat(formatString);
    }
    
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void registerCommands() {
        plugin.getCommand("joindate").setExecutor(this);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Still record join date for potential future use/backup
        // But we'll primarily use Bukkit's getFirstPlayed() method
        if (!plugin.getDataManager().hasJoinDate(player.getUniqueId())) {
            plugin.getDataManager().setJoinDate(player.getUniqueId(), new Date());
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ano.joindate")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length == 0) {
            // Check own join date
            if (!(sender instanceof Player)) {
                sendMessage(sender, "console-usage");
                return true;
            }
            
            Player player = (Player) sender;
            Date joinDate = getActualFirstJoinDate(player);
            
            if (joinDate == null) {
                sendMessage(sender, "no-data", "{player}", player.getName());
                return true;
            }
            
            long daysAgo = calculateDaysAgo(joinDate);
            sendMessage(player, "own-join-date",
                "{date}", dateFormat.format(joinDate),
                "{days_ago}", String.valueOf(daysAgo));
            
        } else if (args.length == 1) {
            // Check another player's join date
            String targetName = args[0];
            
            // Try to get online player first
            Player targetPlayer = Bukkit.getPlayer(targetName);
            OfflinePlayer offlinePlayer = targetPlayer != null ? targetPlayer : Bukkit.getOfflinePlayer(targetName);
            
            if (!offlinePlayer.hasPlayedBefore()) {
                sendMessage(sender, "player-not-found", "{player}", targetName);
                return true;
            }
            
            Date joinDate = getActualFirstJoinDate(offlinePlayer);
            
            if (joinDate == null) {
                sendMessage(sender, "no-data", "{player}", offlinePlayer.getName());
                return true;
            }
            
            long daysAgo = calculateDaysAgo(joinDate);
            sendMessage(sender, "other-join-date",
                "{player}", offlinePlayer.getName(),
                "{date}", dateFormat.format(joinDate),
                "{days_ago}", String.valueOf(daysAgo));
            
        } else {
            sendMessage(sender, "usage");
        }
        
        return true;
    }
    
    /**
     * Gets the actual first join date using Bukkit's built-in tracking
     */
    private Date getActualFirstJoinDate(OfflinePlayer player) {
        long firstPlayed = player.getFirstPlayed();
        
        // If no first played time is recorded, fall back to our tracking
        if (firstPlayed == 0 || firstPlayed == -1) {
            return plugin.getDataManager().getJoinDate(player.getUniqueId());
        }
        
        return new Date(firstPlayed);
    }
    
    /**
     * Calculates how many days ago the join date was
     */
    private long calculateDaysAgo(Date joinDate) {
        long currentTime = System.currentTimeMillis();
        long joinTime = joinDate.getTime();
        long diffInMillis = currentTime - joinTime;
        return TimeUnit.MILLISECONDS.toDays(diffInMillis);
    }
    
    /**
     * Sends a configurable message with placeholder replacement
     */
    private void sendMessage(CommandSender sender, String messageKey, String... placeholders) {
        // Create the full path to the message in messages.yml
        String fullPath = "joindate." + messageKey;
        
        // Add prefix to placeholders
        String[] allPlaceholders = new String[placeholders.length + 2];
        allPlaceholders[0] = "{prefix}";
        allPlaceholders[1] = prefix;
        System.arraycopy(placeholders, 0, allPlaceholders, 2, placeholders.length);
        
        String message = plugin.getMessageManager().getMessage(fullPath, allPlaceholders);
        MessageUtil.sendMessage(sender, message);
    }
} 