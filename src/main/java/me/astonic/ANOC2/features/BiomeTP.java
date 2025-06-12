package me.astonic.ANOC2.features;

import me.astonic.ANOC2.ANOCore2;
import me.astonic.ANOC2.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.*;

public class BiomeTP implements Listener, CommandExecutor {
    
    private final ANOCore2 plugin;
    private final Object economy; // Using Object to avoid Vault dependency
    private final String prefix;
    
    // Available biomes for teleportation
    private final Map<Biome, ItemStack> biomeItems = new HashMap<>();
    private final Map<Player, Inventory> playerInventories = new HashMap<>();
    
    public BiomeTP(ANOCore2 plugin, Object economy) {
        this.plugin = plugin;
        this.economy = economy;
        this.prefix = plugin.getConfigManager().getPrefix("biometp");
        
        initializeBiomeItems();
    }
    
    private void initializeBiomeItems() {
        // Initialize biome items for GUI
        biomeItems.put(Biome.PLAINS, createBiomeItem(Material.GRASS_BLOCK, "&aPlains", "A vast expanse of grass and flowers"));
        biomeItems.put(Biome.FOREST, createBiomeItem(Material.OAK_LOG, "&2Forest", "Dense woodlands full of trees"));
        biomeItems.put(Biome.DESERT, createBiomeItem(Material.SAND, "&eDesert", "Hot, dry, sandy wasteland"));
        biomeItems.put(Biome.JUNGLE, createBiomeItem(Material.JUNGLE_LOG, "&2Jungle", "Thick tropical rainforest"));
        biomeItems.put(Biome.TAIGA, createBiomeItem(Material.SPRUCE_LOG, "&3Taiga", "Cold northern forest"));
        biomeItems.put(Biome.SWAMP, createBiomeItem(Material.LILY_PAD, "&8Swamp", "Murky wetlands"));
        biomeItems.put(Biome.OCEAN, createBiomeItem(Material.WATER_BUCKET, "&bOcean", "Deep blue waters"));
        biomeItems.put(Biome.MUSHROOM_FIELDS, createBiomeItem(Material.RED_MUSHROOM, "&dMushroom Fields", "Mystical mushroom island"));
        biomeItems.put(Biome.ICE_SPIKES, createBiomeItem(Material.ICE, "&bIce Spikes", "Frozen wasteland with ice spikes"));
        biomeItems.put(Biome.BADLANDS, createBiomeItem(Material.TERRACOTTA, "&6Badlands", "Mesa with colorful clay"));
        biomeItems.put(Biome.SAVANNA, createBiomeItem(Material.ACACIA_LOG, "&eSavanna", "African-style grassland"));
        biomeItems.put(Biome.DARK_FOREST, createBiomeItem(Material.DARK_OAK_LOG, "&8Dark Forest", "Dense, dark woodland"));
    }
    
    private ItemStack createBiomeItem(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.colorize(name));
            meta.setLore(Arrays.asList(
                MessageUtil.colorize("&7" + description),
                MessageUtil.colorize("&8"),
                MessageUtil.colorize("&eCost: &6$" + plugin.getConfig().getDouble("biome-tp.cost", 100.0)),
                MessageUtil.colorize("&8Click to teleport!")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void registerCommands() {
        plugin.getCommand("biometp").setExecutor(this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, prefix + "&cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!plugin.getConfig().getBoolean("biome-tp.enabled", true)) {
            MessageUtil.sendMessage(player, prefix + "&cBiome teleportation is currently disabled!");
            return true;
        }
        
        if (!player.hasPermission("ano.biometp")) {
            MessageUtil.sendMessage(player, prefix + "&cYou don't have permission to use this command!");
            return true;
        }
        
        openBiomeGUI(player);
        return true;
    }
    
    private void openBiomeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, MessageUtil.colorize("&6Biome Teleportation"));
        
        int slot = 0;
        for (Map.Entry<Biome, ItemStack> entry : biomeItems.entrySet()) {
            if (slot >= 27) break;
            gui.setItem(slot, entry.getValue());
            slot++;
        }
        
        // Add close button
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(MessageUtil.colorize("&cClose"));
            closeMeta.setLore(Arrays.asList(MessageUtil.colorize("&7Click to close this menu")));
            closeButton.setItemMeta(closeMeta);
        }
        gui.setItem(26, closeButton);
        
        playerInventories.put(player, gui);
        player.openInventory(gui);
        
