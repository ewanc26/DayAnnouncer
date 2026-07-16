# AGENTS.md

Guidance for agents working on DayAnnouncer, a small Kotlin PaperMC plugin that announces the start of each in-game day.

## Key areas

- `src/main/kotlin/` owns lifecycle, dawn detection, commands, metrics, update checks, sounds, and output channels.
- `src/main/resources/plugin.yml` defines the plugin identity, commands, and permissions.
- The bundled configuration is a public contract copied into server installations. New keys need safe defaults and reload handling.

## Invariants

- Announce once per world-day transition, including after reloads and time jumps; do not spam every tick around dawn.
- Respect per-world enablement, message pools, MiniMessage formatting, selected channels, and optional sounds.
- Use Paper APIs on the main server thread. Update checks or other network access must be asynchronous and failure-tolerant.
- Do not break existing configuration files when adding options; malformed values should produce useful diagnostics and fall back safely.
- Keep the plugin dependency-light and compatible with the Java/Paper versions declared in the README and Gradle build.

## Validation

Run `./gradlew build`. Test first enable, reload, disabled worlds, multiple worlds with different times, manual time changes, randomized messages, console/player commands, permissions, sound fallback, and server restart. The deliverable is the JAR in `build/libs/`; never commit build output.
