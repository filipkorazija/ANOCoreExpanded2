package me.astonic.ANOC2.features;

import me.astonic.ANOC2.ANOCore2;
import me.astonic.ANOC2.managers.CooldownManager;
import me.astonic.ANOC2.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class PotionEffectCommands implements CommandExecutor {
    
    private final ANOCore2 plugin;
    private final CooldownManager cooldownManager;
    private final String prefix;
    
    // Map of command names to potion effect types
    private final Map<String, PotionEffectType> potionCommands = new HashMap<>();
    
    public PotionEffectCommands(ANOCore2 plugin, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
        this.prefix = plugin.getConfigManager().getPrefix("potion-effects");
        
        initializePotionCommands();
    }
    
    private void initializePotionCommands() {
        // Register all potion effect commands
        potionCommands.put("speed", PotionEffectType.SPEED);
        potionCommands.put("slowness", PotionEffectType.SLOWNESS);
        potionCommands.put("haste", PotionEffectType.HASTE);
        potionCommands.put("miningfatigue", PotionEffectType.MINING_FATIGUE);
        potionCommands.put("strength", PotionEffectType.STRENGTH);
        potionCommands.put("weakness", PotionEffectType.WEAKNESS);
        potionCommands.put("jump", PotionEffectType.JUMP_BOOST);
        potionCommands.put("nausea", PotionEffectType.NAUSEA);
        potionCommands.put("regeneration", PotionEffectType.REGENERATION);
        potionCommands.put("resistance", PotionEffectType.RESISTANCE);
        potionCommands.put("fireresistance", PotionEffectType.FIRE_RESISTANCE);
        potionCommands.put("waterbreathing", PotionEffectType.WATER_BREATHING);
        potionCommands.put("invisibility", PotionEffectType.INVISIBILITY);
        potionCommands.put("blindness", PotionEffectType.BLINDNESS);
        potionCommands.put("nightvision", PotionEffectType.NIGHT_VISION);
        potionCommands.put("hunger", PotionEffectType.HUNGER);
        potionCommands.put("poison", PotionEffectType.POISON);
        potionCommands.put("wither", PotionEffectType.WITHER);
        potionCommands.put("healthboost", PotionEffectType.HEALTH_BOOST);
        potionCommands.put("absorption", PotionEffectType.ABSORPTION);
        potionCommands.put("saturation", PotionEffectType.SATURATION);
        potionCommands.put("levitation", PotionEffectType.LEVITATION);
        potionCommands.put("luck", PotionEffectType.LUCK);
        potionCommands.put("unluck", PotionEffectType.UNLUCK);
        potionCommands.put("slowfalling", PotionEffectType.SLOW_FALLING);
        potionCommands.put("conduitpower", PotionEffectType.CONDUIT_POWER);
        potionCommands.put("dolphinsgrace", PotionEffectType.DOLPHINS_GRACE);
        potionCommands.put("badomen", PotionEffectType.BAD_OMEN);
        potionCommands.put("heroofthevillage", PotionEffectType.HERO_OF_THE_VILLAGE);
        potionCommands.put("darkness", PotionEffectType.DARKNESS);
    }
    
    public void registerCommands() {
        for (String commandName : potionCommands.keySet()) {
            plugin.getCommand(commandName).setExecutor(this);
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, prefix + "&cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        String commandName = command.getName().toLowerCase();
        
        // Check if the command is disabled
        if (!plugin.getConfig().getBoolean("potion-effects.enabled", true)) {
            MessageUtil.sendMessage(player, prefix + "&cPotion effect commands are currently disabled!");
            return true;
        }
        
        PotionEffectType effectType = potionCommands.get(commandName);
        if (effectType == null) {
            MessageUtil.sendMessage(player, prefix + "&cUnknown potion effect command!");
            return true;
        }
        
        // Check permission
        String permission = "ano.potioneffect." + commandName;
        if (!player.hasPermission(permission)) {
            MessageUtil.sendMessage(player, prefix + "&cYou don't have permission to use this command!");
            return true;
        }
        
        // Check if player already has this effect
        if (player.hasPotionEffect(effectType)) {
            MessageUtil.sendMessage(player, prefix + "&cYou already have this potion effect active!");
            return true;
        }
        
        // Check cooldown
        String cooldownKey = "potioneffect_" + commandName;
        if (cooldownManager.hasCooldown(cooldownKey, player.getUniqueId())) {
            long remainingTime = cooldownManager.getRemainingCooldown(cooldownKey, player.getUniqueId());
            MessageUtil.sendMessage(player, prefix + "&cYou must wait &e" + formatTime(remainingTime) + "&c before using this command again!");
            return true;
        }
        
        // Get configuration for this specific effect
        String effectConfigPath = "potion-effects.effects." + commandName.replace("_", "_");
        int duration = plugin.getConfig().getInt(effectConfigPath + ".duration", 300) * 20; // Convert to ticks
        int amplifier = plugin.getConfig().getInt(effectConfigPath + ".amplifier", 0);
        
        // Parse arguments if provided (override config)
        if (args.length >= 1) {
            try {
                duration = Integer.parseInt(args[0]) * 20; // Convert seconds to ticks
                if (duration > 12000) { // Max 10 minutes (12000 ticks)
                    duration = 12000;
                    MessageUtil.sendMessage(player, prefix + "&eDuration capped at 10 minutes.");
                }
            } catch (NumberFormatException e) {
                MessageUtil.sendMessage(player, prefix + "&cInvalid duration! Using config default.");
            }
        }
        
        if (args.length >= 2) {
            try {
                amplifier = Integer.parseInt(args[1]);
                if (amplifier > 4) { // Max level 5
                    amplifier = 4;
                    MessageUtil.sendMessage(player, prefix + "&eAmplifier capped at level 5.");
                }
            } catch (NumberFormatException e) {
                MessageUtil.sendMessage(player, prefix + "&cInvalid amplifier! Using config default.");
            }
        }
        
        // Apply the potion effect
        PotionEffect effect = new PotionEffect(effectType, duration, amplifier, false, true);
        player.addPotionEffect(effect);
        
        // Set cooldown that starts AFTER the effect ends
        int cooldownSeconds = plugin.getConfig().getInt(effectConfigPath + ".cooldown", 600);
        long cooldownDelay = (duration / 20) * 1000L; // Convert ticks to milliseconds
        
        // Schedule the cooldown to start after the effect ends
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            cooldownManager.setCooldown(cooldownKey, player.getUniqueId(), cooldownSeconds * 1000L);
        }, duration);
        
        // Send success message
        String effectName = effectType.getName().toLowerCase().replace("_", " ");
        MessageUtil.sendMessage(player, prefix + "&aApplied &e" + effectName + "&a for &e" + (duration / 20) + "&a seconds" + 
                               (amplifier > 0 ? " with level &e" + (amplifier + 1) : "") + "!");
        
        return true;
    }
    
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }
} 