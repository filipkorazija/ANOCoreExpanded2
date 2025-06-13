package me.astonic.ANOC2.features;

import me.astonic.ANOC2.ANOCore2;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class DoubleDoors implements Listener {
    
    private final ANOCore2 plugin;
    private final Set<Material> doorMaterials;
    private final Set<Block> processingDoors; // Track doors being processed to prevent infinite loops
    
    public DoubleDoors(ANOCore2 plugin) {
        this.plugin = plugin;
        this.doorMaterials = new HashSet<>();
        this.processingDoors = new HashSet<>();
        initializeDoorMaterials();
    }
    
    private void initializeDoorMaterials() {
        doorMaterials.add(Material.OAK_DOOR);
        doorMaterials.add(Material.BIRCH_DOOR);
        doorMaterials.add(Material.SPRUCE_DOOR);
        doorMaterials.add(Material.JUNGLE_DOOR);
        doorMaterials.add(Material.ACACIA_DOOR);
        doorMaterials.add(Material.DARK_OAK_DOOR);
        doorMaterials.add(Material.CHERRY_DOOR);
        doorMaterials.add(Material.MANGROVE_DOOR);
        doorMaterials.add(Material.BAMBOO_DOOR);
        doorMaterials.add(Material.CRIMSON_DOOR);
        doorMaterials.add(Material.WARPED_DOOR);
        doorMaterials.add(Material.IRON_DOOR);
        doorMaterials.add(Material.COPPER_DOOR);
        doorMaterials.add(Material.EXPOSED_COPPER_DOOR);
        doorMaterials.add(Material.WEATHERED_COPPER_DOOR);
        doorMaterials.add(Material.OXIDIZED_COPPER_DOOR);
        doorMaterials.add(Material.WAXED_COPPER_DOOR);
        doorMaterials.add(Material.WAXED_EXPOSED_COPPER_DOOR);
        doorMaterials.add(Material.WAXED_WEATHERED_COPPER_DOOR);
        doorMaterials.add(Material.WAXED_OXIDIZED_COPPER_DOOR);
    }
    
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        if (!plugin.getConfig().getBoolean("doubledoors.enabled", true)) {
            return;
        }
        
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !isDoor(clickedBlock.getType())) {
            return;
        }
        
        // Get the bottom part of the door
        Block doorBlock = getBottomDoorBlock(clickedBlock);
        if (doorBlock == null) {
            return;
        }
        
        // Check if this door is already being processed
        if (processingDoors.contains(doorBlock)) {
            return;
        }
        
        // Find the best adjacent door for double door behavior
        Block adjacentDoor = findBestAdjacentDoor(doorBlock);
        if (adjacentDoor == null) {
            debugLog("No suitable adjacent door found for door at " + doorBlock.getLocation());
            return;
        }
        
        // Get door data
        Door doorData = (Door) doorBlock.getBlockData();
        Door adjacentDoorData = (Door) adjacentDoor.getBlockData();
        
        // Validate doors form a proper double door configuration
        if (!isValidDoubleDoorPair(doorBlock, adjacentDoor, doorData, adjacentDoorData)) {
            debugLog("Doors at " + doorBlock.getLocation() + " and " + adjacentDoor.getLocation() + " are not a valid double door pair");
            return;
        }
        
        debugLog("Synchronizing double doors at " + doorBlock.getLocation() + " and " + adjacentDoor.getLocation());
        
        // Mark both doors as being processed
        processingDoors.add(doorBlock);
        processingDoors.add(adjacentDoor);
        
        // Schedule the adjacent door synchronization after the main door has been processed
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                synchronizeAdjacentDoor(doorBlock, adjacentDoor);
            } finally {
                // Remove from processing set
                processingDoors.remove(doorBlock);
                processingDoors.remove(adjacentDoor);
            }
        });
    }
    
    private void synchronizeAdjacentDoor(Block mainDoor, Block adjacentDoor) {
        try {
            // Get current door states
            Door mainDoorData = (Door) mainDoor.getBlockData();
            Door adjacentDoorData = (Door) adjacentDoor.getBlockData();
            
            // Only sync if doors have different open states
            if (mainDoorData.isOpen() == adjacentDoorData.isOpen()) {
                return;
            }
            
            // Set adjacent door to same open state as main door
            adjacentDoorData.setOpen(mainDoorData.isOpen());
            
            // Set proper hinge for double door behavior using improved logic
            Door.Hinge properHinge = calculateOptimalHinge(mainDoor, adjacentDoor, mainDoorData, adjacentDoorData);
            adjacentDoorData.setHinge(properHinge);
            
            // Apply changes to bottom half
            adjacentDoor.setBlockData(adjacentDoorData);
            
            // Also update the top half
            Block adjacentTopHalf = adjacentDoor.getRelative(BlockFace.UP);
            if (isDoor(adjacentTopHalf.getType())) {
                Door topData = (Door) adjacentTopHalf.getBlockData();
                topData.setOpen(mainDoorData.isOpen());
                topData.setHinge(properHinge);
                adjacentTopHalf.setBlockData(topData);
            }
            
            // Play sound effect
            if (mainDoorData.isOpen()) {
                adjacentDoor.getWorld().playSound(adjacentDoor.getLocation(), 
                    getDoorOpenSound(adjacentDoor.getType()), 0.5f, 1.0f);
            } else {
                adjacentDoor.getWorld().playSound(adjacentDoor.getLocation(), 
                    getDoorCloseSound(adjacentDoor.getType()), 0.5f, 1.0f);
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error synchronizing double doors: " + e.getMessage());
        }
    }
    
    /**
     * Finds the best adjacent door for double door behavior with improved logic
     */
    private Block findBestAdjacentDoor(Block doorBlock) {
        Door mainDoorData = (Door) doorBlock.getBlockData();
        List<DoorCandidate> candidates = new ArrayList<>();
        
        // Check all horizontal directions
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Block adjacent = doorBlock.getRelative(face);
            if (isDoor(adjacent.getType())) {
                Block bottomAdjacent = getBottomDoorBlock(adjacent);
                if (bottomAdjacent != null && !processingDoors.contains(bottomAdjacent)) {
                    Door adjacentDoorData = (Door) bottomAdjacent.getBlockData();
                    
                    // Calculate priority score for this candidate
                    int score = calculateDoorPairScore(doorBlock, bottomAdjacent, mainDoorData, adjacentDoorData);
                    if (score > 0) {
                        candidates.add(new DoorCandidate(bottomAdjacent, score));
                    }
                }
            }
        }
        
        // Return the highest-scoring candidate
        return candidates.stream()
                .max(Comparator.comparingInt(c -> c.score))
                .map(c -> c.door)
                .orElse(null);
    }
    
    /**
     * Calculates a score for how suitable two doors are as a double door pair
     */
    private int calculateDoorPairScore(Block door1, Block door2, Door door1Data, Door door2Data) {
        int score = 0;
        
        // Same material gets highest priority
        if (door1.getType() == door2.getType()) {
            score += 100;
        } else {
            // Different materials but same category (wood vs metal) gets some points
            if (isWoodenDoor(door1.getType()) == isWoodenDoor(door2.getType())) {
                score += 50;
            }
        }
        
        // Same facing direction is required for proper double doors
        if (door1Data.getFacing() == door2Data.getFacing()) {
            score += 200;
        } else {
            return 0; // Different facing directions are not valid double doors
        }
        
        // Check if doors are positioned correctly relative to their facing direction
        BlockFace direction = getRelativeDirection(door1, door2);
        BlockFace doorFacing = door1Data.getFacing();
        
        // For proper double doors, they should be perpendicular to their facing direction
        if (isPerpendicularAlignment(doorFacing, direction)) {
            score += 150;
        } else {
            return 0; // Not a valid double door alignment
        }
        
        // Prefer doors that already have complementary hinges
        if (door1Data.getHinge() != door2Data.getHinge()) {
            score += 75;
        }
        
        // Prefer doors with same open state (less synchronization needed)
        if (door1Data.isOpen() == door2Data.isOpen()) {
            score += 25;
        }
        
        return score;
    }
    
    /**
     * Validates if two doors form a proper double door configuration
     */
    private boolean isValidDoubleDoorPair(Block door1, Block door2, Door door1Data, Door door2Data) {
        // Must be facing the same direction
        if (door1Data.getFacing() != door2Data.getFacing()) {
            return false;
        }
        
        // Must be positioned correctly for double door behavior
        BlockFace direction = getRelativeDirection(door1, door2);
        BlockFace doorFacing = door1Data.getFacing();
        
        if (!isPerpendicularAlignment(doorFacing, direction)) {
            return false;
        }
        
        // Check if there are walls or blocks supporting the door frame
        if (plugin.getConfig().getBoolean("doubledoors.require-frame", false)) {
            return hasProperDoorFrame(door1, door2, door1Data);
        }
        
        return true;
    }
    
    /**
     * Calculates the optimal hinge for the adjacent door using improved logic
     */
    private Door.Hinge calculateOptimalHinge(Block mainDoor, Block adjacentDoor, 
                                           Door mainDoorData, Door adjacentDoorData) {
        BlockFace direction = getRelativeDirection(mainDoor, adjacentDoor);
        BlockFace doorFacing = mainDoorData.getFacing();
        Door.Hinge mainHinge = mainDoorData.getHinge();
        
        // Check if we should preserve existing hinges for natural behavior
        if (plugin.getConfig().getBoolean("doubledoors.preserve-hinges", false)) {
            // If doors already have complementary hinges, keep them
            if (mainHinge != adjacentDoorData.getHinge()) {
                return adjacentDoorData.getHinge();
            }
        }
        
        // Calculate optimal hinge based on position and facing direction
        // This creates doors that open away from each other (standard double door behavior)
        
        if (doorFacing == BlockFace.NORTH || doorFacing == BlockFace.SOUTH) {
            // North-South facing doors
            if (direction == BlockFace.EAST) {
                // Adjacent door is to the east - should open towards the gap
                return Door.Hinge.LEFT;
            } else if (direction == BlockFace.WEST) {
                // Adjacent door is to the west - should open towards the gap  
                return Door.Hinge.RIGHT;
            }
        } else if (doorFacing == BlockFace.EAST || doorFacing == BlockFace.WEST) {
            // East-West facing doors
            if (direction == BlockFace.NORTH) {
                // Adjacent door is to the north - should open towards the gap
                return Door.Hinge.RIGHT;
            } else if (direction == BlockFace.SOUTH) {
                // Adjacent door is to the south - should open towards the gap
                return Door.Hinge.LEFT;
            }
        }
        
        // Fallback: opposite hinge to main door
        return mainHinge == Door.Hinge.LEFT ? Door.Hinge.RIGHT : Door.Hinge.LEFT;
    }
    
    /**
     * Checks if the door alignment is perpendicular to the facing direction
     */
    private boolean isPerpendicularAlignment(BlockFace doorFacing, BlockFace direction) {
        if (doorFacing == BlockFace.NORTH || doorFacing == BlockFace.SOUTH) {
            return direction == BlockFace.EAST || direction == BlockFace.WEST;
        } else if (doorFacing == BlockFace.EAST || doorFacing == BlockFace.WEST) {
            return direction == BlockFace.NORTH || direction == BlockFace.SOUTH;
        }
        return false;
    }
    
    /**
     * Checks if doors have a proper frame structure around them
     */
    private boolean hasProperDoorFrame(Block door1, Block door2, Door doorData) {
        BlockFace facing = doorData.getFacing();
        BlockFace[] checkFaces = {facing, facing.getOppositeFace()};
        
        // Check if there are solid blocks behind and in front of both doors
        for (BlockFace face : checkFaces) {
            Block behind1 = door1.getRelative(face);
            Block behind2 = door2.getRelative(face);
            
            // At least one side should have supporting blocks
            if (!behind1.getType().isSolid() && !behind2.getType().isSolid()) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isWoodenDoor(Material material) {
        return !material.name().contains("IRON") && !material.name().contains("COPPER");
    }
    
    /**
     * Helper class for door candidate scoring
     */
    private static class DoorCandidate {
        final Block door;
        final int score;
        
        DoorCandidate(Block door, int score) {
            this.door = door;
            this.score = score;
        }
    }
    
    private boolean isDoor(Material material) {
        return doorMaterials.contains(material);
    }
    
    private Block getBottomDoorBlock(Block block) {
        if (!isDoor(block.getType())) {
            return null;
        }
        
        Door doorData = (Door) block.getBlockData();
        if (doorData.getHalf() == Bisected.Half.BOTTOM) {
            return block;
        } else {
            return block.getRelative(BlockFace.DOWN);
        }
    }
    
    private BlockFace getRelativeDirection(Block from, Block to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        
        if (dx > 0) return BlockFace.EAST;
        if (dx < 0) return BlockFace.WEST;
        if (dz > 0) return BlockFace.SOUTH;
        if (dz < 0) return BlockFace.NORTH;
        
        return BlockFace.SELF; // Same position (shouldn't happen)
    }
    
    private org.bukkit.Sound getDoorOpenSound(Material doorMaterial) {
        if (doorMaterial == Material.IRON_DOOR || doorMaterial.name().contains("COPPER")) {
            return org.bukkit.Sound.BLOCK_IRON_DOOR_OPEN;
        }
        return org.bukkit.Sound.BLOCK_WOODEN_DOOR_OPEN;
    }
    
    private org.bukkit.Sound getDoorCloseSound(Material doorMaterial) {
        if (doorMaterial == Material.IRON_DOOR || doorMaterial.name().contains("COPPER")) {
            return org.bukkit.Sound.BLOCK_IRON_DOOR_CLOSE;
        }
        return org.bukkit.Sound.BLOCK_WOODEN_DOOR_CLOSE;
    }
    
    /**
     * Logs debug messages when debug mode is enabled
     */
    private void debugLog(String message) {
        if (plugin.getConfig().getBoolean("doubledoors.debug", false)) {
            plugin.getLogger().info("[DoubleDoors Debug] " + message);
        }
    }
} 