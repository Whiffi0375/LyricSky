# LyricSky

A client-side Fabric mod project targeting Minecraft 26.2 and 26.3. LyricSky connects to a Windows media-session companion, looks up synced lyrics, and displays timed lyric lines in three HUD styles.

## Current status

The project contains:
- A Windows companion that reads the active Windows media session (Spotify is supported when it is the active session).
- Synced lyric lookup through LRCLIB.
- A localhost HTTP endpoint for song metadata, playback state, position, and timed lyric lines.
- A Minecraft client bridge that polls the companion and synchronizes the lyric timeline to the reported playback position.
- Three HUD display layouts: Cinematic, Scatter, and Look.
- Key mappings: J toggles lyrics, K cycles styles, [ and ] shift timing by 100 ms.

**Important limitation:** these are currently HUD visualizations, not the originally planned full 3D world-space sky/word projection. The Look style is a centered HUD reticle layout; it does not yet place text as world geometry along the camera's view direction. The UI and companion need hands-on testing in Minecraft on both supported versions.

## Windows companion setup

1. Install Python 3.10 or newer on Windows and enable the Python launcher (py).
2. Open the repository's companion folder.
3. Double-click start_companion.bat. It installs the Python dependency and starts the service.
4. Start Spotify and play a track.
5. Open http://127.0.0.1:8765/health to check the service status.
6. The lyric payload is available at http://127.0.0.1:8765/lyrics.

The companion binds to 127.0.0.1 only. It does not log in to Spotify or access a Spotify account. It reads the current Windows media session and uses LRCLIB for lyrics. Synced lyrics are not available for every track.

## Minecraft installation

1. Install Fabric Loader for the exact Minecraft version.
2. Install Fabric API for that same Minecraft version.
3. Download the matching lyricsky-minecraft-26.2 or lyricsky-minecraft-26.3 artifact ZIP from the repository's GitHub Actions run.
4. Extract the mod JAR into %appdata%\\.minecraft\\mods.
5. Start Minecraft with the matching Fabric profile and run the companion separately.

## Controls

| Key | Action |
|---|---|
| J | Toggle lyrics |
| K | Cycle Cinematic / Scatter / Look |
| [ | Lyrics timing -100 ms |
| ] | Lyrics timing +100 ms |

Controls can also be changed under Options > Controls > Key Binds.

## Build

GitHub Actions builds separate artifacts for Minecraft 26.2 and 26.3 using Java 25. Check the latest workflow run on the Actions tab.

## Lyrics source

Synced lyrics are retrieved from LRCLIB. Availability and accuracy vary by track. Lyrics remain the property of their respective rights holders; this project does not redistribute a lyric catalog.

## License

MIT.