        MessageUtil.sendMessage(player, prefix + "&aSelect a biome to teleport to!");
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        
        if (!playerInventories.containsKey(player) || !playerInventories.get(player).equals(clickedInventory)) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().equals(Material.AIR)) {
            return;
        }
        
        // Check if close button was clicked
        if (clickedItem.getType().equals(Material.BARRIER)) {
            player.closeInventory();
            playerInventories.remove(player);
            return;
        }
        
        // Find the biome that corresponds to the clicked item
        Biome selectedBiome = null;
        for (Map.Entry<Biome, ItemStack> entry : biomeItems.entrySet()) {
            if (entry.getValue().getType().equals(clickedItem.getType())) {
                selectedBiome = entry.getKey();
                break;
            }
        }
        
        if (selectedBiome == null) {
            return;
        }
        
        player.closeInventory();
        playerInventories.remove(player);
        
        // Process teleportation
        processBiomeTeleport(player, selectedBiome);
    }
    
    private void processBiomeTeleport(Player player, Biome biome) {
        double cost = plugin.getConfig().getDouble("biome-tp.cost", 100.0);
        
        // Check economy using reflection to avoid hard dependency
        if (economy != null && cost > 0) {
            try {
                Method hasMethod = economy.getClass().getMethod("has", org.bukkit.OfflinePlayer.class, double.class);
                boolean hasBalance = (boolean) hasMethod.invoke(economy, player, cost);
                
                if (!hasBalance) {
                    MessageUtil.sendMessage(player, prefix + "&cYou need &6$" + cost + "&c to teleport to this biome!");
                    return;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check player balance: " + e.getMessage());
            }
        }
        
        // Send confirmation message
        MessageUtil.sendMessage(player, prefix + "&eSearching for " + biome.name().toLowerCase().replace("_", " ") + "...");
        
        // Search for biome asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Location biomeLocation = findBiomeLocation(player.getWorld(), biome, player.getLocation());
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (biomeLocation != null) {
                    // Charge player
                    if (economy != null && cost > 0) {
                        try {
                            Method withdrawMethod = economy.getClass().getMethod("withdrawPlayer", org.bukkit.OfflinePlayer.class, double.class);
                            withdrawMethod.invoke(economy, player, cost);
                            MessageUtil.sendMessage(player, prefix + "&cCharged &6$" + cost + "&c for teleportation.");
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to charge player: " + e.getMessage());
                        }
                    }
                    
                    // Teleport player
                    player.teleport(biomeLocation);
                    MessageUtil.sendMessage(player, prefix + "&aTeleported to " + biome.name().toLowerCase().replace("_", " ") + "!");
                } else {
                    MessageUtil.sendMessage(player, prefix + "&cCould not find " + biome.name().toLowerCase().replace("_", " ") + " biome nearby!");
                }
            });
        });
    }
    
    private Location findBiomeLocation(World world, Biome targetBiome, Location center) {
        int searchRadius = plugin.getConfig().getInt("biome-tp.search-radius", 10000);
        int maxAttempts = plugin.getConfig().getInt("biome-tp.max-attempts", 100);
        
        Random random = new Random();
        
        for (int i = 0; i < maxAttempts; i++) {
            // Generate random coordinates within search radius
            int x = center.getBlockX() + (random.nextInt(searchRadius * 2) - searchRadius);
            int z = center.getBlockZ() + (random.nextInt(searchRadius * 2) - searchRadius);
            
            // Get biome at this location
            Biome biome = world.getBiome(x, 64, z);
            
            if (biome.equals(targetBiome)) {
                // Find safe Y coordinate
                Location safeLocation = findSafeY(world, x, z);
                if (safeLocation != null) {
                    return safeLocation;
                }
            }
        }
        
        return null; // Biome not found
    }
    
    private Location findSafeY(World world, int x, int z) {
        // Start from build height and work down
        for (int y = world.getMaxHeight() - 1; y > 0; y--) {
            Location loc = new Location(world, x + 0.5, y, z + 0.5);
            
            if (world.getBlockAt(loc).getType().isSolid() && 
                world.getBlockAt(loc.clone().add(0, 1, 0)).getType().isAir() &&
                world.getBlockAt(loc.clone().add(0, 2, 0)).getType().isAir()) {
                
                return loc.clone().add(0, 1, 0); // Stand on solid block
            }
        }
        
        // Fallback to world spawn height
        return new Location(world, x + 0.5, world.getSpawnLocation().getY(), z + 0.5);
    }
} 