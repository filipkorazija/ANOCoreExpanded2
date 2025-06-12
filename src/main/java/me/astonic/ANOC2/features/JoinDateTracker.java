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
        
        // Record join date if this is the first time
        if (!plugin.getDataManager().hasJoinDate(player.getUniqueId())) {
            plugin.getDataManager().setJoinDate(player.getUniqueId(), new Date());
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ano.joindate")) {
            MessageUtil.sendMessage(sender, prefix + "&cYou don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            // Check own join date
            if (!(sender instanceof Player)) {
                MessageUtil.sendMessage(sender, prefix + "&cOnly players can check their own join date!");
                return true;
            }
            
            Player player = (Player) sender;
            Date joinDate = plugin.getDataManager().getJoinDate(player.getUniqueId());
            
            if (joinDate == null) {
                MessageUtil.sendMessage(player, prefix + "&cYour join date is not recorded!");
                return true;
            }
            
            MessageUtil.sendMessage(player, prefix + "&aYou first joined the server on: &e" + dateFormat.format(joinDate));
            
        } else if (args.length == 1) {
            // Check another player's join date
            String targetName = args[0];
            
            // Try to get online player first
            Player targetPlayer = Bukkit.getPlayer(targetName);
            if (targetPlayer != null) {
                Date joinDate = plugin.getDataManager().getJoinDate(targetPlayer.getUniqueId());
                
                if (joinDate == null) {
                    MessageUtil.sendMessage(sender, prefix + "&cJoin date for " + targetPlayer.getName() + " is not recorded!");
                    return true;
                }
                
                MessageUtil.sendMessage(sender, prefix + "&a" + targetPlayer.getName() + " first joined the server on: &e" + dateFormat.format(joinDate));
                return true;
            }
            
            // Try offline player
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (offlinePlayer.hasPlayedBefore()) {
                Date joinDate = plugin.getDataManager().getJoinDate(offlinePlayer.getUniqueId());
                
                if (joinDate == null) {
                    MessageUtil.sendMessage(sender, prefix + "&cJoin date for " + offlinePlayer.getName() + " is not recorded!");
                    return true;
                }
                
                MessageUtil.sendMessage(sender, prefix + "&a" + offlinePlayer.getName() + " first joined the server on: &e" + dateFormat.format(joinDate));
            } else {
                MessageUtil.sendMessage(sender, prefix + "&cPlayer '" + targetName + "' has never played on this server!");
            }
            
        } else {
            MessageUtil.sendMessage(sender, prefix + "&cUsage: /joindate [player]");
        }
        
        return true;
    }
} 