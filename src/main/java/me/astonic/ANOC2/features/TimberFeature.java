package me.astonic.ANOC2.features;

import me.astonic.ANOC2.ANOCore2;
import me.astonic.ANOC2.managers.TimberManager;
import me.astonic.ANOC2.utils.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class TimberFeature implements Listener, CommandExecutor {
    
    private final ANOCore2 plugin;
    private final TimberManager timberManager;
    private final String prefix;
    
    public TimberFeature(ANOCore2 plugin, TimberManager timberManager) {
        this.plugin = plugin;
        this.timberManager = timberManager;
        this.prefix = plugin.getConfigManager().getPrefix("timber");
    }
    
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void registerCommands() {
        plugin.getCommand("timber").setExecutor(this);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        if (!plugin.getConfig().getBoolean("timber.enabled", true)) {
            return;
        }
        
        Block block = event.getBlock();
        if (timberManager.isLogMaterial(block.getType())) {
            // Track player-placed log blocks
            timberManager.addPlayerPlacedBlock(event.getPlayer().getUniqueId(), block.getLocation());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        if (!plugin.getConfig().getBoolean("timber.enabled", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // Check if timber is enabled for this player
        if (!plugin.getDataManager().isTimberEnabled(player.getUniqueId())) {
            return;
        }
        
        // Check if it's a log block
        if (!timberManager.isLogMaterial(block.getType())) {
            return;
        }
        
        // Check if player is in creative mode (optional restriction)
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        
        // Check if this is a player-placed block and should be ignored
        if (plugin.getConfig().getBoolean("timber.ignore-player-placed", true) &&
            timberManager.isPlayerPlaced(player.getUniqueId(), block.getLocation())) {
            timberManager.removePlayerPlacedBlock(player.getUniqueId(), block.getLocation());
            return;
        }
        
        // Check if player is in allowed world
        List<String> allowedWorlds = plugin.getConfig().getStringList("timber.allowed-worlds");
        if (!allowedWorlds.isEmpty() && !allowedWorlds.contains(player.getWorld().getName())) {
            return;
        }
        
        // Check if player is holding an axe
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!isAxe(tool.getType())) {
            return;
        }
        
        // Get all connected logs
        int maxLogs = plugin.getConfig().getInt("timber.max-logs", 200);
        List<Block> connectedLogs = timberManager.getConnectedLogs(block, maxLogs);
        
        if (connectedLogs.size() > 1) {
            // Cancel the original event since we'll handle it manually
            event.setCancelled(true);
            
            // Break all connected logs
            breakLogsWithTool(player, connectedLogs, tool);
            
            // Send message to player
            MessageUtil.sendMessage(player, prefix + "&aFelled tree with &e" + connectedLogs.size() + "&a logs!");
        }
    }
    
    private void breakLogsWithTool(Player player, List<Block> logs, ItemStack tool) {
        for (Block log : logs) {
            // Remove from player-placed tracking
            timberManager.removePlayerPlacedBlock(player.getUniqueId(), log.getLocation());
            
            // Drop the block naturally
            log.breakNaturally(tool);
            
            // Damage the tool
            if (tool.getType().getMaxDurability() > 0) {
                short durability = tool.getDurability();
                durability++;
                
                if (durability >= tool.getType().getMaxDurability()) {
                    // Tool broke
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    MessageUtil.sendMessage(player, prefix + "&cYour axe broke!");
                    break;
                } else {
                    tool.setDurability(durability);
                }
            }
        }
    }
    
    private boolean isAxe(Material material) {
        return material == Material.WOODEN_AXE ||
               material == Material.STONE_AXE ||
               material == Material.IRON_AXE ||
               material == Material.GOLDEN_AXE ||
               material == Material.DIAMOND_AXE ||
               material == Material.NETHERITE_AXE;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, prefix + "&cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        boolean currentState = plugin.getDataManager().isTimberEnabled(player.getUniqueId());
        boolean newState = !currentState;
        
        plugin.getDataManager().setTimberEnabled(player.getUniqueId(), newState);
        
        if (newState) {
            MessageUtil.sendMessage(player, prefix + "&aTimber mode &aenabled&a! Trees will be cut down completely when you break a log with an axe.");
        } else {
            MessageUtil.sendMessage(player, prefix + "&cTimber mode &cdisabled&c! Trees will be cut normally.");
        }
        
        return true;
    }
} 