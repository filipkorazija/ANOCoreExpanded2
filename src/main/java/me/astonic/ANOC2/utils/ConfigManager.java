package me.astonic.ANOC2.utils;

import me.astonic.ANOC2.ANOCore2;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final ANOCore2 plugin;
    
    public ConfigManager(ANOCore2 plugin) {
        this.plugin = plugin;
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
    }
    
    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }
    
    public String getPrefix(String feature) {
        return MessageUtil.colorize(getConfig().getString(feature + ".prefix", "&7[ANOCore2]&r "));
    }
    
    public boolean isFeatureEnabled(String feature) {
        return getConfig().getBoolean(feature + ".enabled", true);
    }
    
    public int getInt(String path, int defaultValue) {
        return getConfig().getInt(path, defaultValue);
    }
    
    public double getDouble(String path, double defaultValue) {
        return getConfig().getDouble(path, defaultValue);
    }
    
    public String getString(String path, String defaultValue) {
        return getConfig().getString(path, defaultValue);
    }
    
    public boolean getBoolean(String path, boolean defaultValue) {
        return getConfig().getBoolean(path, defaultValue);
    }
} 