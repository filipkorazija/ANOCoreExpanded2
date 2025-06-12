# ANOCore2 - Comprehensive Minecraft Plugin

ANO Core Phase 2 is a comprehensive Minecraft plugin for version 1.21.4 using Java 21, packed with multiple useful features for server administrators and players.

## Features

### ✅ Fully Implemented Features

1. **DoubleDoors** - Synchronizes adjacent doors to open/close together with vanilla-like behavior
2. **SlimeChunkChecker** - Check for slime chunks in a configurable radius with `/slime`
3. **TeleportWithEntities** - Teleport with entities in boats and other vehicles
4. **JoinDateTracker** - Track and display when players first joined the server
5. **XPBoost** - Global XP multiplier system with action bar display (Integrated from LokoBoosts)
6. **GracePeriod** - New player protection system with boss bar display (Integrated from LokoGP)
7. **Timber** - Tree cutting feature with toggle capability and player-placed block tracking
8. **DragonWings** - Award custom enchanted elytra when players kill the Ender Dragon
9. **PotionEffectCommands** - All 29 potion effect commands with individual cooldowns and config
10. **BiomeTP** - GUI-based biome teleportation with economy integration and async biome finding

### 🎯 All Features Complete!

The plugin now includes all requested features with full functionality.

## Plugin Structure

```
src/main/java/me/astonic/ANOC2/
├── ANOCore2.java                    # Main plugin class
├── features/                        # Individual feature implementations
│   ├── DoubleDoors.java            # ✅ Complete
│   ├── SlimeChunkChecker.java      # ✅ Complete
│   ├── TeleportWithEntities.java   # ✅ Complete
│   ├── JoinDateTracker.java        # ✅ Complete
│   ├── XPBoost.java                # ✅ Complete (Integrated)
│   ├── GracePeriod.java            # ✅ Complete (Integrated)
│   ├── TimberFeature.java          # ✅ Complete - Tree cutting with toggle
│   ├── DragonWings.java            # ✅ Complete - Custom elytra rewards
│   ├── PotionEffectCommands.java   # ✅ Complete - All 29 potion effects
│   └── BiomeTP.java                # ✅ Complete - GUI biome teleportation
├── managers/                        # Core management classes
│   ├── DataManager.java            # Player data persistence
│   ├── CooldownManager.java        # Command cooldown management
│   └── TimberManager.java          # Tree cutting logic
├── utils/                          # Utility classes
│   ├── ConfigManager.java          # Configuration management
│   └── MessageUtil.java            # Message formatting utilities
├── xpboost/                        # XPBoost specific classes
│   └── ActiveBooster.java          # Booster data structure
└── graceperiod/                    # Grace Period specific classes
    └── GraceUser.java              # Grace period user management
```

## Configuration

The plugin uses a comprehensive configuration system with individual prefixes for each feature:

```yaml
# Each feature has its own configurable prefix and settings
doubledoors:
  enabled: true
  prefix: "&6[DoubleDoors]&r "

# XPBoost Feature (integrated from LokoBoosts)
xpboost:
  enabled: true
  prefix: "&6[XPBoost]&r "
  action-bar: "&a&lXP Boost: {multiplier}x &7({duration})"
  messages:
    # Complete configuration for all XPBoost features

# Grace Period Feature (integrated from LokoGP)  
graceperiod:
  enabled: true
  prefix: "&9[GracePeriod]&r "
  grace-period-duration: 10  # minutes
  bossbar-title: "&aGrace Period: &f{time}"
  # Complete configuration for all Grace Period features

# ... and more for each feature
```

## Commands

### All Available Commands:

**Core Features:**
- `/slime [radius]` - Check for slime chunks (Permission: `ano.slimecheck`)
- `/joindate [player]` - Check join dates (Permission: `ano.joindate`)
- `/timber` - Toggle timber mode (Permission: `ano.timber`)
- `/biometp` - Open biome teleportation GUI (Permission: `ano.biometp`)

**Integrated Systems:**
- `/xpboost <start|end|check|reload>` - Manage XP boosts (Permission: `ano.xpboost`)
- `/graceperiod <start|end|reload>` - Manage grace periods (Permission: `ano.graceperiod`)

**Potion Effect Commands (29 total):**
- `/speed [duration] [amplifier]` - Speed effect
- `/jump [duration] [amplifier]` - Jump boost effect
- `/strength [duration] [amplifier]` - Strength effect
- `/regeneration [duration] [amplifier]` - Regeneration effect
- `/nightvision [duration] [amplifier]` - Night vision effect
- `/invisibility [duration] [amplifier]` - Invisibility effect
- `/fireresistance [duration] [amplifier]` - Fire resistance effect
- `/waterbreathing [duration] [amplifier]` - Water breathing effect
- `/haste [duration] [amplifier]` - Haste effect
- `/resistance [duration] [amplifier]` - Resistance effect
- `/absorption [duration] [amplifier]` - Absorption effect
- `/saturation [duration] [amplifier]` - Saturation effect
- `/slowfalling [duration] [amplifier]` - Slow falling effect
- `/conduitpower [duration] [amplifier]` - Conduit power effect
- `/dolphinsgrace [duration] [amplifier]` - Dolphins grace effect
- `/luck [duration] [amplifier]` - Luck effect
- `/healthboost [duration] [amplifier]` - Health boost effect
- `/levitation [duration] [amplifier]` - Levitation effect
- And 12 more negative effects (slowness, weakness, poison, etc.)

