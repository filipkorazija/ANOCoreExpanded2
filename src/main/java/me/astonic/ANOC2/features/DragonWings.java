package me.astonic.ANOC2.features;

import me.astonic.ANOC2.ANOCore2;
import me.astonic.ANOC2.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

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
            return;
        }
        
        // Create the custom elytra
        ItemStack elytra = createDragonWings();
        
        // Try to give the elytra to the player
        if (killer.getInventory().firstEmpty() != -1) {
            killer.getInventory().addItem(elytra);
        } else {
            // Drop it at the player's location if inventory is full
            killer.getWorld().dropItemNaturally(killer.getLocation(), elytra);
            MessageUtil.sendMessage(killer, prefix + "&cYour inventory is full! Dragon Wings dropped on the ground.");
        }
        
        // Send message to the killer
        MessageUtil.sendMessage(killer, prefix + "&aCongratulations! You have earned &5&lDragon Wings &afor slaying the Ender Dragon!");
        
        // Broadcast message if enabled
        if (plugin.getConfig().getBoolean("dragonwings.broadcast-enabled", true)) {
            String broadcastMessage = plugin.getConfig().getString("dragonwings.broadcast-message", 
                "&6{player} &fhas slain the Ender Dragon and earned their &5&lDragon Wings&f!");
            broadcastMessage = broadcastMessage.replace("{player}", killer.getName());
            
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                MessageUtil.sendMessage(onlinePlayer, broadcastMessage);
            }
        }
    }
    
    private ItemStack createDragonWings() {
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemMeta meta = elytra.getItemMeta();
        
        if (meta != null) {
            // Set the display name
            String elytraName = plugin.getConfig().getString("dragonwings.elytra-name", "&5&lDragon Wings");
            meta.setDisplayName(MessageUtil.colorize(elytraName));
            
            // Set lore
            meta.setLore(Arrays.asList(
                MessageUtil.colorize("&7Earned by slaying the Ender Dragon"),
                MessageUtil.colorize("&7A symbol of triumph and courage"),
                MessageUtil.colorize("&5&oMay your flights be swift and high")
            ));
            
            // Apply unbreaking enchantment for durability
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 3, true);
            
            elytra.setItemMeta(meta);
        }
        
        return elytra;
    }
} 