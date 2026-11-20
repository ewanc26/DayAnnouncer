# Contributing to DayAnnouncer

A PaperMC plugin that broadcasts a message at dawn each in-game day, with per-world messages, random message pools, MiniMessage formatting, and configurable output channels. Thanks for wanting to help!

## Getting started

```bash
./gradlew build
# Output: build/libs/DayAnnouncer-<version>.jar
```

Install into a Paper server's `plugins/` directory; a default `config.yml` is generated on first run.

## Notes for this project

- PaperMC 26.1.2+ (api-version 1.21) and Java 25+. Match the README's API expectations; don't raise them casually — every bump forces every server owner to update Java.
- Keep the plugin tiny: it does one thing, on a timer. Feature PRs that grow scope beyond "announce the dawn" will likely be declined.
- Config surface should stay per-world-capable and inheritable, as it is today (per-world output and sound overriding the global defaults).
- MiniMessage in user-supplied messages is a feature; make sure nothing filters or escapes it for server owners.
- Test on a real server before the PR: timers are the kind of logic that looks right in code and misbehaves per-world.

## Before opening a PR

- `./gradlew build` passes and you saw the announcement actually fire on a test server.
- Conventional commits; focused PRs; describe the test.
- AI-assisted PRs welcome; stand behind every line and run the build yourself.

## Licence

See [LICENSE](LICENSE). Contributions accepted under the same terms.