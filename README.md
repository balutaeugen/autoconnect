# AutoConnect

AutoConnect is a lightweight client-side Fabric mod that automatically joins a saved multiplayer server when you open the Multiplayer screen.

It is useful if you usually play on the same server and want to skip opening the server list, selecting the server, and pressing Join every time. The server address can be set manually, or updated automatically when you join a server from the normal multiplayer list.

AutoConnect only changes the client-side connection flow. It does not bypass authentication, whitelists, bans, player limits, server rules, or any other server-side restriction.

## Features

- Automatically connects after you open the Multiplayer screen
- Saves the last server you joined manually
- Lets you manually edit the saved server address
- Can use or clear the last connected server from the config screen
- Optional retry-on-failure behavior
- Configurable retry count and retry delay
- Adds a Reconnect button to failed connection screens
- Client-side only
- Supports Fabric

## Settings

| Setting | Description | Default |
| --- | --- | --- |
| Enabled | Turns AutoConnect on or off. When enabled, opening Multiplayer starts one automatic connection attempt if a server is configured. | `true` |
| Server Address | The server AutoConnect should join. If this is empty, AutoConnect can use the last connected server when one has been remembered. | Empty |
| Retry on Failure | Allows AutoConnect to retry after a failed connection. | `false` |
| Retry Count | Number of retry attempts after the first failed connection. | `0` |
| Automatic Retry Timeout (in seconds) | Delay before an automatic retry. `0` retries immediately. | `3` |

## Disconnect Screen

When AutoConnect is configured, failed connection screens include a **Reconnect** button next to **Back to Server List**.

If retries are enabled and another retry is available, the disconnect screen also shows a countdown before the next automatic retry.

## Configuration

AutoConnect can be configured in-game through Mod Menu when Mod Menu and Cloth Config are installed.

The config file can also be edited directly:

```text
config/autoconnect.json
```

## Compatibility

<!-- autoconnect:compatibility:start -->
AutoConnect supports Minecraft `26.2`, `26.3`.

- Fabric: `26.2`, `26.3`
<!-- autoconnect:compatibility:end -->

Current releases support Fabric only. Minecraft 26.1.x, Forge, NeoForge, and Quilt are no longer supported.

## Downloads

- Modrinth: https://modrinth.com/mod/auto-connect
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/auto-connect

## Optional Dependencies

Install Mod Menu and Cloth Config to access the in-game configuration screen.

AutoConnect does not require Fabric API.

## Project Layout

```text
src/common/      Shared AutoConnect logic and config code
src/fabric/      Fabric entrypoints, metadata, and integration code
versions/        Fabric Stonecutter projects
publish/         Generated upload jars, ignored by Git
```

## Building

This project uses Gradle, Stonecutter, and Java 25.

Build every release jar and collect uploadable files in `publish/`:

```sh
gradle preparePublishArtifacts
```

The generated release jars are named with the loader first, then the mod version, then the Minecraft target:

```text
<!-- autoconnect:artifact-examples:start -->
autoconnect-fabric-26.9.1-26.2.jar
autoconnect-fabric-26.9.1-26.3.jar
<!-- autoconnect:artifact-examples:end -->
```

## Local Testing

Launch Fabric for a supported Minecraft version:

```sh
gradle launchFabric26_3
```

Build and launch from a specific subproject:

```sh
gradle :fabric-26.3:buildJarAndRunClient
```

Print the configured Minecraft dependency matrix:

```sh
gradle printMinecraftVersionMatrix
```

## Publishing

Player-facing platform descriptions live in:

- `MODRINTH.md`
- `CURSEFORGE.md`

GitHub Actions includes a manual `Publish` workflow. It builds the release jars, writes the provided changelog, and can publish to Modrinth, CurseForge, and GitHub Releases.

The `Publish Dry Run` workflow builds the publish artifacts and validates Modrinth and CurseForge metadata without uploading.

Required repository secrets:

- `MODRINTH_TOKEN`
- `CURSEFORGE_TOKEN`

The current publishing targets are:

- Modrinth project `HwkBvmkg`
- CurseForge project `1580976`

Developer notes for adding Minecraft versions live in `docs/adding-minecraft-version.md`.

## Ignored Files

Build outputs, run folders, local IDE files, generated caches, logs, crash reports, `publish/`, and the decompiled helper `net/` folder are ignored by Git.
