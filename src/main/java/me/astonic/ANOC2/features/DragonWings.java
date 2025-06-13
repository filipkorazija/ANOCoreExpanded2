package me.astonic.ANOC2.features;

import me.astonic.ANOC2.ANOCore2;
import me.astonic.ANOC2.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DragonWings implements Listener {
    
    private final ANOCore2 plugin;
    private final String prefix;
    
    public DragonWings(ANOCore2 plugin) {
        this.plugin = plugin;
        this.prefix = plugin.getConfigManager().getPrefix("dragonwings");
    }
    
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEnderDragonDeath(EntityDeathEvent event) {
        if (!plugin.getConfig().getBoolean("dragonwings.enabled", true)) {
            return;
        }
        
        // Check if the entity is an Ender Dragon
        if (!(event.getEntity() instanceof EnderDragon)) {
            return;
        }
        
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        
        // Check if player has permission
        if (!killer.hasPermission("ano.dragonelytra")) {
            sendMessage(killer, "no-permission");
            return;
        }
        
        // Create the custom reward item
        ItemStack rewardItem = createRewardItem();
        if (rewardItem == null) {
            plugin.getLogger().warning("Could not create dragon reward item - invalid material configured!");
            return;
        }
        
        String itemName = getItemDisplayName(rewardItem);
        
        // Try to give the item to the player
        if (killer.getInventory().firstEmpty() != -1) {
            killer.getInventory().addItem(rewardItem);
        } else {
            // Drop it at the player's location if inventory is full
            killer.getWorld().dropItemNaturally(killer.getLocation(), rewardItem);
            sendMessage(killer, "inventory-full", "{item_name}", itemName);
        }
        
        // Send reward message to the killer
        sendMessage(killer, "reward-received", "{item_name}", itemName);
        
        // Broadcast message if enabled
        if (plugin.getConfig().getBoolean("dragonwings.broadcast-enabled", true)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                sendMessage(onlinePlayer, "broadcast-message", 
                    "{player}", killer.getName(),
                    "{item_name}", itemName);
            }
        }
    }
    
    private ItemStack createRewardItem() {
        // Get material from config
        String materialName = plugin.getConfig().getString("dragonwings.reward-item.material", "ELYTRA");
        Material material;
        
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material configured for dragon wings: " + materialName);
            return null;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set the display name
            String itemName = plugin.getConfig().getString("dragonwings.reward-item.name", "&5&lDragon Wings");
            meta.setDisplayName(MessageUtil.colorize(itemName));
            
            // Set lore
            List<String> configLore = plugin.getConfig().getStringList("dragonwings.reward-item.lore");
            if (!configLore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : configLore) {
                    coloredLore.add(MessageUtil.colorize(line));
                }
                meta.setLore(coloredLore);
            }
            
            // Apply enchantments
            if (plugin.getConfig().contains("dragonwings.reward-item.enchantments")) {
                for (String enchantName : plugin.getConfig().getConfigurationSection("dragonwings.reward-item.enchantments").getKeys(false)) {
                    try {
                        Enchantment enchantment = Enchantment.getByName(enchantName.toUpperCase());
                        if (enchantment != null) {
                            int level = plugin.getConfig().getInt("dragonwings.reward-item.enchantments." + enchantName, 1);
                            meta.addEnchant(enchantment, level, true);
                        } else {
                            plugin.getLogger().warning("Invalid enchantment configured: " + enchantName);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error applying enchantment " + enchantName + ": " + e.getMessage());
                    }
                }
            }
            
            // Add glow effect if configured
            if (plugin.getConfig().getBoolean("dragonwings.reward-item.glow", false)) {
                // Add a harmless enchantment and hide it to create glow effect
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Gets the display name of an item for use in messages
     */
    private String getItemDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        
        // Fallback to config name or material name
        String configName = plugin.getConfig().getString("dragonwings.reward-item.name", null);
        if (configName != null) {
            return MessageUtil.colorize(configName);
        }
        
        return item.getType().name().toLowerCase().replace("_", " ");
    }
    
    /**
     * Sends a configurable message with placeholder replacement
     */
    private void sendMessage(Player player, String messageKey, String... placeholders) {
        // Create the full path to the message in messages.yml
        String fullPath = "dragonwings." + messageKey;
        
        // Add prefix to placeholders
        String[] allPlaceholders = new String[placeholders.length + 2];
        allPlaceholders[0] = "{prefix}";
        allPlaceholders[1] = prefix;
        System.arraycopy(placeholders, 0, allPlaceholders, 2, placeholders.length);
        
        String message = plugin.getMessageManager().getMessage(fullPath, allPlaceholders);
        MessageUtil.sendMessage(player, message);
    }
} 