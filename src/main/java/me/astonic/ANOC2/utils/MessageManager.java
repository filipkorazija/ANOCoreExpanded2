package me.astonic.ANOC2.utils;

import me.astonic.ANOC2.ANOCore2;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class MessageManager {
    
    private final ANOCore2 plugin;
    private File messagesFile;
    private FileConfiguration messagesConfig;
    
    public MessageManager(ANOCore2 plugin) {
        this.plugin = plugin;
        setupMessagesFile();
        loadMessages();
    }
    
    private void setupMessagesFile() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        // Create messages.yml if it doesn't exist
        if (!messagesFile.exists()) {
            try {
                // Create the plugin data folder if it doesn't exist
                if (!plugin.getDataFolder().exists()) {
                    plugin.getDataFolder().mkdirs();
                }
                
                // Copy the default messages.yml from resources
                InputStream defaultMessages = plugin.getResource("messages.yml");
                if (defaultMessages != null) {
                    Files.copy(defaultMessages, messagesFile.toPath());
                } else {
                    // Create empty file if resource doesn't exist
                    messagesFile.createNewFile();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create messages.yml file!");
                e.printStackTrace();
            }
        }
    }
    
    private void loadMessages() {
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    /**
     * Reloads the messages configuration from file
     */
    public void reloadMessages() {
        loadMessages();
    }
    
    /**
     * Gets a message from the messages.yml file with placeholder replacement
     */
    public String getMessage(String path, String... placeholders) {
        String message = messagesConfig.getString(path);
        
        if (message == null) {
            plugin.getLogger().warning("Message not found: " + path);
            return "&cMessage not found: " + path;
        }
        
        // Replace placeholders
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        
        return message;
    }
    
    /**
     * Gets a list of messages from the messages.yml file with placeholder replacement
     */
    public List<String> getMessageList(String path, String... placeholders) {
        List<String> messages = messagesConfig.getStringList(path);
        
        if (messages.isEmpty()) {
            plugin.getLogger().warning("Message list not found: " + path);
            return List.of("&cMessage list not found: " + path);
        }
        
        // Replace placeholders in each message
        for (int i = 0; i < messages.size(); i++) {
            String message = messages.get(i);
            for (int j = 0; j < placeholders.length; j += 2) {
                if (j + 1 < placeholders.length) {
                    message = message.replace(placeholders[j], placeholders[j + 1]);
                }
            }
            messages.set(i, message);
        }
        
        return messages;
    }
    
    /**
     * Checks if a message path exists in the configuration
     */
    public boolean hasMessage(String path) {
        return messagesConfig.contains(path);
    }
    
    /**
     * Gets the raw messages configuration
     */
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
} 