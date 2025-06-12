package me.astonic.ANOC2.features;

import me.astonic.ANOC2.ANOCore2;
import me.astonic.ANOC2.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeleportWithEntities implements Listener {
    
    private final ANOCore2 plugin;
    private final String prefix;
    private final Map<UUID, Entity> playerVehicles;
    
    public TeleportWithEntities(ANOCore2 plugin) {
        this.plugin = plugin;
        this.prefix = plugin.getConfigManager().getPrefix("teleportwithentities");
        this.playerVehicles = new HashMap<>();
    }
    
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        if (!plugin.getConfig().getBoolean("teleportwithentities.enabled", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Check if player is in a vehicle
        if (!player.isInsideVehicle()) {
            return;
        }
        
        Entity vehicle = player.getVehicle();
        if (vehicle == null) {
            return;
        }
        
        // Check if it's a valid vehicle type (boats, horses, etc.)
        if (!isValidVehicle(vehicle)) {
            return;
        }
        
        // Get all passengers in the vehicle
        List<Entity> passengers = vehicle.getPassengers();
        if (passengers.isEmpty() || !passengers.contains(player)) {
            return;
        }
        
        // Check entity limit
        int maxEntities = plugin.getConfig().getInt("teleportwithentities.max-entities", 5);
        if (passengers.size() > maxEntities) {
            MessageUtil.sendMessage(player, prefix + "&cToo many entities in vehicle! Maximum: " + maxEntities);
            return;
        }
        
        Location destination = event.getTo();
        if (destination == null) {
            return;
        }
        
        // Schedule the vehicle and entity teleportation
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                // Remove all passengers first
                for (Entity passenger : passengers) {
                    vehicle.removePassenger(passenger);
                }
                
                // Teleport the vehicle
                vehicle.teleport(destination);
                
                // Teleport and re-add all passengers
                for (Entity passenger : passengers) {
                    if (passenger instanceof Player) {
                        // Player is already teleported by the event
                        continue;
                    }
                    
                    // Teleport other entities
                    passenger.teleport(destination);
                    
                    // Re-add passenger to vehicle
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        if (vehicle.isValid() && passenger.isValid()) {
                            vehicle.addPassenger(passenger);
                        }
                    }, 2L);
                }
                
                // Re-add the player to the vehicle
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (vehicle.isValid() && player.isOnline()) {
                        vehicle.addPassenger(player);
                        
                        // Send success message
                        MessageUtil.sendMessage(player, prefix + "&aTeleported with your vehicle and " + 
                            (passengers.size() - 1) + " other entities!");
                    }
                }, 3L);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Error teleporting vehicle with entities: " + e.getMessage());
                MessageUtil.sendMessage(player, prefix + "&cError occurred while teleporting with entities!");
            }
        }, 1L);
    }
    
    private boolean isValidVehicle(Entity entity) {
        // Check if the entity is a valid vehicle type
        String entityType = entity.getType().name();
        
        // Boats
        if (entityType.contains("BOAT") || entityType.contains("CHEST_BOAT")) {
            return true;
        }
        
        // Horses and similar
        if (entityType.contains("HORSE") || 
            entityType.equals("DONKEY") || 
            entityType.equals("MULE") || 
            entityType.equals("LLAMA") ||
            entityType.equals("TRADER_LLAMA")) {
            return true;
        }
        
        // Pigs with saddle
        if (entityType.equals("PIG") && entity instanceof Vehicle) {
            return true;
        }
        
        // Striders
        if (entityType.equals("STRIDER")) {
            return true;
        }
        
        // Camels (1.20+)
        if (entityType.equals("CAMEL")) {
            return true;
        }
        
        // Minecarts
        if (entityType.contains("MINECART")) {
            return true;
        }
        
        return false;
    }
} 