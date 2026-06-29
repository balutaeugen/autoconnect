# AutoConnect

AutoConnect is a lightweight client-side mod that joins your saved multiplayer server when you open the Multiplayer screen.

It is useful if you usually play on the same server and want to skip opening the server list, selecting the server, and pressing Join every time. The server address can be entered manually, or updated automatically when you join a server from the normal multiplayer list.

AutoConnect only changes the client-side connection flow. It does not bypass authentication, whitelists, bans, player limits, server rules, or any other server-side restriction.

## Features

- Automatically connects after you open Multiplayer
- Saves the last server you joined manually
- Lets you manually edit the saved server address
- Optional retry-on-failure behavior
- Configurable retry count and retry delay
- Adds a Reconnect button to failed connection screens
- Client-side only

## Configuration

AutoConnect can be configured in-game where a config screen is available.

The config file can also be edited directly:

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

**Fabric**

- Mod Menu (Optional)
- Cloth Config (Optional)

**Quilt**

- Mod Menu (Optional)
- Cloth Config (Optional)