*Each potion effect command has individual cooldowns that start after the effect ends.*

## Permissions

- `ano.*` - All permissions
- `ano.slimecheck` - Use slime chunk checker
- `ano.joindate` - Check join dates
- `ano.xpboost` - Use XP boost commands
- `ano.graceperiod` - Use grace period commands
- `ano.timber` - Use timber feature
- `ano.biometp` - Use biome teleportation
- Individual potion effect permissions (e.g., `ano.speed`, `ano.jump_boost`)

## Features Details

### XPBoost (Integrated from LokoBoosts)
- Global XP multiplier system
- Real-time action bar display showing current multiplier and remaining time
- Commands: `/xpboost start <multiplier> <duration>`, `/xpboost end`, `/xpboost check`, `/xpboost reload`
- Automatic integration with vanilla Minecraft XP
- Persistent booster storage across server restarts
- Server-wide broadcasts when boosts start

### Grace Period (Integrated from LokoGP)
- Automatic protection for new players joining for the first time
- Customizable duration (default: 10 minutes)
- Dynamic boss bar display with countdown timer
- Color-coded progress (Green → Yellow → Red as time runs out)
- Complete damage immunity during grace period
- Commands: `/graceperiod start <player>`, `/graceperiod end`, `/graceperiod reload`
- Manual grace period assignment for specific players

## Dependencies

- **Spigot API 1.21.4**
- **Java 21**
- **Vault** (Optional, for economy features)

## Build Instructions

1. Ensure you have Java 21 and Maven installed
2. Clone the repository
3. Run `mvn clean package`
4. The compiled JAR will be in the `target/` directory

## Installation

1. Place the compiled JAR in your server's `plugins/` directory
2. Start the server to generate configuration files
3. Configure the plugin in `plugins/ANOCore2/config.yml`
4. Restart the server or use `/reload` (if supported)

## Integration Notes

The XPBoost and GracePeriod features have been successfully integrated from the original LokoBoosts and LokoGP plugins with the following improvements:

- **Unified Configuration**: All settings now use the ANOCore2 configuration system
- **Consistent Messaging**: All features use the same message formatting system
- **Data Management**: Integrated with ANOCore2's data persistence system
- **Permission System**: Follows the `ano.*` permission structure
- **Prefix System**: Each feature has its own configurable prefix

## Feature Details

### 🌳 Timber Feature
- **Toggle Command**: `/timber` - Enable/disable timber mode per player
- **Smart Detection**: Ignores player-placed blocks (configurable)
- **Tool Durability**: Properly damages axes when cutting trees
- **World Restrictions**: Configurable allowed worlds
- **Max Logs**: Configurable maximum logs per tree (default: 200)
- **Axe Requirement**: Only works when holding an axe

### 🐉 Dragon Wings Feature
- **Automatic Reward**: Custom elytra given when killing Ender Dragon
- **Custom Item**: "Dragon Wings" with special name, lore, and Unbreaking III
- **Broadcast System**: Configurable server-wide announcements
- **Permission Check**: Requires `ano.dragonelytra` permission
- **Inventory Management**: Drops item if inventory is full

### 🧪 Potion Effect Commands
- **29 Different Effects**: All Minecraft potion effects available as commands
- **Individual Cooldowns**: Each effect has its own cooldown timer
- **Config-Based**: Duration, amplifier, and cooldown configurable per effect
- **Smart Cooldowns**: Cooldown starts AFTER the effect ends
- **Permission System**: Individual permissions for each effect
- **Argument Support**: Optional duration and amplifier arguments

### 🗺️ BiomeTP Feature
- **GUI Interface**: Beautiful inventory-based biome selection
- **12 Biomes Available**: Plains, Forest, Desert, Jungle, Taiga, Swamp, Ocean, Mushroom Fields, Ice Spikes, Badlands, Savanna, Dark Forest
- **Economy Integration**: Configurable cost per teleportation (Vault support)
- **Async Search**: Non-blocking biome finding with configurable search radius
- **Safe Teleportation**: Finds safe Y coordinates automatically
- **Search Limits**: Configurable max attempts and search radius

## Testing Recommendations

1. **Test DoubleDoors**: ✅ Fixed - Doors now open in correct directions with proper hinge behavior (vanilla-like)
2. **Test Timber**: Verify player-placed blocks are ignored, tool durability works
3. **Test Dragon Wings**: Kill Ender Dragon and verify custom elytra reward
4. **Test Potion Commands**: Try various effects with different durations/amplifiers
5. **Test BiomeTP**: Use GUI to teleport to different biomes, test economy integration
6. **Test Integration**: Verify XPBoost and GracePeriod work as expected

## Support

This plugin is built specifically for your server requirements. For modifications or additional features, please refer to the source code or contact the developer.

---

**Version**: 2.0.0  
**Minecraft**: 1.21.4  
**Java**: 21  
**Author**: astonic 