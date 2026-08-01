# DayAnnouncer

A PaperMC plugin that broadcasts a message at dawn each in-game day.

---

## Requirements

- PaperMC 26.1.2+ (api-version 1.21)
- Java 25+

---

## Build

```bash
./gradlew build
```

Output: `build/libs/DayAnnouncer-1.4.0.jar`

---

## Install

Copy the JAR to your server's `plugins/` directory:

```bash
cp build/libs/DayAnnouncer-1.4.0.jar /path/to/server/plugins/
```

Restart the server. A default `config.yml` is generated in `plugins/DayAnnouncer/`.

---

## Configuration

```yaml
enabled: true

worlds:
  world:
    enabled: true
    # Single message or a list of messages (randomly picked each dawn)
    message: "<yellow>It's {time}! Day {day}</yellow>"
    # messages:
    #   - "<yellow>It's {time}! Day {day}</yellow>"
    #   - "<gold>Good morning, Day {day}!</gold>"
    check-interval: 20
    dawn-threshold: 20
    # Per-world output and sound — omit to inherit global defaults
    # output:
    #   chat: true
    #   action-bar: false
    #   title: false
    #   boss-bar: false
    # sound: "entity.experience_orb.pickup"

# Default output channels
output:
  chat: true
  action-bar: false
  title: false
  boss-bar: false

# Default sound
sound: ""
```

### Placeholders

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `{day}` | Current in-game day number | `123` |
| `{time}` | Current in-game time (HH:MM) | `06:00` |
| `{time-name}` | Name of the current time period | `dawn`, `morning`, `afternoon`, `dusk`, `night`, `midnight` |
| `{world}` | Name of the world | `world` |
| `{players}` | Number of players in the world | `5` |
| `{max-players}` | Server max players | `20` |

### Message Pools

Define a list of messages instead of a single string. One is chosen at random each dawn:

```yaml
worlds:
  world:
    messages:
      - "<yellow>It's {time}! Day {day}</yellow>"
      - "<gradient:gold:yellow>Dawn breaks — Day {day}</gradient>"
      - "<bold><green>Day {day}</green></bold> — good morning!"
```

### MiniMessage

Messages support [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting:

```yaml
message: "<gradient:gold:yellow>Dawn breaks — Day {day}</gradient>"
message: "<bold><green>Day {day}</green></bold> — good morning!"
```

---

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/dayannouncer reload` | `dayannouncer.admin` | Reload configuration from disk |
| `/dayannouncer test [world]` | `dayannouncer.admin` | Send a test announcement |
| `/dayannouncer status [world]` | `dayannouncer.admin` | Show plugin status and config |
| `/dayannouncer toggle [world]` | `dayannouncer.admin` | Toggle announcements on/off |

Alias: `/da`

Toggle state is persisted to `config.yml` and survives restarts.

---

## How It Works

Two mechanisms detect dawn per-world:

A repeating task (`BukkitRunnable`) checks world time every `check-interval` ticks. When time drops below `dawn-threshold`, it picks a random message (from the pool) and dispatches it.

A `TimeSkipEvent` listener catches night skips from beds. If the skip lands in the dawn window, the announcement fires immediately without waiting for the next poll.

A shared day tracker prevents duplicate announcements when both mechanisms fire for the same dawn.

### Output Channels

The announcement can be delivered through multiple channels simultaneously:
- **Chat** — broadcast to all players in the world
- **Action bar** — displayed above the hotbar
- **Title** — large centred text (3 second duration)
- **Boss bar** — temporary bar that auto-hides after 5 seconds

Each world can define its own output mix, falling back to the global defaults.

### Sounds

A configurable sound effect can play alongside the announcement (e.g. `entity.experience_orb.pickup`). Uses the Bukkit sound registry. Per-world and default sound are supported.

### Metrics

DayAnnouncer uses bStats for anonymous usage statistics. Server administrators can disable this in `plugins/bStats/config.yml`.

### Update Checking

On startup, DayAnnouncer checks GitHub for newer releases. If an update is available, a message is logged to the console.

---

## Project Structure

```
src/main/kotlin/uk/ewancroft/dayannouncer/
├── DayAnnouncer.kt              # Main plugin class
├── command/
│   └── DayAnnouncerCommand.kt   # Command handler
├── config/
│   └── PluginConfig.kt          # Config wrapper
├── task/
│   └── DayCheckTask.kt          # Repeating time-poll task
├── listener/
│   └── TimeSkipListener.kt      # Night-skip event handler
├── message/
│   ├── AnnounceDispatcher.kt    # Multi-channel output + sound
│   └── MessageFormatter.kt      # Placeholder replacement + MiniMessage
└── util/
    └── UpdateChecker.kt         # GitHub release update checker
```

---

License: AGPL-3.0-only

## Support

If you find this project useful, consider supporting its development:

[![Ko-fi](https://img.shields.io/badge/Ko--fi-F16061?style=for-the-badge&logo=ko-fi&logoColor=white)](https://ko-fi.com/ewancroft)
[![GitHub Sponsors](https://img.shields.io/badge/GitHub%20Sponsors-30363D?style=for-the-badge&logo=github&logoColor=white)](https://github.com/sponsors/ewanc26)
