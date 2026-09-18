# AutoConnect

AutoConnect is a lightweight client-side Fabric mod that joins your saved multiplayer server when you open the Multiplayer screen.

It is made for players who usually connect to the same server and want to skip the repeated flow of opening the server list, selecting the server, and pressing Join. You can enter a server address yourself, or let AutoConnect update it automatically when you join a server normally.

AutoConnect does not bypass authentication, whitelists, bans, player limits, server rules, or any other server-side restriction. It only changes the client-side connection flow.

## Features

- Automatically connects after you open the Multiplayer screen
- Saves the last server you joined manually
- Lets you edit the saved server address
- Optional retry-on-failure behavior
- Configurable retry count and retry delay
- Adds a Reconnect button to failed connection screens
- Client-side only

## Compatibility

Current releases support Minecraft **26.2** and **26.3** with **Fabric Loader** only. Minecraft 26.1.x, Forge, NeoForge, and Quilt are no longer supported.

## Configuration

AutoConnect can be configured in-game when Mod Menu and Cloth Config are installed.

You can also edit the config file directly:

```text
config/autoconnect.json
```

## Settings

| Setting | Description | Default |
| --- | --- | --- |
| Enabled | Turns AutoConnect on or off. | `true` |
| Server Address | The server AutoConnect should join. Joining a server manually updates this value. | Empty |
| Retry on Failure | Allows AutoConnect to retry after a failed connection. | `false` |
| Retry Count | Number of retry attempts after the first failed connection. | `0` |
| Automatic Retry Timeout (in seconds) | Delay before an automatic retry. `0` retries immediately. | `3` |

## Dependencies

- Fabric Loader (Required)
- Mod Menu (Optional)
- Cloth Config (Optional)

Install both Mod Menu and Cloth Config to access the in-game configuration screen. Fabric API is not required.
