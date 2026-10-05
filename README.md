# Player Ping Display — Fabric ports

Displays a player's ping after you attack them. Version **1.2.0** brings the
customizable, draggable HUD and **U** shortcut to all supported Fabric versions.
Ping lookup modes, live refresh, latency colors and the fade are preserved.
Existing `config/playerpingdisplay.json` files migrate automatically.

## Install

Use the jar for your exact Minecraft version in the client's `mods` folder.
Use Fabric Loader **0.19.3 or newer** and the matching Fabric API.
Minecraft **1.21–1.21.11 uses Java 21**; **26.x uses Java 25**.
Press **U** in-game to open the settings GUI. The key can be changed in Controls.
Existing Minecraft key assignments are preserved; reset the mod's binding in Controls
if an older installation still has it unbound. Mod Menu is optional.

| Minecraft | Fabric API used for the build | Optional Mod Menu |
| --- | --- | --- |
| 1.21 | 0.102.0+1.21 | 11.0.5 |
| 1.21.1 | 0.116.17+1.21.1 | 11.0.5 |
| 1.21.4 | 0.119.4+1.21.4 | 13.0.4 |
| 1.21.8 | 0.136.1+1.21.8 | 15.0.2 |
| 1.21.11 | 0.141.5+1.21.11 | 17.0.1-beta.1 |
| 26.1 | 0.145.1+26.1 | 18.0.2 |
| 26.1.1 | 0.155.3+26.1.2 | 18.0.2 |
| 26.1.2 | 0.155.3+26.1.2 | 18.0.2 |
| 26.2 | 0.161.0+26.2 | 20.0.3 |
| 26.3 | 0.161.0+26.3 | 21.0.0 |

Install only one Player Ping Display jar. The `-sources.jar` files are source
archives, not installable mods. This mod is client-only; servers do not need it.

## Customization — all versions

Version **1.2.0** adds a **Customization...** button to the settings GUI:

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
new options.

## Build

With JDK 25 installed for Gradle/Loom (the 1.21 jars target Java 21):

```sh
bash gradlew build -Pminecraft_version=1.21
bash gradlew build -Pminecraft_version=1.21.1
bash gradlew build -Pminecraft_version=1.21.4
bash gradlew build -Pminecraft_version=1.21.8
bash gradlew build -Pminecraft_version=1.21.11
bash gradlew build -Pminecraft_version=26.1
bash gradlew build -Pminecraft_version=26.1.1
bash gradlew build -Pminecraft_version=26.1.2
bash gradlew build -Pminecraft_version=26.2
bash gradlew build -Pminecraft_version=26.3
```

Outputs go to `build/<minecraft_version>/libs/`, with the Minecraft version in
each filename. The default is 26.1. The GitHub Actions workflow builds all ten
versions and uploads the jars as separate artifacts.

Configuration and Mod Menu integration are shared. The four older 1.21 targets use
Yarn and legacy mouse events, with separate matrix adapters before/after 1.21.8.
Minecraft 1.21.11 uses newer Yarn font and mouse APIs. The 26.x targets use
unobfuscated names and Fabric's HUD element registry; small adapters handle the
screen and HUD state move in 26.2. Minecraft 26.3 uses SDL scancodes for keys,
so its U shortcut uses Minecraft's key constant rather than GLFW's numeric code.

## Validation

The client smoke test exercises the default **U** shortcut and opening the GUI
through its registered key, all font/color/customization controls, editable formats,
presets, drag hit tests, grab offsets, clamping, resized bounds, persistence,
legacy config migration, reset, and Mod Menu integration. GitHub Actions builds
and runs these checks separately for every supported version on Loader 0.19.3.
Live multiplayer ping still needs a gameplay check.

With JDK 21 and JDK 25 installed, run on Linux with Xvfb (substitute the target version):

```sh
SDL_VIDEO_FORCE_EGL=1 xvfb-run -a bash gradlew -I tests/smoke.init.gradle -Pminecraft_version=26.3 runClient
bash gradlew clean build -Pminecraft_version=26.3
```

Screenshots are saved under `run/customization-smoke-<version>/screenshots/`.
The smoke harness is included only when that init script is supplied. Always
clean before packaging after running it. Run versions sequentially in one checkout;
CI uses isolated workspaces. The 26.3 smoke profile selects OpenGL for the virtual
display; production settings are unaffected. Production jars exclude the smoke harness.

## Gameplay verification

For each version, join a multiplayer server with another player:

- Attack the player and check their name, ping, latency color, configured duration,
  and fade during the final second. Attack another player to change the target.
- Check both lookup modes and live refresh enabled/disabled. Missing tab-list
  entries should show `unknown`; temporary zero values retain the last positive ping.
- Open settings with U and Mod Menu. Check anchors, offsets,
  preview, refresh interval, reset, Done, and Escape. Restart to check persistence.
- Press F1 to hide the HUD. The overlay should disappear with the vanilla HUD.
