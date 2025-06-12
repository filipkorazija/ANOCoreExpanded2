package me.astonic.ANOC2.managers;

import me.astonic.ANOC2.ANOCore2;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TimberManager {
    
    private final ANOCore2 plugin;
    private final Set<Material> logMaterials;
    private final Set<Material> leafMaterials;
    private final Map<UUID, Set<Location>> playerPlacedBlocks;
    
    public TimberManager(ANOCore2 plugin) {
        this.plugin = plugin;
        this.logMaterials = new HashSet<>();
        this.leafMaterials = new HashSet<>();
        this.playerPlacedBlocks = new ConcurrentHashMap<>();
        
        initializeMaterials();
    }
    
    private void initializeMaterials() {
        // Add all log materials
        logMaterials.add(Material.OAK_LOG);
        logMaterials.add(Material.BIRCH_LOG);
        logMaterials.add(Material.SPRUCE_LOG);
        logMaterials.add(Material.JUNGLE_LOG);
        logMaterials.add(Material.ACACIA_LOG);
        logMaterials.add(Material.DARK_OAK_LOG);
        logMaterials.add(Material.CHERRY_LOG);
        logMaterials.add(Material.MANGROVE_LOG);
        logMaterials.add(Material.BAMBOO_BLOCK);
        
        // Add stripped log materials
        logMaterials.add(Material.STRIPPED_OAK_LOG);
        logMaterials.add(Material.STRIPPED_BIRCH_LOG);
        logMaterials.add(Material.STRIPPED_SPRUCE_LOG);
        logMaterials.add(Material.STRIPPED_JUNGLE_LOG);
        logMaterials.add(Material.STRIPPED_ACACIA_LOG);
        logMaterials.add(Material.STRIPPED_DARK_OAK_LOG);
        logMaterials.add(Material.STRIPPED_CHERRY_LOG);
        logMaterials.add(Material.STRIPPED_MANGROVE_LOG);
        logMaterials.add(Material.STRIPPED_BAMBOO_BLOCK);
        
        // Add wood materials
        logMaterials.add(Material.OAK_WOOD);
        logMaterials.add(Material.BIRCH_WOOD);
        logMaterials.add(Material.SPRUCE_WOOD);
        logMaterials.add(Material.JUNGLE_WOOD);
        logMaterials.add(Material.ACACIA_WOOD);
        logMaterials.add(Material.DARK_OAK_WOOD);
        logMaterials.add(Material.CHERRY_WOOD);
        logMaterials.add(Material.MANGROVE_WOOD);
        
        // Add stripped wood materials
        logMaterials.add(Material.STRIPPED_OAK_WOOD);
        logMaterials.add(Material.STRIPPED_BIRCH_WOOD);
        logMaterials.add(Material.STRIPPED_SPRUCE_WOOD);
        logMaterials.add(Material.STRIPPED_JUNGLE_WOOD);
        logMaterials.add(Material.STRIPPED_ACACIA_WOOD);
        logMaterials.add(Material.STRIPPED_DARK_OAK_WOOD);
        logMaterials.add(Material.STRIPPED_CHERRY_WOOD);
        logMaterials.add(Material.STRIPPED_MANGROVE_WOOD);
        
        // Add leaf materials
        leafMaterials.add(Material.OAK_LEAVES);
        leafMaterials.add(Material.BIRCH_LEAVES);
        leafMaterials.add(Material.SPRUCE_LEAVES);
        leafMaterials.add(Material.JUNGLE_LEAVES);
        leafMaterials.add(Material.ACACIA_LEAVES);
        leafMaterials.add(Material.DARK_OAK_LEAVES);
        leafMaterials.add(Material.CHERRY_LEAVES);
        leafMaterials.add(Material.MANGROVE_LEAVES);
    }
    
    public boolean isLogMaterial(Material material) {
        return logMaterials.contains(material);
    }
    
    public boolean isLeafMaterial(Material material) {
        return leafMaterials.contains(material);
    }
    
    public void addPlayerPlacedBlock(UUID playerUuid, Location location) {
        if (plugin.getConfig().getBoolean("timber.ignore-player-placed", true)) {
            playerPlacedBlocks.computeIfAbsent(playerUuid, k -> new HashSet<>()).add(location);
        }
    }
    
    public boolean isPlayerPlaced(UUID playerUuid, Location location) {
        Set<Location> playerBlocks = playerPlacedBlocks.get(playerUuid);
        return playerBlocks != null && playerBlocks.contains(location);
    }
    
    public void removePlayerPlacedBlock(UUID playerUuid, Location location) {
        Set<Location> playerBlocks = playerPlacedBlocks.get(playerUuid);
        if (playerBlocks != null) {
            playerBlocks.remove(location);
        }
    }
    
    public List<Block> getConnectedLogs(Block startBlock, int maxLogs) {
        List<Block> logs = new ArrayList<>();
        Set<Location> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        
        queue.add(startBlock);
        visited.add(startBlock.getLocation());
        
        while (!queue.isEmpty() && logs.size() < maxLogs) {
            Block block = queue.poll();
            logs.add(block);
            
            // Check all 6 directions
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        
                        Block relative = block.getRelative(x, y, z);
                        Location relativeLocation = relative.getLocation();
                        
                        if (!visited.contains(relativeLocation) && 
                            isLogMaterial(relative.getType()) &&
                            logs.size() < maxLogs) {
                            
                            visited.add(relativeLocation);
                            queue.add(relative);
                        }
                    }
                }
            }
        }
        
        return logs;
    }
    
    public void cleanup() {
        playerPlacedBlocks.clear();
    }
} 