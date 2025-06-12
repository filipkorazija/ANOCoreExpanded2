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

import java.util.Set;
import java.util.HashSet;

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
        
        // Find adjacent door
        Block adjacentDoor = findAdjacentDoor(doorBlock);
        if (adjacentDoor == null) {
            return;
        }
        
        // Get door data
        Door doorData = (Door) doorBlock.getBlockData();
        Door adjacentDoorData = (Door) adjacentDoor.getBlockData();
        
        // Check if doors are configured as a double door
        if (!areDoorsAdjacent(doorBlock, adjacentDoor, doorData, adjacentDoorData)) {
            return;
        }
        
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
            
            // Set proper hinge for double door behavior
            Door.Hinge properHinge = getProperHingeForAdjacentDoor(mainDoor, adjacentDoor, mainDoorData);
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
    
    private Block findAdjacentDoor(Block doorBlock) {
        // Check all horizontal directions
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Block adjacent = doorBlock.getRelative(face);
            if (isDoor(adjacent.getType())) {
                Block bottomAdjacent = getBottomDoorBlock(adjacent);
                if (bottomAdjacent != null) {
                    return bottomAdjacent;
                }
            }
        }
        
        return null;
    }
    
    private boolean areDoorsAdjacent(Block door1, Block door2, Door door1Data, Door door2Data) {
        // Get the facing directions
        BlockFace facing1 = door1Data.getFacing();
        BlockFace facing2 = door2Data.getFacing();
        
        // For proper double doors, they should be facing the same direction
        if (facing1 != facing2) {
            return false;
        }
        
        // Check if doors are positioned correctly for double door setup
        BlockFace direction = getRelativeDirection(door1, door2);
        
        // Doors should be adjacent (not diagonal)
        return direction == BlockFace.NORTH || direction == BlockFace.SOUTH || 
               direction == BlockFace.EAST || direction == BlockFace.WEST;
    }
    
    private Door.Hinge getProperHingeForAdjacentDoor(Block mainDoor, Block adjacentDoor, Door mainDoorData) {
        BlockFace direction = getRelativeDirection(mainDoor, adjacentDoor);
        BlockFace doorFacing = mainDoorData.getFacing();
        Door.Hinge mainHinge = mainDoorData.getHinge();
        
        // For proper double door behavior, determine hinge based on relative positions
        // This ensures doors open away from each other (creating a passage in the middle)
        
        // If doors are side by side (perpendicular to their facing direction)
        if ((doorFacing == BlockFace.NORTH || doorFacing == BlockFace.SOUTH) && 
            (direction == BlockFace.EAST || direction == BlockFace.WEST)) {
            
            // East-West alignment with North-South facing doors
            if (direction == BlockFace.EAST) {
                // Adjacent door is to the east, should have opposite hinge
                return mainHinge == Door.Hinge.LEFT ? Door.Hinge.RIGHT : Door.Hinge.LEFT;
            } else {
                // Adjacent door is to the west, should have opposite hinge  
                return mainHinge == Door.Hinge.LEFT ? Door.Hinge.RIGHT : Door.Hinge.LEFT;
            }
            
        } else if ((doorFacing == BlockFace.EAST || doorFacing == BlockFace.WEST) && 
                   (direction == BlockFace.NORTH || direction == BlockFace.SOUTH)) {
            
            // North-South alignment with East-West facing doors
            if (direction == BlockFace.SOUTH) {
                // Adjacent door is to the south, should have opposite hinge
                return mainHinge == Door.Hinge.LEFT ? Door.Hinge.RIGHT : Door.Hinge.LEFT;
            } else {
                // Adjacent door is to the north, should have opposite hinge
                return mainHinge == Door.Hinge.LEFT ? Door.Hinge.RIGHT : Door.Hinge.LEFT;
            }
        }
        
        // Default: opposite hinge for proper double door behavior
        return mainHinge == Door.Hinge.LEFT ? Door.Hinge.RIGHT : Door.Hinge.LEFT;
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
} 