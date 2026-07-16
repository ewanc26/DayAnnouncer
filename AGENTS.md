# AGENTS.md

Guidance for agents working on DayAnnouncer, a Kotlin Paper plugin that announces dawn per configured world.

## Current architecture

- `DayAnnouncer.kt` owns lifecycle, bStats, async update checking, command wiring, reload, per-world task creation, listener registration, and persisted toggles.
- `PluginConfig.kt` migrates configuration to version 1, clamps check intervals/dawn thresholds, supports single-message precedence over message lists, and parses global/per-world output and sound settings.
- `DayCheckTask` polls `world.time`, tracks its own `announced`/`lastDay` state, and dispatches once while inside its dawn window. `TimeSkipListener` separately dispatches on qualifying `TimeSkipEvent`; the two paths do not share deduplication state despite the README claim.
- `AnnounceDispatcher` supports chat, action bar, title, five-second boss bar, and registry-resolved sound. With no players in a world, chat currently falls back to a server-wide `Bukkit.broadcast`.
- `MessageFormatter` performs literal placeholder replacement before MiniMessage deserialization.

## Important invariants and known gaps

- The root `enabled` value is parsed and displayed/toggled but is not consulted when tasks/listeners are built; a global toggle currently does not disable announcements. Do not document it as working until this is fixed and tested.
- Per-world disablement prevents the polling task, but disabled worlds are still passed to `TimeSkipListener`, which does not check `config.enabled`. Preserve honest behavior or correct both paths together.
- Reload cancels tasks/boss bars, unregisters all listeners owned by the plugin, and rebuilds state. Keep it leak- and duplicate-free.
- Paper world/player calls and announcement delivery belong on the server thread. The GitHub update check is the only asynchronous path.
- Configuration changes need migration/default behavior. Keep `plugin.yml`, README commands, version, and Gradle version aligned.
- Gradle uses a Java 26 toolchain but emits Java/JVM 25 bytecode; Paper is compile-only, while Kotlin and relocated bStats are shaded.

## Validation

Run `./gradlew build` with JDK 26 available and inspect `build/libs/DayAnnouncer-1.4.0.jar`. There is no test suite today, so exercise on a test Paper 26.1 server: poll versus bed-skip duplication, day/time jumps, reload, missing/delayed worlds, global and world toggles, config migration/clamps/message precedence, every output channel, empty-world chat behavior, invalid sounds/MiniMessage, boss-bar cleanup, permissions, and update-check failure. Never commit server runtime data or build output.
