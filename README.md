# MCPvP Tier Tagger — Fabric 1.21.11

A client-side MCPvP adaptation of [PvPTiers/Tiers](https://github.com/PvPTiers/Tiers), based on its `1.21.11` branch. This is an independent fork, not an official PvPTiers release.

Install `mcpvp-tier-tagger-1.0.0+1.21.11.jar` in your Minecraft 1.21.11 `mods` folder with Fabric Loader **0.19.3 or newer**, Java **21 or newer**, and Fabric API **0.141.6+1.21.11 or newer for 1.21.11**. Mod Menu is optional. This fork retains mod ID `tiers`; replace the original Tiers JAR if it is installed.

## Usage

- `/tiers <username>` opens the upstream Tiers player profile GUI with MCPvP rankings, region, overall position and skin.
- `/tiers` or `/tiers -config` opens the upstream display configuration GUI.
- `/tiers -api` opens API settings. It is also accessible through the configuration screen's **API settings** button.
- `/tiers -clear` clears cached profiles; **Update** reloads a profile and its tier data.
- Tiers appear beside player names in the world, tab list and chat, using the upstream display toggles. The default selection is Sword, placed on the right; unranked Sword players show their highest available kit through the upstream adaptive mode.
- The upstream H shortcut opens the nearest player's profile. U and I cycle display kits; Y detects the current kit. Keybindings can be changed in Controls.
- **Previous kits / Next kits** in the profile GUI show all ranked kits without overflowing the upstream layout. Additional MCPvP kits use short text icons with full-name tooltips.

MCPvP high, middle and low tiers (`HT`, `MT`, `LT`), retired tiers, and peak tiers are preserved. Supported kits include Crystal, Sword, UHC, Pot, Netherite Pot, SMP, Axe, Mace, Shield, Early Game, Late Game, End Game, Spear, Diamond SMP, Cart, Creeper and Bow. Only kits returned by the selected provider are shown. Tier score tooltips are sorting scores inherited from the upstream display, not claimed MCPvP per-kit point awards. Overall points come from the provider.

## API configuration

`config/mcpvp-tier-tagger-api.json` is created automatically:

```json
{
  "primaryUrl": "https://www.mcpvp.com/tiers/data",
  "backupUrl": "https://mctiers.com/api/profile/{uuid}",
  "backupEnabled": true,
  "timeoutSeconds": 8,
  "cacheMinutes": 10
}
```

The primary URL must return the MCPvP leaderboard JSON (`players`, `hasMore`, `nextOffset`). The mod requests pages using `offset`, `limit=100`, and `include_retired=1`, sharing downloaded pages across player lookups. It stops once the requested UUID is found. Searches are asynchronous, with an 8-second request timeout and a 30-second primary lookup budget; the backup is used if enabled and the primary fails or does not contain the player. The backup must return the MCTiers `rankings` profile schema; `{uuid}` is replaced with the undashed UUID. The GUI identifies which provider supplied the results.

Online player UUIDs are taken from the server's player list. Other usernames are resolved with `https://api.mojang.com/users/profiles/minecraft/{name}`. The upstream player skin rendering services remain in use for the existing GUI. API failures produce an error state rather than inventing tiers. Clear the cache or use Update to refresh already displayed profiles. Editing the JSON directly requires a restart; GUI changes apply immediately.

## Build and verification

The included Gradle wrapper builds with JDK 25 and produces Java 21 classes for Minecraft 1.21.11:

```sh
./gradlew clean build
./gradlew -I tests/api.init.gradle tierApiTests
./gradlew -I tests/api.init.gradle -PliveApi tierApiTests
./gradlew -I tests/gui.init.gradle runClient
```

The API tests use a local HTTP server and a captured real MCPvP player fixture. They cover pagination, shared concurrent requests, cache hits, fallback on HTTP/malformed responses/missing players, disabled backup, retired and middle tiers, and URL validation. `-PliveApi` additionally checks real Mojang and MCPvP lookups.

The GUI test requires a graphical display (Xvfb works), Java 21 installed for the test launcher, and checks the actual client command, tier text, extra kit pages, settings navigation, invalid URL rejection and saved backup configuration. It exits automatically after success. Always run `./gradlew clean build` without the GUI test init script before distribution, so the test entrypoint is excluded.

Live MCPvP and Mojang were verified. The workspace could not access the MCTiers backup host, so the backup was verified against the profile schema and local HTTP fixtures, not a live backup response.

## License and attribution

GNU GPL version 3 or later; see [LICENSE](LICENSE), [NOTICE.md](NOTICE.md) and [UPSTREAM_README.md](UPSTREAM_README.md). Original Tiers code and assets are by Flavio6561 and contributors. The complete corresponding source, assets, build wrapper and verification scripts are included in the accompanying source ZIP.
