package me.astonic.ANOC2.managers;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {
    
    private final Map<String, Map<UUID, Long>> cooldowns;
    
    public CooldownManager() {
        this.cooldowns = new ConcurrentHashMap<>();
    }
    
    public void setCooldown(String type, UUID playerUuid, long durationMillis) {
        cooldowns.computeIfAbsent(type, k -> new ConcurrentHashMap<>())
                .put(playerUuid, System.currentTimeMillis() + durationMillis);
    }
    
    public boolean hasCooldown(String type, UUID playerUuid) {
        Map<UUID, Long> typeCooldowns = cooldowns.get(type);
        if (typeCooldowns == null) {
            return false;
        }
        
        Long endTime = typeCooldowns.get(playerUuid);
        if (endTime == null) {
            return false;
        }
        
        if (System.currentTimeMillis() >= endTime) {
            typeCooldowns.remove(playerUuid);
            return false;
        }
        
        return true;
    }
    
    public long getRemainingCooldown(String type, UUID playerUuid) {
        Map<UUID, Long> typeCooldowns = cooldowns.get(type);
        if (typeCooldowns == null) {
            return 0;
        }
        
        Long endTime = typeCooldowns.get(playerUuid);
        if (endTime == null) {
            return 0;
        }
        
        long remaining = endTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    public void removeCooldown(String type, UUID playerUuid) {
        Map<UUID, Long> typeCooldowns = cooldowns.get(type);
        if (typeCooldowns != null) {
            typeCooldowns.remove(playerUuid);
        }
    }
    
    public String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    public void cleanup() {
        // Remove expired cooldowns
        long currentTime = System.currentTimeMillis();
        for (Map<UUID, Long> typeCooldowns : cooldowns.values()) {
            typeCooldowns.entrySet().removeIf(entry -> entry.getValue() <= currentTime);
        }
    }
} 