# Player Ping Display — Fabric ports

Displays a player's ping for five seconds after you attack them. These ports keep
version 1.0.6's ping lookup modes, live refresh interval, fade, latency colors,
HUD anchors/offsets, configuration preview, reset button, and optional Mod Menu
integration. Existing `config/playerpingdisplay.json` files work unchanged.

## Install

Use the jar for your exact Minecraft version in the client's `mods` folder.
For Minecraft **1.21.11**, use Fabric Loader **0.19.3 or newer**. The other builds require **0.19.5 or newer**. Install the matching Fabric API.
Minecraft **1.21–1.21.11 uses Java 21**; **26.x uses Java 25**.
Mod Menu is optional; the configuration key can also be assigned in Controls.

| Minecraft | Fabric API used for the build | Optional Mod Menu |
| --- | --- | --- |
| 1.21 | 0.102.0+1.21 | 11.0.5 |
| 1.21.1 | 0.116.17+1.21.1 | 11.0.5 |
| 1.21.4 | 0.119.4+1.21.4 | 13.0.4 |
| 1.21.8 | 0.136.1+1.21.8 | 15.0.2 |
| 1.21.11 | 0.141.5+1.21.11 | 17.0.1-beta.1 |
| 26.1 | 0.145.1+26.1 | 18.0.2 |
| 26.2 | 0.161.0+26.2 | 20.0.3 |
| 26.3 | 0.161.0+26.3 | 21.0.0 |

Install only one Player Ping Display jar. The `-sources.jar` files are source
archives, not installable mods. This mod is client-only; servers do not need it.

## Customization — Minecraft 1.21.11

Version **1.1.1** adds a **Customization...** button to the settings GUI:

- Fonts: Minecraft, Unicode, and Enchanting (the vanilla enchanting glyphs).
- Ping color: automatic latency colors, named swatches, or RGB sliders.
- Text shadow, rounded corners, display duration (1–30 seconds), and HUD scale (50–300%).
- Background on/off, RGB color, opacity, and horizontal/vertical padding.
- Editable format with `%player%` and `%ping%`, plus preset formats such as
  `%player% - Ping: %ping%` and `%player% - %ping% ms`.

The preview updates immediately. Click and drag the preview in any settings screen
to move the HUD; its position is saved relative to the window and stays inside the
screen when resized. Controls hide while dragging so the preview can be placed over
them. Choose an anchor or change an offset to return to anchor positioning.
Settings save automatically. Reset restores all defaults, including the position.
Existing configuration files retain their old settings and receive defaults for the
new options. The other Minecraft builds remain on version 1.0.6.

## Build

With JDK 25 installed for Gradle/Loom (the 1.21 jars target Java 21):

```sh
bash gradlew build -Pminecraft_version=1.21
bash gradlew build -Pminecraft_version=1.21.1
bash gradlew build -Pminecraft_version=1.21.4
bash gradlew build -Pminecraft_version=1.21.8
bash gradlew build -Pminecraft_version=1.21.11
bash gradlew build -Pminecraft_version=26.1
bash gradlew build -Pminecraft_version=26.2
bash gradlew build -Pminecraft_version=26.3
```

Outputs go to `build/<minecraft_version>/libs/`, with the Minecraft version in
each filename. The default is 26.1. The GitHub Actions workflow builds all eight
versions and uploads the jars as separate artifacts.

The seven original ports share their config and Mod Menu entrypoint. Minecraft 1.21.11 has a separate source set for the customization update. The 1.21 sources
use Yarn mappings and the older HUD callback; 26.x sources use Minecraft's
unobfuscated class names and Fabric's HUD element registry. The small `src/mc26.1` and `src/mc26.2` adapters account for the
screen/HUD state move in 26.2; the latter also serves 26.3.

## Validation

All seven versions passed clean Gradle builds. The four 1.21 ports passed
automated development-client smoke checks on Java 21; the three 26.x ports
passed the same checks on Java 25. Checks used Xvfb, Fabric Loader 0.19.5, and
the matching Mod Menu.
The temporary smoke harness checked client initialization, settings rendering,
legacy config migration, saved settings, reset, returning to the parent screen,
and the Mod Menu configuration factory. Minecraft 26.3 required
`SDL_VIDEO_FORCE_EGL=1` for this virtual display.

The installable jars are rebuilt without the smoke harness. Live multiplayer ping
behavior has not been exercised; the original lookup and tracking logic is retained.

The 1.21.11 customization smoke test opens the real client and exercises font
choices, color controls, every customization setting, formats, drag/clamping,
window resizing, persistence, legacy migration, reset, and Mod Menu integration.
The 1.21.11 checks run on Fabric Loader 0.19.3. With both JDK 21 and JDK 25 installed, run on Linux with Xvfb:

```sh
xvfb-run -a bash gradlew -I tests/1.21.11/smoke.init.gradle -Pminecraft_version=1.21.11 runClient
bash gradlew clean build -Pminecraft_version=1.21.11
```

Screenshots are saved under `run/customization-smoke/screenshots/`. The smoke
harness is included only when that init script is supplied. Always clean before
packaging after running it; the CI build and smoke jobs use separate workspaces.

## Gameplay verification

For each version, join a multiplayer server with another player:

- Attack the player and check their name, ping, latency color, five-second display,
  and fade during the final second (1.21.11 uses the configured duration). Attack another player to change the target.
- Check both lookup modes and live refresh enabled/disabled. Missing tab-list
  entries should show `unknown`; temporary zero values retain the last positive ping.
- Open settings from the assigned key and Mod Menu. Check anchors, offsets,
  preview, refresh interval, reset, Done, and Escape. Restart to check persistence.
- Press F1 to hide the HUD. The overlay should disappear with the vanilla HUD.
