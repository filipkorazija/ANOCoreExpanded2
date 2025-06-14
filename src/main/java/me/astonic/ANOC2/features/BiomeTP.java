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
        // Overworld Biomes - Basic
        biomeItems.put(Biome.PLAINS, createBiomeItem(Material.GRASS_BLOCK, "&aPlains", "A vast expanse of grass and flowers"));
        biomeItems.put(Biome.FOREST, createBiomeItem(Material.OAK_LOG, "&2Forest", "Dense woodlands full of trees"));
        biomeItems.put(Biome.DESERT, createBiomeItem(Material.SAND, "&eDesert", "Hot, dry, sandy wasteland"));
        biomeItems.put(Biome.JUNGLE, createBiomeItem(Material.JUNGLE_LOG, "&2Jungle", "Thick tropical rainforest"));
        biomeItems.put(Biome.TAIGA, createBiomeItem(Material.SPRUCE_LOG, "&3Taiga", "Cold northern forest"));
        biomeItems.put(Biome.SWAMP, createBiomeItem(Material.LILY_PAD, "&8Swamp", "Murky wetlands"));
        biomeItems.put(Biome.MUSHROOM_FIELDS, createBiomeItem(Material.RED_MUSHROOM, "&dMushroom Fields", "Mystical mushroom island"));
        biomeItems.put(Biome.ICE_SPIKES, createBiomeItem(Material.ICE, "&bIce Spikes", "Frozen wasteland with ice spikes"));
        biomeItems.put(Biome.BADLANDS, createBiomeItem(Material.TERRACOTTA, "&6Badlands", "Mesa with colorful clay"));
        biomeItems.put(Biome.SAVANNA, createBiomeItem(Material.ACACIA_LOG, "&eSavanna", "African-style grassland"));
        biomeItems.put(Biome.DARK_FOREST, createBiomeItem(Material.DARK_OAK_LOG, "&8Dark Forest", "Dense, dark woodland"));
        biomeItems.put(Biome.CHERRY_GROVE, createBiomeItem(Material.CHERRY_LOG, "&dCherry Grove", "Beautiful pink cherry blossom forest"));
        biomeItems.put(Biome.MANGROVE_SWAMP, createBiomeItem(Material.MANGROVE_LOG, "&6Mangrove Swamp", "Tropical mangrove wetlands"));
        
        // Forest Variants
        biomeItems.put(Biome.BIRCH_FOREST, createBiomeItem(Material.BIRCH_LOG, "&fBirch Forest", "Light colored birch tree forest"));
        biomeItems.put(Biome.OLD_GROWTH_BIRCH_FOREST, createBiomeItem(Material.BIRCH_LOG, "&fOld Growth Birch Forest", "Ancient birch trees"));
        biomeItems.put(Biome.OLD_GROWTH_PINE_TAIGA, createBiomeItem(Material.SPRUCE_LOG, "&2Old Growth Pine Taiga", "Ancient pine forest"));
        biomeItems.put(Biome.OLD_GROWTH_SPRUCE_TAIGA, createBiomeItem(Material.SPRUCE_LOG, "&2Old Growth Spruce Taiga", "Ancient spruce forest"));
        biomeItems.put(Biome.FLOWER_FOREST, createBiomeItem(Material.POPPY, "&cFlower Forest", "Colorful forest full of flowers"));
        biomeItems.put(Biome.WINDSWEPT_FOREST, createBiomeItem(Material.OAK_LOG, "&2Windswept Forest", "Mountain forest with strong winds"));
        
        // Plains Variants
        biomeItems.put(Biome.SUNFLOWER_PLAINS, createBiomeItem(Material.SUNFLOWER, "&eSunflower Plains", "Plains covered in sunflowers"));
        biomeItems.put(Biome.SNOWY_PLAINS, createBiomeItem(Material.SNOW_BLOCK, "&fSnowy Plains", "Snow-covered grasslands"));
        
        // Jungle Variants
        biomeItems.put(Biome.SPARSE_JUNGLE, createBiomeItem(Material.JUNGLE_LOG, "&2Sparse Jungle", "Less dense jungle biome"));
        biomeItems.put(Biome.BAMBOO_JUNGLE, createBiomeItem(Material.BAMBOO, "&aBAMBOO_JUNGLE", "Jungle filled with bamboo"));
        
        // Taiga Variants
        biomeItems.put(Biome.SNOWY_TAIGA, createBiomeItem(Material.SNOW_BLOCK, "&fSnowy Taiga", "Snow-covered taiga forest"));
        
        // Savanna Variants
        biomeItems.put(Biome.SAVANNA_PLATEAU, createBiomeItem(Material.ACACIA_LOG, "&eSavanna Plateau", "Elevated savanna plateau"));
        biomeItems.put(Biome.WINDSWEPT_SAVANNA, createBiomeItem(Material.ACACIA_LOG, "&eWindswept Savanna", "Mountainous savanna with strong winds"));
        
        // Badlands Variants
        biomeItems.put(Biome.ERODED_BADLANDS, createBiomeItem(Material.RED_TERRACOTTA, "&cEroded Badlands", "Heavily eroded mesa terrain"));
        biomeItems.put(Biome.WOODED_BADLANDS, createBiomeItem(Material.OAK_LOG, "&6Wooded Badlands", "Mesa with scattered oak trees"));
        
        // Mountain Biomes
        biomeItems.put(Biome.WINDSWEPT_HILLS, createBiomeItem(Material.STONE, "&7Windswept Hills", "Rocky mountainous terrain"));
        biomeItems.put(Biome.WINDSWEPT_GRAVELLY_HILLS, createBiomeItem(Material.GRAVEL, "&7Windswept Gravelly Hills", "Gravel-covered mountains"));
        biomeItems.put(Biome.MEADOW, createBiomeItem(Material.GRASS_BLOCK, "&aMeadow", "High-altitude grassy meadow"));
        biomeItems.put(Biome.GROVE, createBiomeItem(Material.SNOW, "&fGrove", "Snowy mountain grove"));
        biomeItems.put(Biome.SNOWY_SLOPES, createBiomeItem(Material.POWDER_SNOW_BUCKET, "&fSnowy Slopes", "Snow-covered mountain slopes"));
        biomeItems.put(Biome.FROZEN_PEAKS, createBiomeItem(Material.PACKED_ICE, "&bFrozen Peaks", "Icy mountain peaks"));
        biomeItems.put(Biome.JAGGED_PEAKS, createBiomeItem(Material.STONE, "&7Jagged Peaks", "Sharp rocky mountain peaks"));
        biomeItems.put(Biome.STONY_PEAKS, createBiomeItem(Material.STONE, "&7Stony Peaks", "Stone-covered mountain peaks"));
        
        // Water Biomes
        biomeItems.put(Biome.OCEAN, createBiomeItem(Material.WATER_BUCKET, "&bOcean", "Deep blue waters"));
        biomeItems.put(Biome.DEEP_OCEAN, createBiomeItem(Material.WATER_BUCKET, "&1Deep Ocean", "Very deep ocean waters"));
        biomeItems.put(Biome.WARM_OCEAN, createBiomeItem(Material.TROPICAL_FISH_BUCKET, "&3Warm Ocean", "Tropical warm ocean"));
        biomeItems.put(Biome.LUKEWARM_OCEAN, createBiomeItem(Material.SALMON_BUCKET, "&9Lukewarm Ocean", "Moderately warm ocean"));
        biomeItems.put(Biome.COLD_OCEAN, createBiomeItem(Material.COD_BUCKET, "&1Cold Ocean", "Cold ocean waters"));
        biomeItems.put(Biome.DEEP_LUKEWARM_OCEAN, createBiomeItem(Material.SALMON_BUCKET, "&9Deep Lukewarm Ocean", "Deep moderately warm ocean"));
        biomeItems.put(Biome.DEEP_COLD_OCEAN, createBiomeItem(Material.COD_BUCKET, "&1Deep Cold Ocean", "Deep cold ocean"));
        biomeItems.put(Biome.FROZEN_OCEAN, createBiomeItem(Material.ICE, "&bFrozen Ocean", "Ice-covered ocean"));
        biomeItems.put(Biome.DEEP_FROZEN_OCEAN, createBiomeItem(Material.PACKED_ICE, "&bDeep Frozen Ocean", "Deep ice-covered ocean"));
        
        // Coastal Biomes
        biomeItems.put(Biome.BEACH, createBiomeItem(Material.SAND, "&eBeach", "Sandy coastline"));
        biomeItems.put(Biome.SNOWY_BEACH, createBiomeItem(Material.SAND, "&fSnowy Beach", "Snow-covered beach"));
        biomeItems.put(Biome.STONY_SHORE, createBiomeItem(Material.STONE, "&7Stony Shore", "Rocky coastline"));
        biomeItems.put(Biome.RIVER, createBiomeItem(Material.WATER_BUCKET, "&bRiver", "Flowing freshwater"));
        biomeItems.put(Biome.FROZEN_RIVER, createBiomeItem(Material.ICE, "&fFrozen River", "Ice-covered river"));
        
        // Cave Biomes
        biomeItems.put(Biome.DRIPSTONE_CAVES, createBiomeItem(Material.DRIPSTONE_BLOCK, "&7Dripstone Caves", "Underground caves with dripstone"));
        biomeItems.put(Biome.LUSH_CAVES, createBiomeItem(Material.MOSS_BLOCK, "&aLush Caves", "Underground caves with lush vegetation"));
        biomeItems.put(Biome.DEEP_DARK, createBiomeItem(Material.SCULK, "&0Deep Dark", "Deep underground sculk biome"));
        
        // Nether Biomes
        biomeItems.put(Biome.NETHER_WASTES, createBiomeItem(Material.NETHERRACK, "&cNether Wastes", "Classic hellish nether terrain"));
        biomeItems.put(Biome.CRIMSON_FOREST, createBiomeItem(Material.CRIMSON_STEM, "&4Crimson Forest", "Red nether forest"));
        biomeItems.put(Biome.WARPED_FOREST, createBiomeItem(Material.WARPED_STEM, "&bWarped Forest", "Blue-green nether forest"));
        biomeItems.put(Biome.SOUL_SAND_VALLEY, createBiomeItem(Material.SOUL_SAND, "&8Soul Sand Valley", "Valley of soul sand and fossils"));
        biomeItems.put(Biome.BASALT_DELTAS, createBiomeItem(Material.BASALT, "&8Basalt Deltas", "Volcanic basalt formations"));
        
        // End Biomes
        biomeItems.put(Biome.THE_END, createBiomeItem(Material.END_STONE, "&5The End", "Main End dimension"));
        biomeItems.put(Biome.SMALL_END_ISLANDS, createBiomeItem(Material.END_STONE, "&5Small End Islands", "Floating End islands"));
        biomeItems.put(Biome.END_MIDLANDS, createBiomeItem(Material.END_STONE, "&5End Midlands", "Mid-level End terrain"));
        biomeItems.put(Biome.END_HIGHLANDS, createBiomeItem(Material.END_STONE, "&5End Highlands", "High-level End terrain"));
        biomeItems.put(Biome.END_BARRENS, createBiomeItem(Material.END_STONE, "&5End Barrens", "Barren End wasteland"));
        
        // Special/Rare Biomes
        biomeItems.put(Biome.THE_VOID, createBiomeItem(Material.BARRIER, "&0The Void", "Empty void dimension"));
        biomeItems.put(Biome.PALE_GARDEN, createBiomeItem(Material.PALE_OAK_LOG, "&fPale Garden", "Mysterious pale garden"));
    }
    
    private ItemStack createBiomeItem(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.colorize(name));
            meta.setLore(Arrays.asList(
                MessageUtil.colorize("&7" + description),
                MessageUtil.colorize("&8"),
                MessageUtil.colorize("&8Click to teleport!")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack updateBiomeItemCost(ItemStack item) {
        ItemStack updatedItem = item.clone();
        ItemMeta meta = updatedItem.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                // Create new lore with current cost
                List<String> newLore = new ArrayList<>();
                for (String line : lore) {
                    newLore.add(line);
                }
                // Insert cost line before the last line (click to teleport)
                newLore.add(newLore.size() - 1, MessageUtil.colorize("&eCost: &6$" + plugin.getConfig().getDouble("biome-tp.cost", 100.0)));
                meta.setLore(newLore);
                updatedItem.setItemMeta(meta);
            }
        }
        return updatedItem;
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
            gui.setItem(slot, updateBiomeItemCost(entry.getValue()));
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
            
            // If not found with normal search, try a more conservative search closer to player
            if (biomeLocation == null) {
                biomeLocation = findBiomeLocationNearPlayer(player.getWorld(), biome, player.getLocation());
            }
            
            final Location finalBiomeLocation = biomeLocation;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (finalBiomeLocation != null) {
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
                    player.teleport(finalBiomeLocation);
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
        
        // Get world border info
        org.bukkit.WorldBorder worldBorder = world.getWorldBorder();
        double borderSize = worldBorder.getSize();
        Location borderCenter = worldBorder.getCenter();
        double maxDistance = borderSize / 2.0;
        
        Random random = new Random();
        
        for (int i = 0; i < maxAttempts; i++) {
            // Generate random coordinates within search radius
            int x = center.getBlockX() + (random.nextInt(searchRadius * 2) - searchRadius);
            int z = center.getBlockZ() + (random.nextInt(searchRadius * 2) - searchRadius);
            
            // Check if coordinates are within world border (with safety margin)
            double distanceFromBorderCenter = Math.sqrt(
                Math.pow(x - borderCenter.getX(), 2) + 
                Math.pow(z - borderCenter.getZ(), 2)
            );
            
            double safetyMargin = plugin.getConfig().getDouble("biome-tp.world-border-safety-margin", 50.0);
            if (distanceFromBorderCenter >= (maxDistance - safetyMargin)) {
                continue; // Skip this location, it's too close to or outside the border
            }
            
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
    
    private Location findBiomeLocationNearPlayer(World world, Biome targetBiome, Location center) {
        // More conservative search with smaller radius and closer to player
        int smallerRadius = Math.min(2000, plugin.getConfig().getInt("biome-tp.search-radius", 10000) / 2);
        int maxAttempts = plugin.getConfig().getInt("biome-tp.max-attempts", 100);
        
        // Get world border info
        org.bukkit.WorldBorder worldBorder = world.getWorldBorder();
        double borderSize = worldBorder.getSize();
        Location borderCenter = worldBorder.getCenter();
        double maxDistance = borderSize / 2.0;
        
        Random random = new Random();
        
        for (int i = 0; i < maxAttempts; i++) {
            // Generate random coordinates within smaller search radius
            int x = center.getBlockX() + (random.nextInt(smallerRadius * 2) - smallerRadius);
            int z = center.getBlockZ() + (random.nextInt(smallerRadius * 2) - smallerRadius);
            
            // Check if coordinates are within world border (with larger safety margin)
            double distanceFromBorderCenter = Math.sqrt(
                Math.pow(x - borderCenter.getX(), 2) + 
                Math.pow(z - borderCenter.getZ(), 2)
            );
            
            double safetyMargin = plugin.getConfig().getDouble("biome-tp.world-border-safety-margin", 50.0);
            if (distanceFromBorderCenter >= (maxDistance - (safetyMargin * 2))) { // Double safety margin for fallback
                continue; // Skip this location, it's too close to or outside the border
            }
            
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
        
        return null; // Biome not found even with conservative search
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