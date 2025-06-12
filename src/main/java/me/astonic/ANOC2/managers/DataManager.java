package me.astonic.ANOC2.managers;

import me.astonic.ANOC2.ANOCore2;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {
    
    private final ANOCore2 plugin;
    private final Map<String, Object> playerData;
    private final Map<UUID, Date> joinDates;
    private final Set<UUID> timberEnabled;
    
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public DataManager(ANOCore2 plugin) {
        this.plugin = plugin;
        this.playerData = new ConcurrentHashMap<>();
        this.joinDates = new ConcurrentHashMap<>();
        this.timberEnabled = new HashSet<>();
        
        setupDataFile();
        loadData();
    }
    
    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerdata.yml file!");
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    private void loadData() {
        // Load join dates
        if (dataConfig.contains("joinDates")) {
            for (String uuidString : dataConfig.getConfigurationSection("joinDates").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    long timestamp = dataConfig.getLong("joinDates." + uuidString);
                    joinDates.put(uuid, new Date(timestamp));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in playerdata: " + uuidString);
                }
            }
        }
        
        // Load timber settings
        if (dataConfig.contains("timber")) {
            List<String> timberList = dataConfig.getStringList("timber");
            for (String uuidString : timberList) {
                try {
                    timberEnabled.add(UUID.fromString(uuidString));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in timber data: " + uuidString);
                }
            }
        }
    }
    
    public void saveAllData() {
        // Save join dates
        for (Map.Entry<UUID, Date> entry : joinDates.entrySet()) {
            dataConfig.set("joinDates." + entry.getKey().toString(), entry.getValue().getTime());
        }
        
        // Save timber settings
        List<String> timberList = new ArrayList<>();
        for (UUID uuid : timberEnabled) {
            timberList.add(uuid.toString());
        }
        dataConfig.set("timber", timberList);
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerdata.yml!");
            e.printStackTrace();
        }
    }
    
    // Join date methods
    public void setJoinDate(UUID playerUuid, Date date) {
        joinDates.put(playerUuid, date);
    }
    
    public Date getJoinDate(UUID playerUuid) {
        return joinDates.get(playerUuid);
    }
    
    public boolean hasJoinDate(UUID playerUuid) {
        return joinDates.containsKey(playerUuid);
    }
    
    // Timber methods
    public void setTimberEnabled(UUID playerUuid, boolean enabled) {
        if (enabled) {
            timberEnabled.add(playerUuid);
        } else {
            timberEnabled.remove(playerUuid);
        }
    }
    
    public boolean isTimberEnabled(UUID playerUuid) {
        return timberEnabled.contains(playerUuid);
    }
    
    // Generic data methods
    public void setPlayerData(String key, Object value) {
        playerData.put(key, value);
    }
    
    public Object getPlayerData(String key) {
        return playerData.get(key);
    }
    
    public boolean hasPlayerData(String key) {
        return playerData.containsKey(key);
    }
} 