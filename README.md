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

Output: `build/libs/DayAnnouncer-1.2.0.jar`

---

## Install

Copy the JAR to your server's `plugins/` directory:

```bash
cp build/libs/DayAnnouncer-1.2.0.jar /path/to/server/plugins/
```

Restart the server. A default `config.yml` is generated in `plugins/DayAnnouncer/`.

---

## Configuration

```yaml
# Master toggle to enable/disable all announcements.
enabled: true

# Per-world settings. Each key is a world name.
worlds:
  world:
    message: "<yellow>It's 06:00! Day {day}</yellow>"
    check-interval: 20
    dawn-threshold: 20

# Output channels — can all be enabled simultaneously.
output:
  chat: true
  action-bar: false
  title: false
  boss-bar: false

# Sound played to every player when an announcement fires.
# Set to '' or omit to disable.
sound: ""
```

### Placeholders

Messages support the following placeholders:

| Placeholder | Description |
|-------------|-------------|
| `{day}`     | Current in-game day number |
| `{time}`    | Current in-game time (HH:MM) |

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

---

## How It Works

Two mechanisms detect dawn per-world:

A repeating task (`BukkitRunnable`) checks world time every `check-interval` ticks. When time drops below `dawn-threshold`, it announces and sets a flag to prevent duplicates.

A `TimeSkipEvent` listener catches night skips from beds. If the skip lands in the dawn window, the announcement fires immediately without waiting for the next poll.

A shared day tracker prevents duplicate announcements when both mechanisms fire for the same dawn.

The day counter comes from `World.getFullTime() / 24000` — total ticks across all days.

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
└── message/
    ├── AnnounceDispatcher.kt    # Multi-channel output + sound
    └── MessageFormatter.kt     # Placeholder replacement + MiniMessage
```

---

License: AGPL-3.0-only
