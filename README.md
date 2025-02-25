# Fallout - Radiation System for Minecraft

A Minecraft plugin that adds a Fallout-style radiation system to your server. Players can encounter radiation zones, gain radiation poisoning, and use items to reduce radiation levels.

## Features

- **Radiation Zones**: Create radiation sources with customizable radius, strength, and power
- **Radiation Effects**: Players gain radiation when in radiation zones, with effects based on radiation level
- **Boss Bar Display**: Shows current radiation level with a customizable boss bar
- **Radiation Resistance**: Players can gain resistance through armor and special items
- **Healing Items**: Configurable items like Rad-Away and Rad-X to reduce radiation
- **MMOItems Integration**: Optional integration with MMOItems for custom items and attributes
- **Storage Options**: Store data in YAML files or MySQL database

## Commands

- `/rad new <name> <radius> <strength> [power]` - Create a new radiation source
- `/rad list` - List all radiation sources
- `/rad remove <name>` - Remove a radiation source
- `/rad info [name]` - Show info about a radiation source or your current location
- `/rad reload` - Reload the plugin configuration

## Permissions

- `fallout.admin` - Access to all Fallout plugin commands
- `fallout.bypass` - Immunity to radiation effects

## Configuration

The plugin is highly configurable. See `config.yml` for all options.

### Radiation Sources

Radiation sources are defined by:
- **Radius**: How far the radiation extends from the center
- **Strength**: How strong the radiation is at the center (0-100)
- **Power**: How quickly radiation builds up as players get closer to the center (1-10)

### Radiation Effects

Players gain radiation when in radiation zones. The closer to the center, the faster radiation builds up.
Effects include:
- Damage over time
- Potion effects based on radiation level
- Visual and sound effects

### Healing Items

The plugin includes two default healing items:
- **Rad-Away**: Reduces radiation level
- **Rad-X**: Provides temporary radiation resistance

## MMOItems Integration

When MMOItems is present, the plugin will register two custom attributes:
- `RADIATION_RESISTANCE`: Reduces radiation gain
- `RADIATION_HEALING`: Increases radiation healing from items

## Installation

1. Place the plugin JAR in your server's `plugins` folder
2. Restart your server
3. Configure the plugin in `plugins/Fallout/config.yml`
4. Use `/rad new` to create radiation sources

## Building from Source

1. Clone the repository
2. Run `mvn clean package`
3. The compiled JAR will be in the `target` folder

## Requirements

- Minecraft 1.20.6 or higher
- Paper server
- Java 17 or higher
- MMOItems (optional) 