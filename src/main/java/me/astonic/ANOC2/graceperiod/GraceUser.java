package me.astonic.ANOC2.graceperiod;

import me.astonic.ANOC2.ANOCore2;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GraceUser {

    private static final Map<UUID, GraceUser> userMap = new HashMap<>();

    public static void register(Player player, ANOCore2 plugin) {
        int duration = plugin.getConfig().getInt("graceperiod.grace-period-duration", 10);
        userMap.put(player.getUniqueId(), new GraceUser(player.getUniqueId(), (int) TimeUnit.MINUTES.toSeconds(duration)));
    }

    public static GraceUser of(OfflinePlayer player) {
        return of(player.getUniqueId());
    }

    public static GraceUser of(UUID uuid) {
        return userMap.get(uuid);
    }

    public static void load(ANOCore2 plugin) {
        FileConfiguration config = plugin.getDataManager().getPlayerData("graceperiod") instanceof FileConfiguration ? 
            (FileConfiguration) plugin.getDataManager().getPlayerData("graceperiod") : null;
        
        plugin.getLogger().info("Starting to load grace period user data...");

        if (config == null || !config.isConfigurationSection("data")) {
            plugin.getLogger().info("No grace period data found!");
            return;
        }

        for (String uuidString : config.getConfigurationSection("data").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidString);
            int duration = config.getInt("data." + uuidString + ".duration");

            userMap.put(uuid, new GraceUser(uuid, duration));
        }

        plugin.getLogger().info("Successfully loaded " + userMap.size() + " grace period users!");
    }

    public static void save(ANOCore2 plugin) {
        plugin.getLogger().info("Starting to save grace period user data...");
        
        // Save to ANOCore2's data manager
        Map<String, Object> graceData = new HashMap<>();
        for (GraceUser user : userMap.values()) {
            graceData.put("data." + user.getUniqueId() + ".duration", user.getDuration());
        }
        
        plugin.getDataManager().setPlayerData("graceperiod", graceData);
        plugin.getLogger().info("Successfully saved " + userMap.size() + " grace period users!");
    }

    public static Collection<GraceUser> getUsers() {
        return userMap.values();
    }

    public static void removeUser(UUID uuid) {
        userMap.remove(uuid);
    }

    private final UUID uuid;
    private int duration;

    public GraceUser(UUID uuid, int duration) {
        this.uuid = uuid;
        this.duration = duration;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public int getDuration() {
        return duration;
    }

    public void decrease() {
        this.duration--;
    }

    public void destroy() {
        userMap.remove(this.uuid);
    }
} 