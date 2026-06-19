# AutoConnect

AutoConnect is a lightweight client-side mod that joins your saved multiplayer server when you open the Multiplayer screen.

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
- Works on Fabric, Forge, NeoForge, and Quilt

## Configuration

AutoConnect can be configured in-game on supported loaders:

- Fabric: through Mod Menu when Mod Menu and Cloth Config are installed
- Forge: through the built-in mod list/config screen
- NeoForge: through the built-in mod list/config screen
- Quilt: through Mod Menu when available

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
| Retries Count | Number of retry attempts after the first failed connection. | `0` |
| Automatic Retry Timeout (in seconds) | Delay before an automatic retry. `0` retries immediately. | `0` |

## Compatibility

AutoConnect supports Minecraft `26.1`, `26.1.1`, `26.1.2`, and `26.2`.

Supported loaders:

- Fabric
- Forge
- NeoForge
- Quilt

## Optional Dependencies

Fabric and Quilt users can install Mod Menu for an in-game configuration entry. Fabric also uses Cloth Config for the Mod Menu config screen.

AutoConnect does not require Fabric API.
