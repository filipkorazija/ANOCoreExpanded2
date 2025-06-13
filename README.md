# ANOCore2 - Complete Admin Documentation

**Version:** 2.0.0  
**Author:** Astonic  
**Minecraft Version:** 1.21.4+  
**Server Software:** Paper, Purpur, Spigot

ANOCore2 is a comprehensive Minecraft plugin that combines multiple quality-of-life features and utilities into a single, well-organized package. This documentation serves as a complete guide for server administrators.

---

## 📋 Table of Contents

1. [Installation](#installation)
2. [Core Features](#core-features)
3. [Commands Reference](#commands-reference)
4. [Permissions](#permissions)
5. [Configuration](#configuration)
6. [Message Customization](#message-customization)
7. [Feature Details](#feature-details)
8. [Troubleshooting](#troubleshooting)

---

## 🚀 Installation

1. Download the latest ANOCore2.jar file
2. Place it in your server's `plugins/` folder
3. Restart your server
4. Configure the plugin in `plugins/ANOCore2/config.yml`
5. Customize messages in `plugins/ANOCore2/messages.yml`

### Dependencies
- **Optional:** Vault (for economy features in BiomeTP)

---

## 🎯 Core Features

### **Utility Features**
- **Double Doors**: Automatic synchronization of adjacent doors
- **Slime Chunk Checker**: Find slime chunks in your world
- **Join Date Tracker**: Track when players first joined
- **Timber**: Tree cutting with one swing
- **Dragon Wings**: Reward players for killing the Ender Dragon
- **Teleport with Entities**: Keep pets when teleporting

### **Boost Systems**
- **XP Boost**: Server-wide experience multipliers
- **Grace Period**: Protection for new players

### **Teleportation**
- **BiomeTP**: Teleport to specific biomes (requires Vault)

### **Potion Effects**
- **Quick Commands**: Easy access to all potion effects with cooldowns

---

## 📝 Commands Reference

### **Admin Commands**
| Command | Description | Permission | Aliases |
|---------|-------------|------------|---------|
| `/anocore2 help` | Show complete command help | `ano.admin` | `/anoc2`, `/ano` |
| `/anocore2 reload` | Reload configuration and messages | `ano.admin` | |
| `/anocore2 version` | Show plugin version info | `ano.admin` | |

### **Core Features**
| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/slime [radius]` | Check for slime chunks around you | `ano.slimecheck` | `/slime 5` |
| `/joindate [player]` | View first join date | `ano.joindate` | `/joindate Steve` |
| `/timber` | Toggle timber mode on/off | `ano.timber` | `/timber` |

### **Boost Systems**
| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/xpboost start <multiplier> <duration>` | Start XP boost | `ano.xpboost` | `/xpboost start 2.0 300` |
| `/xpboost end` | End current XP boost | `ano.xpboost` | |
| `/xpboost check` | Check current boost status | `ano.xpboost` | |
| `/xpboost reload` | Reload XP boost config | `ano.xpboost` | |
| `/graceperiod start <player>` | Give grace period to player | `ano.graceperiod` | `/graceperiod start Steve` |
| `/graceperiod end` | End your grace period | `ano.graceperiod` | |
| `/graceperiod reload` | Reload grace period config | `ano.graceperiod` | |

### **Teleportation**
| Command | Description | Permission | Requirements |
|---------|-------------|------------|--------------|
| `/biometp` | Open biome teleport GUI | `ano.biometp` | Vault + Economy |

### **Potion Effects**
All potion effect commands follow the pattern: `/[effect_name]`

| Command | Effect | Permission | Cooldown |
|---------|--------|------------|----------|
| `/speed` | Speed | `ano.speed` | 10 minutes |
| `/jump_boost` | Jump Boost | `ano.jump_boost` | 10 minutes |
| `/regeneration` | Regeneration | `ano.regeneration` | 20 minutes |
| `/strength` | Strength | `ano.strength` | 15 minutes |
| `/instant_health` | Instant Health | `ano.instant_health` | 1 minute |
| `/night_vision` | Night Vision | `ano.night_vision` | 10 minutes |
| `/invisibility` | Invisibility | `ano.invisibility` | 15 minutes |
| `/fire_resistance` | Fire Resistance | `ano.fire_resistance` | 10 minutes |
| `/water_breathing` | Water Breathing | `ano.water_breathing` | 10 minutes |

**Additional Effects Available:**
`/instant_damage`, `/slowness`, `/haste`, `/mining_fatigue`, `/nausea`, `/blindness`, `/hunger`, `/weakness`, `/poison`, `/wither`, `/health_boost`, `/absorption`, `/saturation`, `/glowing`, `/levitation`, `/luck`, `/bad_luck`, `/slow_falling`, `/conduit_power`, `/dolphins_grace`, `/bad_omen`, `/hero_of_the_village`, `/darkness`

---

## 🔐 Permissions

### **Master Permission**
- `ano.*` - Grants access to all ANO permissions

### **Individual Permissions**
| Permission | Description | Default |
|------------|-------------|---------|
| `ano.admin` | Admin commands (reload, version) | OP |
| `ano.slimecheck` | Use slime chunk checker | Everyone |
| `ano.joindate` | Check join dates | Everyone |
| `ano.timber` | Use timber feature | Everyone |
| `ano.dragonelytra` | Receive dragon wings | Everyone |
| `ano.biometp` | Use biome teleportation | Everyone |
| `ano.xpboost` | Manage XP boosts | OP |
| `ano.graceperiod` | Manage grace periods | OP |

### **Potion Effect Permissions**
Each effect has its own permission: `ano.[effect_name]`
- Example: `ano.speed`, `ano.regeneration`, `ano.night_vision`

---

## ⚙️ Configuration

### **Main Configuration** (`config.yml`)

```yaml
# Feature Toggle Example
doubledoors:
  enabled: true
  prefix: "&6[DoubleDoors]&r "
  preserve-hinges: false
  require-frame: false
  debug: false

slimechunk:
  enabled: true
  prefix: "&a[SlimeCheck]&r "
  radius: 5
  max-radius: 10

# XP Boost Configuration
xpboost:
  enabled: true
  prefix: "&6[XPBoost]&r "
  action-bar: "&a&lXP Boost: {multiplier}x &7({duration})"

# Grace Period Configuration
graceperiod:
  enabled: true
  prefix: "&9[GracePeriod]&r "
  grace-period-duration: 10  # minutes
  title-switch: 30  # seconds
  bossbar-title: "&aGrace Period: &f{time}"

# Dragon Wings Reward Configuration
dragonwings:
  enabled: true
  prefix: "&d[DragonWings]&r "
  broadcast-enabled: true
  reward-item:
    material: "ELYTRA"
    name: "&5&lDragon Wings"
    lore:
      - "&7Earned by slaying the Ender Dragon"
    enchantments:
      UNBREAKING: 3
      MENDING: 1
    glow: false

# Potion Effects Configuration
potion-effects:
  enabled: true
  prefix: "&c[Effects]&r "
  effects:
    speed:
      duration: 300  # seconds
      cooldown: 600  # seconds
      amplifier: 1
```

### **Feature-Specific Settings**

#### **Double Doors**
- `preserve-hinges`: Keep existing door hinges instead of optimizing
- `require-frame`: Require solid blocks around doors
- `debug`: Enable debug messages

#### **Slime Chunk Checker**
- `radius`: Default search radius
- `max-radius`: Maximum allowed radius

#### **Timber**
- `default-enabled`: New players start with timber enabled
- `ignore-player-placed`: Only cut naturally generated trees
- `max-logs`: Maximum logs per tree
- `allowed-worlds`: Worlds where timber works

#### **BiomeTP**
- `cost`: Economy cost per teleport
- `search-radius`: How far to search for biomes
- `teleport-delay`: Delay before teleport
- `invincibility-duration`: Protection after teleport

---

## 💬 Message Customization

### **Message Configuration** (`messages.yml`)

All messages are fully customizable with color codes and placeholders:

```yaml
slimechunk:
  no-permission: "{prefix}&cYou don't have permission to use this command!"
  search-header: "{prefix}&aSlime chunk search results:"
  chunks-found-header: "&aFound {slime_count} slime chunk(s):"
  chunk-format: "&7- Chunk ({chunk_x}, {chunk_z}) &a{distance} chunks {direction}"

xpboost:
  start:
    - "&f"
    - "  &6&lXP BOOST HAS BEEN STARTED"
    - "  &f{multiplier}x multiplier for {duration}"
    - "&f"
```

### **Available Placeholders**

#### **Global Placeholders**
- `{prefix}` - Feature prefix
- `{player}` - Player name

#### **Feature-Specific Placeholders**

**Slime Chunk Checker:**
- `{radius}`, `{max_radius}`, `{total_chunks}`, `{slime_count}`
- `{chunk_x}`, `{chunk_z}`, `{distance}`, `{direction}`

**Join Date Tracker:**
- `{date}`, `{days_ago}`

**XP Boost:**
- `{multiplier}`, `{duration}`

**Dragon Wings:**
- `{item_name}`

---

## 📖 Feature Details

### **Double Doors**
- Automatically detects and synchronizes adjacent doors
- Smart hinge calculation for natural opening
- Configurable validation requirements
- Debug mode for troubleshooting

### **Slime Chunk Checker**
- Accurate slime chunk detection using Minecraft's algorithm
- Customizable search radius with admin limits
- Detailed results with distance and direction
- Real-time chunk analysis

### **Join Date Tracker**
- Uses actual server join data (not plugin-specific)
- Shows exact join date and time
- Calculates days since first join
- Works for both online and offline players

### **Timber**
- One-swing tree cutting with configurable limits
- Respects player-placed vs natural blocks
- World-specific enablement
- Individual player toggle

### **Dragon Wings**
- Fully configurable reward items (not limited to elytra)
- Custom enchantments and glow effects
- Broadcast messages to server
- Automatic inventory management

### **XP Boost**
- Server-wide experience multipliers
- Temporary duration-based boosts
- Real-time action bar display
- Administrative controls

### **Grace Period**
- New player protection system
- Configurable duration and boss bar
- Damage immunity and PvP protection
- Manual start/end controls

### **BiomeTP**
- GUI-based biome selection
- Economy integration (requires Vault)
- Distance-based pricing
- Safety checks and confirmation system

---

## 🔧 Troubleshooting

### **Common Issues**

#### **Commands Not Working**
1. Check if the feature is enabled in `config.yml`
2. Verify player has correct permissions
3. Ensure plugin loaded without errors
4. Try `/anocore2 reload`

#### **Messages Not Displaying**
1. Check `messages.yml` syntax
2. Verify color codes are correct (`&a`, `&c`, etc.)
3. Ensure placeholders are properly formatted
4. Try reloading messages with `/anocore2 reload`

#### **Economy Features Not Working**
1. Install Vault plugin
2. Install an economy plugin (EssentialsX, etc.)
3. Restart server
4. Verify economy is detected in console

### **Debug Mode**
Enable debug mode for specific features in `config.yml`:
```yaml
doubledoors:
  debug: true
```

### **Log Files**
Check server console and logs for:
- Plugin loading errors
- Permission issues
- Configuration problems
- Feature-specific debug messages

### **Performance Issues**
- Reduce slime chunk max radius
- Lower timber max logs limit
- Increase potion effect cooldowns
- Disable unused features

---

## 📞 Support

### **Getting Help**
1. Check this documentation first
2. Review server console for errors
3. Test with `/anocore2 reload`
4. Verify configuration syntax

### **Bug Reporting**
When reporting issues, include:
- Plugin version (`/anocore2 version`)
- Server version and type
- Relevant configuration sections
- Error messages from console
- Steps to reproduce

### **Feature Requests**
All features are designed to be configurable and extensible. Check configuration options before requesting new features.

---

**ANOCore2 v2.0.0** - Created by Astonic  
*A comprehensive solution for Minecraft server administration*