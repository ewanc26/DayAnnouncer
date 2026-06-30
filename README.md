# DayAnnouncer

A PaperMC plugin that broadcasts a message at dawn each in-game day.

---

## Requirements

- PaperMC 26.1.2+ (api-version 1.21)
- Java 25+

---

## Build

```bash
gradle build
```

Output: `build/libs/DayAnnouncer-1.1.0.jar`

The build pulls the Paper API from the PaperMC Maven repository. No local jar needed.

---

## Install

Copy the JAR to your server's `plugins/` directory:

```bash
cp build/libs/DayAnnouncer-1.1.0.jar /path/to/server/plugins/
```

Restart the server. A default `config.yml` is generated in `plugins/DayAnnouncer/`.

---

## Configuration

```yaml
# Message broadcast at dawn. Supports MiniMessage formatting.
# Placeholders: {day} = day number, {time} = in-game time string
message: "<yellow>It's 06:00! Day {day}</yellow>"

# Which world to monitor. Empty = first loaded world.
world: ""

# Check interval in ticks (20 = 1 second).
check-interval: 20

# Tick window after dawn (time 0) during which the announcement fires.
# Must be >= check-interval to guarantee detection.
dawn-threshold: 20
```

### MiniMessage

Messages support [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting:

```yaml
message: "<gradient:gold:yellow>Dawn breaks — Day {day}</gradient>"
message: "<bold><green>Day {day}</green></bold> — good morning!"
```

---

## How It Works

Two mechanisms detect dawn:

A repeating task (`BukkitRunnable`) checks world time every `check-interval` ticks. When time drops below `dawn-threshold`, it announces and sets a flag to prevent duplicates.

A `TimeSkipEvent` listener catches night skips from beds. If the skip lands in the dawn window, the announcement fires immediately without waiting for the next poll.

The day counter comes from `World.getFullTime() / 24000` — total ticks across all days.

---

## Project Structure

```
src/main/kotlin/uk/ewancroft/dayannouncer/
├── DayAnnouncer.kt          # Main plugin class
├── config/
│   └── PluginConfig.kt      # Config wrapper
├── task/
│   └── DayCheckTask.kt      # Repeating time-poll task
├── listener/
│   └── TimeSkipListener.kt  # Night-skip event handler
└── message/
    └── MessageFormatter.kt  # Placeholder replacement + MiniMessage
```

---

License: AGPL-3.0-only
