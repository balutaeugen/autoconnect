# AutoConnect

AutoConnect is a lightweight client-side Minecraft mod that automatically joins a saved multiplayer server when you open the Multiplayer screen.

It is useful if you usually play on the same server and want to skip opening the server list, selecting the server, and pressing Join every time. The server address can be set manually, or updated automatically when you join a server from the normal multiplayer list.

AutoConnect only changes the client-side connection flow. It does not bypass authentication, whitelists, bans, player limits, server rules, or any other server-side restriction.

## Features

- Automatically connects after you open the Multiplayer screen
- Saves the last server you joined manually
- Lets you manually edit the saved server address
- Optional retry-on-failure behavior
- Configurable retry count and retry delay
- Adds a Reconnect button to failed connection screens
- Client-side only
- Supports Fabric, Forge, NeoForge, and Quilt

## Settings

| Setting | Description | Default |
| --- | --- | --- |
| Enabled | Turns AutoConnect on or off. When enabled, opening Multiplayer starts one automatic connection attempt if a server is configured. | `true` |
| Server Address | The server AutoConnect should join. Joining a server manually updates this value. | Empty |
| Retry on Failure | Allows AutoConnect to retry after a failed connection. | `false` |
| Retries Count | Number of retry attempts after the first failed connection. | `0` |
| Automatic Retry Timeout (in seconds) | Delay before an automatic retry. `0` retries immediately. | `0` |

## Disconnect Screen

When AutoConnect is configured, failed connection screens include a **Reconnect** button next to **Back to Server List**.

If retries are enabled and another retry is available, the disconnect screen also shows a countdown before the next automatic retry.

## Configuration

AutoConnect can be configured in-game where supported:

- Fabric: through Mod Menu when Mod Menu and Cloth Config are installed
- Forge: through the built-in mod list/config screen
- NeoForge: through the built-in mod list/config screen
- Quilt: through Mod Menu when available

The config file can also be edited directly:

```text
config/autoconnect.json
```

## Compatibility

AutoConnect supports Minecraft `26.1`, `26.1.1`, `26.1.2`, and `26.2`.

Supported loaders:

- Fabric
- Forge
- NeoForge
- Quilt

The `26.1.2` builds are used for the `26.1` compatibility range, because the mod has been tested across `26.1`, `26.1.1`, and `26.1.2` with the same compiled output.

## Optional Dependencies

Fabric and Quilt users can install Mod Menu for an in-game configuration entry. Fabric also uses Cloth Config for the Mod Menu configuration screen.

AutoConnect does not require Fabric API.

## Project Layout

```text
src/common/      Shared AutoConnect logic and config code
src/fabric/      Fabric entrypoints, metadata, and integration code
src/forge/       Forge entrypoints, metadata, and integration code
src/neoforge/    NeoForge entrypoints, metadata, and integration code
src/quilt/       Quilt entrypoints, metadata, and integration code
versions/        Fabric Stonecutter projects
forge/           Forge version projects
neoforge/        NeoForge version projects
quilt/           Quilt version projects
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
autoconnect-fabric-26.6.0-26.1-26.1.2.jar
autoconnect-fabric-26.6.0-26.2.jar
autoconnect-forge-26.6.0-26.1-26.1.2.jar
autoconnect-forge-26.6.0-26.2.jar
autoconnect-neoforge-26.6.0-26.1-26.1.2.jar
autoconnect-neoforge-26.6.0-26.2.jar
autoconnect-quilt-26.6.0-26.1-26.1.2.jar
autoconnect-quilt-26.6.0-26.2.jar
```

## Local Testing

Launch a specific loader and Minecraft version:

```sh
gradle launchFabric26_2
gradle launchNeoForge26_2
gradle launchQuilt26_2
```

Build and launch from a specific subproject:

```sh
gradle :fabric-26.2:buildJarAndRunClient
gradle :forge-26.2:buildJarAndRunClient
gradle :neoforge-26.2:buildJarAndRunClient
gradle :quilt-26.2:buildJarAndRunClient
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

Required repository secrets:

- `MODRINTH_TOKEN`
- `CURSEFORGE_TOKEN`

The current publishing targets are:

- Modrinth project `HwkBvmkg`
- CurseForge project `1580976`

## Ignored Files

Build outputs, run folders, local IDE files, generated caches, logs, crash reports, `publish/`, and the decompiled helper `net/` folder are ignored by Git.
