# AutoConnect 26.6.3

This release is less about shiny buttons and more about making AutoConnect behave like it has had coffee: calmer config handling, clearer retry behavior, sturdier release automation, and fewer places for duplicated build logic to hide.

Commit references are included for traceability.

## Added

- The AutoConnect config UI, retry status, reconnect button, and Cloth Config labels now use translatable strings. This should make future localization work much less awkward. (`209bff3`)
- Saved server addresses are validated before AutoConnect tries to use them. If the address is blank or malformed, AutoConnect now backs away politely instead of charging at the wall. (`7163ca7`)
- Config values are sanitized when loaded or saved, including saved addresses, retry count, and retry delay. (`5cbf9b7`, refined in `7163ca7`)
- Broken or unreadable JSON config files are backed up before AutoConnect writes a clean replacement. (`7163ca7`)
- Auto-connect, manual reconnect, and ordinary manual joins now have separate retry counters, so one path no longer spends another path's attempts. (`7163ca7`)
- Unit tests now cover config sanitization, server address validation, and retry counter behavior. (`7163ca7`)
- A dependency bump workflow can now run on a schedule or manually. (`345a538`, refined in `7163ca7`)
- CI now checks generated README content, Minecraft version builds, and unit tests. (`7163ca7`)
- Release jars now get SHA-256 checksum files. (`7163ca7`)

## Changed

- The default automatic retry delay is now `3` seconds instead of `0`, giving failed connections a tiny breather before the next attempt. (`5cbf9b7`)
- The disconnect retry countdown now updates from the screen tick instead of widget rendering, which is a better fit for timing logic. (`7163ca7`)
- Config UI and mod metadata wording now more clearly describe the core behavior: joining a saved multiplayer server when the Multiplayer screen opens. (`209bff3`)
- Quilt metadata now suggests Cloth Config alongside Mod Menu. (`209bff3`)
- Loader, Minecraft version, dependency, artifact, and publishing metadata now live in `gradle/autoconnect-metadata.json`. One table to rule the release matrix, minus the dramatic soundtrack. (`325d3bd`)
- Shared Gradle helpers moved into `gradle/autoconnect.gradle`, while loader-specific build logic now lives under `fabric/`, `forge/`, `neoforge/`, and `quilt/`. (`42424da`, refined in `325d3bd`)
- Release jar tasks now build, validate, checksum, and prepare the expected loader/version artifacts from shared metadata. (`325d3bd`, refined in `7163ca7`)
- Modrinth and CurseForge publishing now use metadata-driven publisher scripts instead of a long line of nearly identical workflow steps. (`9518706`, refined in `7163ca7`)
- README compatibility and artifact examples are now generated from Gradle metadata. (`209bff3`)

## Fixed

- Blank or malformed server addresses are no longer remembered or used for automatic connections. (`7163ca7`)
- Retry count and retry delay values are clamped back into their supported ranges. (`5cbf9b7`, refined in `7163ca7`)
- The last server address is only preserved when the joined server address is actually usable. (`7163ca7`)
- The release workflow now commits the version bump directly on the release branch. (`7163ca7`)

## Maintenance

- Refreshed tracked loader and dependency versions for Fabric, Forge, NeoForge, Quilt, Mod Menu, Cloth Config, Mixin, ASM, Stonecutter, and Foojay resolver. (`345a538`, `325d3bd`, `7163ca7`)
- Reduced duplicated Gradle configuration across loaders and Minecraft targets. The build files are still build files, but they are at least telling the same story now. (`325d3bd`)
- Simplified Modrinth and CurseForge project descriptions and dependency notes. (`209bff3`)
