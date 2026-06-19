# AutoConnect

AutoConnect is a lightweight client-side mod that automatically joins a saved multiplayer server when you open the Multiplayer screen.

This is useful if you usually play on the same server and want to skip opening the server list, selecting the server, and pressing Join every time. The server can be set manually, or updated automatically when you join a server from the normal multiplayer list.

AutoConnect only changes the client-side connection flow. It does not bypass authentication, whitelists, bans, player limits, server rules, or any other server-side restriction.

## Connection Options

| Name | Description | Default Value |
| --- | --- | --- |
| Enabled | Turns AutoConnect on or off. When enabled, opening Multiplayer starts one automatic connection attempt if a server is configured. | `true` |
| Server | The server address AutoConnect should join. Joining a server manually updates this value. | Empty |

## Retry Options

| Name | Description | Default Value |
| --- | --- | --- |
| Retry on Failure | Allows AutoConnect to retry automatically from the disconnect screen after a failed connection. | `false` |
| Retries Count | The number of additional attempts after the first failed connection. For example, `2` means the first attempt plus two retries. | `0` |
| Automatic Retry Timeout (in seconds) | How long to wait before retrying automatically from the disconnect screen. `0` retries as soon as the disconnect screen opens. | `0` |

## Disconnect Screen

When AutoConnect is configured, failed connection screens include a **Reconnect** button next to **Back to Server List**.

If retries are enabled and another retry is available, the disconnect screen also shows a countdown before the next automatic retry.

## Configuration

The in-game configuration screen is available through Mod Menu when both Mod Menu and Cloth Config are installed.

If those optional mods are not installed, or if you are using Forge, NeoForge, or Quilt, AutoConnect still works and can be configured from:

```text
config/autoconnect.json
```

## Requirements

### Fabric

- Fabric Loader
- Mod Menu (Optional)
- Cloth Config (Optional, required only for the Mod Menu configuration screen)

### Forge

- Minecraft Forge

### NeoForge

- NeoForge

### Quilt

- Quilt Loader

## Compatibility

AutoConnect currently targets Fabric, Forge, NeoForge, and Quilt for Minecraft `26.1` through `26.2`.
