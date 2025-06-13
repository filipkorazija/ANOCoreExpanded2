package me.astonic.ANOC2;

import me.astonic.ANOC2.commands.MainCommand;
import me.astonic.ANOC2.features.*;
import me.astonic.ANOC2.managers.*;
import me.astonic.ANOC2.utils.ConfigManager;
import me.astonic.ANOC2.utils.MessageManager;
import me.astonic.ANOC2.utils.MessageUtil;
// Vault imports are handled dynamically
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class ANOCore2 extends JavaPlugin {
    
    private static ANOCore2 instance;
    private static final Logger logger = Logger.getLogger("ANOCore2");
    
    // Managers
    private ConfigManager configManager;
    private MessageManager messageManager;
    private DataManager dataManager;
    private CooldownManager cooldownManager;
    private TimberManager timberManager;
    
    // Features
    private DoubleDoors doubleDoors;
    private SlimeChunkChecker slimeChunkChecker;
    private TeleportWithEntities teleportWithEntities;
    private JoinDateTracker joinDateTracker;
    private TimberFeature timberFeature;
    private DragonWings dragonWings;
    private PotionEffectCommands potionEffectCommands;
    private BiomeTP biomeTP;
    private XPBoost xpBoost;
    private GracePeriod gracePeriod;
    
    // Economy (Vault) - using Object to avoid import issues
    private Object economy;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        initializeManagers();
        
        // Setup Vault economy
        setupEconomy();
        
        // Initialize features
        initializeFeatures();
        
        // Register events and commands
        registerEvents();
        registerCommands();
        
        logger.info("ANOCore2 v" + getDescription().getVersion() + " has been enabled!");
        logFeatureStatus();
    }
    
    @Override
    public void onDisable() {
        // Save all data
        if (dataManager != null) {
            dataManager.saveAllData();
        }
        
        // Cleanup resources
        cleanup();
        
        logger.info("ANOCore2 has been disabled!");
        instance = null;
    }
    
    private void initializeManagers() {
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        dataManager = new DataManager(this);
        cooldownManager = new CooldownManager();
        timberManager = new TimberManager(this);
    }
    
    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        
        try {
            // Use reflection to avoid compile-time dependency on Vault
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            Object rsp = getServer().getServicesManager().getRegistration(economyClass);
            if (rsp != null) {
                // Use reflection to get the provider
                economy = rsp.getClass().getMethod("getProvider").invoke(rsp);
            }
        } catch (Exception e) {
            logger.warning("Failed to setup economy: " + e.getMessage());
        }
    }
    
    private void initializeFeatures() {
        // Initialize all features based on config
        if (getConfig().getBoolean("doubledoors.enabled", true)) {
            doubleDoors = new DoubleDoors(this);
        }
        
        if (getConfig().getBoolean("slimechunk.enabled", true)) {
            slimeChunkChecker = new SlimeChunkChecker(this);
        }
        
        if (getConfig().getBoolean("teleportwithentities.enabled", true)) {
            teleportWithEntities = new TeleportWithEntities(this);
        }
        
        if (getConfig().getBoolean("joindate.enabled", true)) {
            joinDateTracker = new JoinDateTracker(this);
        }
        
        if (getConfig().getBoolean("timber.enabled", true)) {
            timberFeature = new TimberFeature(this, timberManager);
        }
        
        if (getConfig().getBoolean("dragonwings.enabled", true)) {
            dragonWings = new DragonWings(this);
        }
        
        if (getConfig().getBoolean("potion-effects.enabled", true)) {
            potionEffectCommands = new PotionEffectCommands(this, cooldownManager);
        }
        
        if (getConfig().getBoolean("biometp.enabled", true)) {
            biomeTP = new BiomeTP(this, economy);
        }
        
        if (getConfig().getBoolean("xpboost.enabled", true)) {
            xpBoost = new XPBoost(this);
        }
        
        if (getConfig().getBoolean("graceperiod.enabled", true)) {
            gracePeriod = new GracePeriod(this);
        }
    }
    
    private void registerEvents() {
        if (doubleDoors != null) doubleDoors.registerEvents();
        if (teleportWithEntities != null) teleportWithEntities.registerEvents();
        if (joinDateTracker != null) joinDateTracker.registerEvents();
        if (timberFeature != null) timberFeature.registerEvents();
        if (dragonWings != null) dragonWings.registerEvents();
        if (biomeTP != null) biomeTP.registerEvents();
        if (xpBoost != null) xpBoost.registerEvents();
        if (gracePeriod != null) gracePeriod.registerEvents();
    }
    
    private void registerCommands() {
        // Register main command
        MainCommand mainCommand = new MainCommand(this);
        getCommand("anocore2").setExecutor(mainCommand);
        getCommand("anocore2").setTabCompleter(mainCommand);
        
        if (slimeChunkChecker != null) slimeChunkChecker.registerCommands();
        if (joinDateTracker != null) joinDateTracker.registerCommands();
        if (timberFeature != null) timberFeature.registerCommands();
        if (potionEffectCommands != null) potionEffectCommands.registerCommands();
        if (biomeTP != null) biomeTP.registerCommands();
        if (xpBoost != null) xpBoost.registerCommands();
        if (gracePeriod != null) gracePeriod.registerCommands();
    }
    
    private void logFeatureStatus() {
        logger.info("Feature Status:");
        logger.info("- DoubleDoors: " + (doubleDoors != null ? "ENABLED" : "DISABLED"));
        logger.info("- SlimeChunk Checker: " + (slimeChunkChecker != null ? "ENABLED" : "DISABLED"));
        logger.info("- Teleport With Entities: " + (teleportWithEntities != null ? "ENABLED" : "DISABLED"));
        logger.info("- Join Date Tracker: " + (joinDateTracker != null ? "ENABLED" : "DISABLED"));
        logger.info("- Timber: " + (timberFeature != null ? "ENABLED" : "DISABLED"));
        logger.info("- Dragon Wings: " + (dragonWings != null ? "ENABLED" : "DISABLED"));
        logger.info("- Potion Effect Commands: " + (potionEffectCommands != null ? "ENABLED" : "DISABLED"));
        logger.info("- BiomeTP: " + (biomeTP != null ? "ENABLED" : "DISABLED"));
        logger.info("- XPBoost: " + (xpBoost != null ? "ENABLED" : "DISABLED"));
        logger.info("- Grace Period: " + (gracePeriod != null ? "ENABLED" : "DISABLED"));
        logger.info("- Economy (Vault): " + (economy != null ? "ENABLED" : "DISABLED"));
    }
    
    private void cleanup() {
        // Cleanup resources for each feature
        if (cooldownManager != null) {
            cooldownManager.cleanup();
        }
        
        if (timberManager != null) {
            timberManager.cleanup();
        }
        
        if (xpBoost != null) {
            xpBoost.cleanup();
        }
        
        if (gracePeriod != null) {
            gracePeriod.cleanup();
        }
    }
    
    public void reloadPlugin() {
        reloadConfig();
        configManager.reloadConfig();
        messageManager.reloadMessages();
        
        // Reinitialize features
        initializeFeatures();
        
        logger.info("ANOCore2 configuration reloaded!");
    }
    
    // Getters
    public static ANOCore2 getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
    
    public TimberManager getTimberManager() {
        return timberManager;
    }
    
    public Object getEconomy() {
        return economy;
    }
    
    public boolean hasEconomy() {
        return economy != null;
    }
    
    // Feature getters
    public DoubleDoors getDoubleDoors() {
        return doubleDoors;
    }
    
    public SlimeChunkChecker getSlimeChunkChecker() {
        return slimeChunkChecker;
    }
    
    public TeleportWithEntities getTeleportWithEntities() {
        return teleportWithEntities;
    }
    
    public JoinDateTracker getJoinDateTracker() {
        return joinDateTracker;
    }
    
    public TimberFeature getTimberFeature() {
        return timberFeature;
    }
    
    public DragonWings getDragonWings() {
        return dragonWings;
    }
    
    public PotionEffectCommands getPotionEffectCommands() {
        return potionEffectCommands;
    }
    
    public BiomeTP getBiomeTP() {
        return biomeTP;
    }
    
    public XPBoost getXPBoost() {
        return xpBoost;
    }
    
    public GracePeriod getGracePeriod() {
        return gracePeriod;
    }
} 