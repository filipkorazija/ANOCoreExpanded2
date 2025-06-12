package me.astonic.ANOC2.features;

import me.astonic.ANOC2.ANOCore2;
import me.astonic.ANOC2.utils.MessageUtil;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SlimeChunkChecker implements CommandExecutor {
    
    private final ANOCore2 plugin;
    private final String prefix;
    
    public SlimeChunkChecker(ANOCore2 plugin) {
        this.plugin = plugin;
        this.prefix = plugin.getConfigManager().getPrefix("slimechunk");
    }
    
    public void registerCommands() {
        plugin.getCommand("slime").setExecutor(this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, prefix + "&cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ano.slimecheck")) {
            MessageUtil.sendMessage(player, prefix + "&cYou don't have permission to use this command!");
            return true;
        }
        
        int radius = plugin.getConfig().getInt("slimechunk.radius", 5);
        int maxRadius = plugin.getConfig().getInt("slimechunk.max-radius", 10);
        
        // Parse custom radius if provided
        if (args.length > 0) {
            try {
                int customRadius = Integer.parseInt(args[0]);
                if (customRadius > maxRadius) {
                    MessageUtil.sendMessage(player, prefix + "&cMaximum radius is " + maxRadius + " chunks!");
                    return true;
                }
                if (customRadius < 1) {
                    MessageUtil.sendMessage(player, prefix + "&cRadius must be at least 1 chunk!");
                    return true;
                }
                radius = customRadius;
            } catch (NumberFormatException e) {
                MessageUtil.sendMessage(player, prefix + "&cInvalid radius! Please use a number.");
                return true;
            }
        }
        
        Chunk playerChunk = player.getLocation().getChunk();
        List<Chunk> slimeChunks = new ArrayList<>();
        
        // Check chunks in radius
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Chunk chunk = player.getWorld().getChunkAt(
                    playerChunk.getX() + x, 
                    playerChunk.getZ() + z
                );
                
                if (isSlimeChunk(chunk)) {
                    slimeChunks.add(chunk);
                }
            }
        }
        
        // Send results
        MessageUtil.sendMessage(player, prefix + "&aSlime chunk search results:");
        MessageUtil.sendMessage(player, "&7Searched " + ((radius * 2 + 1) * (radius * 2 + 1)) + " chunks in a " + radius + " chunk radius");
        
        if (slimeChunks.isEmpty()) {
            MessageUtil.sendMessage(player, "&cNo slime chunks found in the searched area!");
        } else {
            MessageUtil.sendMessage(player, "&aFound " + slimeChunks.size() + " slime chunk(s):");
            
            for (Chunk chunk : slimeChunks) {
                int distance = Math.max(
                    Math.abs(chunk.getX() - playerChunk.getX()),
                    Math.abs(chunk.getZ() - playerChunk.getZ())
                );
                
                String direction = getDirection(playerChunk, chunk);
                MessageUtil.sendMessage(player, 
                    "&7- Chunk (" + chunk.getX() + ", " + chunk.getZ() + ") " +
                    "&a" + distance + " chunks " + direction
                );
            }
            
            // Check if player is currently in a slime chunk
            if (isSlimeChunk(playerChunk)) {
                MessageUtil.sendMessage(player, "&a&lYou are currently standing in a slime chunk!");
            }
        }
        
        return true;
    }
    
    private boolean isSlimeChunk(Chunk chunk) {
        // Use Minecraft's slime chunk algorithm
        // This is the same algorithm used by the game to determine slime chunks
        Random rnd = new Random(
            chunk.getWorld().getSeed() +
            (long) (chunk.getX() * chunk.getX() * 0x4c1906) +
            (long) (chunk.getX() * 0x5ac0db) +
            (long) (chunk.getZ() * chunk.getZ()) * 0x4307a7L +
            (long) (chunk.getZ() * 0x5f24f) ^ 0x3ad76af0
        );
        
        return rnd.nextInt(10) == 0;
    }
    
    private String getDirection(Chunk from, Chunk to) {
        int deltaX = to.getX() - from.getX();
        int deltaZ = to.getZ() - from.getZ();
        
        if (deltaX == 0 && deltaZ == 0) {
            return "here";
        }
        
        String direction = "";
        
        if (deltaZ < 0) {
            direction += "North";
        } else if (deltaZ > 0) {
            direction += "South";
        }
        
        if (deltaX > 0) {
            direction += "East";
        } else if (deltaX < 0) {
            direction += "West";
        }
        
        return direction.toLowerCase();
    }
} 