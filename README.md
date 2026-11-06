# LyricSky

A client-side Fabric mod prototype targeting Minecraft 26.2 and 26.3. The intended display modes are Cinematic, Scatter, and Look.

## Current implementation status

This repository is an **early source prototype**. The build workflow is being used to surface API and toolchain compatibility errors; a successful compile is not yet confirmed. No release JAR should be considered ready until the CI builds pass and the mod is tested in-game.

The Minecraft renderer and lyric timeline are prototypes. Spotify detection and the Windows media-session companion app are **not implemented yet**. The client polls a local endpoint at `http://127.0.0.1:8765/lyrics`, but the companion server that would supply data still needs to be written.

## Controls

- J: toggle lyrics
- K: cycle display mode
- [: shift timing -100 ms
- ]: shift timing +100 ms

## Local companion endpoint

The client polls `GET http://127.0.0.1:8765/lyrics` and expects:

```json
{
  "title": "Song title",
  "artist": "Artist",
  "lines": [
    {"timeMs": 0, "text": "First line"},
    {"timeMs": 4200, "text": "Second line"}
  ]
}
```

A future Windows companion can read the active media session using Windows Global System Media Transport Controls (GSMTC), then obtain timed lyrics from a separate lyrics provider. That companion is not part of the current build.

## Build

GitHub Actions builds separate artifacts for Minecraft 26.2 and 26.3. The toolchain coordinates are based on Fabric's published version guidance and will be validated by CI. The project uses Java 25 and Minecraft's unobfuscated 26.x names (no Yarn mappings).

Build outputs are uploaded as workflow artifacts for 14 days. Open the repository's **Actions** tab, select the latest successful run, and download the matching `lyricsky-minecraft-26.x` artifact.

## License

MIT.
