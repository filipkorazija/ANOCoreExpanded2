package me.astonic.ANOC2.features;

import me.astonic.ANOC2.ANOCore2;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Set;
import java.util.HashSet;

public class DoubleDoors implements Listener {
    
    private final ANOCore2 plugin;
    private final Set<Material> doorMaterials;
    
    public DoubleDoors(ANOCore2 plugin) {
        this.plugin = plugin;
        this.doorMaterials = new HashSet<>();
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
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
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
        
        // Find adjacent door
        Block adjacentDoor = findAdjacentDoor(doorBlock);
        if (adjacentDoor == null) {
            return;
        }
        
        // Get door data
        Door doorData = (Door) doorBlock.getBlockData();
        Door adjacentDoorData = (Door) adjacentDoor.getBlockData();
        
        // Check if doors are facing each other (double door configuration)
        if (!areDoorsAdjacent(doorData, adjacentDoorData)) {
            return;
        }
        
        // Schedule the adjacent door to open/close after the current door
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                // Get current state of the clicked door
                Door currentDoorData = (Door) doorBlock.getBlockData();
                
                // Determine the proper hinge position for the adjacent door
                Door.Hinge adjacentHinge = getProperHingeForAdjacentDoor(doorBlock, adjacentDoor, currentDoorData);
                
                // Set adjacent door to same open state but with proper hinge
                adjacentDoorData.setOpen(currentDoorData.isOpen());
                adjacentDoorData.setHinge(adjacentHinge);
                adjacentDoor.setBlockData(adjacentDoorData);
                
                // Also update the top half
                Block adjacentTopHalf = adjacentDoor.getRelative(BlockFace.UP);
                if (isDoor(adjacentTopHalf.getType())) {
                    Door topData = (Door) adjacentTopHalf.getBlockData();
                    topData.setOpen(currentDoorData.isOpen());
                    topData.setHinge(adjacentHinge);
                    adjacentTopHalf.setBlockData(topData);
                }
                
                // Play sound effect
                if (currentDoorData.isOpen()) {
                    doorBlock.getWorld().playSound(doorBlock.getLocation(), 
                        getDoorOpenSound(doorBlock.getType()), 1.0f, 1.0f);
                } else {
                    doorBlock.getWorld().playSound(doorBlock.getLocation(), 
                        getDoorCloseSound(doorBlock.getType()), 1.0f, 1.0f);
                }
                
            } catch (Exception e) {
                plugin.getLogger().warning("Error synchronizing double doors: " + e.getMessage());
            }
        }, 1L); // 1 tick delay
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
                if (bottomAdjacent != null && bottomAdjacent.equals(adjacent)) {
                    return adjacent;
                }
            }
        }
        
        return null;
    }
    
    private boolean areDoorsAdjacent(Door door1, Door door2) {
        // Check if doors are configured as a double door
        BlockFace facing1 = door1.getFacing();
        BlockFace facing2 = door2.getFacing();
        
        // For proper double doors, they should be facing the same direction
        // The hinge positions will determine how they open
        return facing1 == facing2;
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
    
    private Door.Hinge getProperHingeForAdjacentDoor(Block mainDoor, Block adjacentDoor, Door mainDoorData) {
        // Determine the relative position of the adjacent door
        BlockFace direction = getRelativeDirection(mainDoor, adjacentDoor);
        BlockFace doorFacing = mainDoorData.getFacing();
        Door.Hinge mainHinge = mainDoorData.getHinge();
        
        // For proper double door behavior, adjacent doors should have opposite hinges
        // This ensures they open away from each other (vanilla behavior)
        
        if (direction == BlockFace.NORTH || direction == BlockFace.SOUTH) {
            // Doors are aligned north-south
            if (doorFacing == BlockFace.EAST || doorFacing == BlockFace.WEST) {
                // Doors face east/west, so they're side by side
                return mainHinge == Door.Hinge.LEFT ? Door.Hinge.RIGHT : Door.Hinge.LEFT;
            }
        } else if (direction == BlockFace.EAST || direction == BlockFace.WEST) {
            // Doors are aligned east-west
            if (doorFacing == BlockFace.NORTH || doorFacing == BlockFace.SOUTH) {
                // Doors face north/south, so they're side by side
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
} 