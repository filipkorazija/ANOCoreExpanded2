package me.astonic.ANOC2.features;

import me.astonic.ANOC2.ANOCore2;
import me.astonic.ANOC2.graceperiod.GraceUser;
import me.astonic.ANOC2.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GracePeriod implements Listener, CommandExecutor, TabCompleter {
    
    private static final int DAYS_IN_SECOND = 86400;
    
    private final ANOCore2 plugin;
    private final String prefix;
    private final Map<UUID, BossBar> bossBarMap = new HashMap<>();
    private int timer = 0;
    
    public GracePeriod(ANOCore2 plugin) {
        this.plugin = plugin;
        this.prefix = plugin.getConfigManager().getPrefix("graceperiod");
        
        // Load existing grace period users
        GraceUser.load(plugin);
        
        // Start the boss bar task
        startBossBarTask();
    }
    
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void registerCommands() {
        plugin.getCommand("graceperiod").setExecutor(this);
        plugin.getCommand("graceperiod").setTabCompleter(this);
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player) ||
                !(event.getDamager() instanceof Player damager)) {
            return;
        }
        // Cancel the damage if either player is in grace period
        if (GraceUser.of(player) != null || GraceUser.of(damager) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (GraceUser.of(player) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeBossBar(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Check if it's the first time player joins the server
        if (player.hasPlayedBefore()) {
            return;
        }
        // Give grace period to the player
        GraceUser.register(player, plugin);
    }
    
    private void startBossBarTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Check the boss bar stuff
            for (UUID uuid : bossBarMap.keySet()) {
                if (GraceUser.of(uuid) == null) {
                    removeBossBar(uuid);
                }
            }
            
            // Reset the timer back to 0
            int titleSwitch = plugin.getConfig().getInt("graceperiod.title-switch", 30);
            if (timer >= titleSwitch * 2) {
                timer = 0;
            }
            
            for (GraceUser user : GraceUser.getUsers()) {
                if (user.getDuration() <= 0) {
                    removeBossBar(user.getUniqueId());
                    user.destroy();
                    continue;
                }
                
                // Get the bossbar progress
                int graceDuration = plugin.getConfig().getInt("graceperiod.grace-period-duration", 10);
                double progress = ((double) user.getDuration() / (double) TimeUnit.MINUTES.toSeconds(graceDuration));
                
                // Define the bar color
                BarColor color = BarColor.GREEN;
                if (progress > 0.3 && progress <= 0.5) {
                    color = BarColor.YELLOW;
                }
                if (progress >= 0.0 && progress < 0.3) {
                    color = BarColor.RED;
                }

                String title;
                if (timer > titleSwitch) {
                    title = MessageUtil.colorize(plugin.getConfig().getString("graceperiod.bossbar-title-additional", 
                        "&aUse &6/graceperiod end &ato bypass the grace period"));
                } else {
                    title = MessageUtil.colorize(plugin.getConfig().getString("graceperiod.bossbar-title", 
                        "&aGrace Period: &f{time}").replace("{time}", timeFormat(user.getDuration())));
                }

                // Create the bossbar here
                BossBar bar = bossBarMap.get(user.getUniqueId());
                if (bar == null) {
                    bar = Bukkit.createBossBar(title, color, BarStyle.SOLID);
                    bar.setProgress(progress);
                    bar.setVisible(true);
                    bossBarMap.put(user.getUniqueId(), bar);
                }
                
                // Update the bossbar
                bar.setTitle(title);
                bar.setColor(color);
                bar.setProgress(progress);
                
                // Add player so they can view the boss bar
                Player player = Bukkit.getPlayer(user.getUniqueId());
                if (player != null && !bar.getPlayers().contains(player)) {
                    bar.addPlayer(player);
                }
                user.decrease();
            }
            // Timer for dynamic bossbar title
            timer++;
        }, 0L, 20L);
    }
    
    private void removeBossBar(UUID uuid) {
        BossBar bar = bossBarMap.remove(uuid);
        if (bar != null) {
            bar.setVisible(false);
            bar.removeAll();
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ano.graceperiod")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "start":
                if (args.length != 2) {
                    MessageUtil.sendMessage(sender, prefix + "&cUsage: /graceperiod start <player>");
                    return true;
                }
                
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sendMessage(sender, "player-not-found");
                    return true;
                }
                
                if (GraceUser.of(target) != null) {
                    MessageUtil.sendMessage(sender, prefix + "&c" + target.getName() + " already has a grace period!");
                    return true;
                }
                
                GraceUser.register(target, plugin);
                sendMessage(sender, "start", "{player}", target.getName());
                break;
                
            case "end":
                if (!(sender instanceof Player)) {
                    MessageUtil.sendMessage(sender, prefix + "&cOnly players can end their own grace period!");
                    return true;
                }
                
                Player player = (Player) sender;
                GraceUser user = GraceUser.of(player);
                if (user == null) {
                    sendMessage(sender, "no-grace");
                    return true;
                }
                
                removeBossBar(player.getUniqueId());
                user.destroy();
                sendMessage(sender, "end");
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
        String fullPath = "graceperiod." + messageKey;
        
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
        String fullPath = "graceperiod." + messageKey;
        
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
            return Arrays.asList("start", "end", "reload");
        }
        return null;
    }
    
    public static String timeFormat(long remaining) {
        int days = toDays(remaining);
        int hours = toHours(remaining);
        int minutes = toMinutes(remaining);
        int seconds = toSeconds(remaining);
        StringBuilder builder = new StringBuilder();
        
        if (days != 0) {
            builder.append(days).append("d ");
        }
        if (hours != 0) {
            builder.append(hours).append("h ");
        }
        if (minutes != 0) {
            builder.append(minutes).append("m ");
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
    
    public void cleanup() {
        for (BossBar bar : bossBarMap.values()) {
            bar.setVisible(false);
            bar.removeAll();
        }
        bossBarMap.clear();
        GraceUser.save(plugin);
    }
} 