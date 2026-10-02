# Player Ping Display — Fabric 26.x ports

Displays a player's ping for five seconds after you attack them. These ports keep
version 1.0.6's ping lookup modes, live refresh interval, fade, latency colors,
HUD anchors/offsets, configuration preview, reset button, and optional Mod Menu
integration. Existing `config/playerpingdisplay.json` files work unchanged.

## Install

Use the jar for your exact Minecraft version in the client's `mods` folder.
Install Fabric Loader **0.19.5 or newer**, the matching Fabric API, and Java **25**.
Mod Menu is optional; the configuration key can also be assigned in Controls.

| Minecraft | Fabric API used for the build | Optional Mod Menu |
| --- | --- | --- |
| 26.1 | 0.145.1+26.1 | 18.0.2 |
| 26.2 | 0.161.0+26.2 | 20.0.3 |
| 26.3 | 0.161.0+26.3 | 21.0.0 |

Install only one Player Ping Display jar. The `-sources.jar` files are source
archives, not installable mods. This mod is client-only; servers do not need it.

## Build

With JDK 25 installed:

```sh
bash gradlew build -Pminecraft_version=26.1
bash gradlew build -Pminecraft_version=26.2
bash gradlew build -Pminecraft_version=26.3
```

Outputs go to `build/<minecraft_version>/libs/`, with the Minecraft version in
each filename. The default is 26.1. The GitHub Actions workflow builds all three
versions and uploads the jars as separate artifacts.

The common code uses Minecraft's unobfuscated class names and Fabric's HUD
element registry. The small `src/mc26.1` and `src/mc26.2` adapters account for the
screen/HUD state move in 26.2; the latter also serves 26.3.

## Validation

All three versions passed a clean Gradle build and an automated development-client
smoke check under Xvfb with Fabric Loader 0.19.5 and their matching Mod Menu.
The temporary smoke harness checked client initialization, settings rendering,
legacy config migration, saved settings, reset, returning to the parent screen,
and the Mod Menu configuration factory. Minecraft 26.3 required
`SDL_VIDEO_FORCE_EGL=1` for this virtual display.

The installable jars are rebuilt without the smoke harness. Live multiplayer ping
behavior has not been exercised; the original lookup and tracking logic is retained.

## Gameplay verification

For each version, join a multiplayer server with another player:

- Attack the player and check their name, ping, latency color, five-second display,
  and fade during the final second. Attack another player to change the target.
- Check both lookup modes and live refresh enabled/disabled. Missing tab-list
  entries should show `unknown`; temporary zero values retain the last positive ping.
- Open settings from the assigned key and Mod Menu. Check anchors, offsets,
  preview, refresh interval, reset, Done, and Escape. Restart to check persistence.
- Press F1 to hide the HUD. The overlay should disappear with the vanilla HUD.
