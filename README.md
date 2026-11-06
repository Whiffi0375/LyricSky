# LyricSky

LyricSky is a client-side Fabric mod project for Minecraft 26.2 and 26.3. Its goal is to display timed Spotify lyrics as immersive world-space text in three modes: Cinematic Sky, Scatter, and Look Direction.

## Current status — work in progress

The current GitHub Actions builds prove that the project packages against both Minecraft versions. **They are not yet a finished in-game lyric visualizer.** The Minecraft client entrypoint and renderer are still placeholders, so the current JAR does not show lyrics or provide the three display modes.

This repository now also includes a Windows companion prototype that:
- Reads the currently active Windows media session (including Spotify when Spotify is the active session).
- Looks up synced lyrics from LRCLIB.
- Serves track metadata, playback position, and timed lyric lines at \`http://127.0.0.1:8765/lyrics\`.

Spotify desktop must be running and actively exposing a Windows media session. Lyrics availability depends on LRCLIB; some tracks will not have synced lyrics. The companion does not log in to Spotify or access a Spotify account.

## Run the Windows companion

1. Install Python 3.10 or newer on Windows and enable the Python launcher (\`py\`).
2. Double-click \`start_companion.bat\` in the \`companion\` folder.
3. Start Spotify and play a track.
4. Open \`http://127.0.0.1:8765/health\` in a browser. The status should change to indicate whether a track and synced lyrics were found.
5. The lyric JSON is available at \`http://127.0.0.1:8765/lyrics\`.

The server binds to \`127.0.0.1\` only and is not exposed to your local network.

## Minecraft builds

GitHub Actions builds separate artifacts for Minecraft 26.2 and 26.3. Open the repository's **Actions** tab, select the latest successful run, and download the matching \`lyricsky-minecraft-26.x\` artifact ZIP. Extract the JAR into \`%appdata%\\.minecraft\\mods\` alongside Fabric API for the same Minecraft version.

**Important:** installing the current JAR alone will not display lyrics yet. The in-game input, playback synchronization, and world renderer still need to be integrated and tested against the 26.x client rendering APIs.

## Intended controls

The planned controls are:
- J: toggle lyrics
- K: cycle display mode
- [: shift timing -100 ms
- ]: shift timing +100 ms

These controls are not active in the current placeholder JAR.

## Lyrics source

Synced lyrics are requested from LRCLIB. Lyric availability and accuracy vary by track. Lyrics remain the property of their respective rights holders; this project retrieves short-lived data for display and does not redistribute a lyric catalog.

## License

MIT.
