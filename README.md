# LyricSky

A client-side Fabric mod prototype for Minecraft 26.2 and 26.3. It is intended to display timed lyrics in three world-space modes: Cinematic, Scatter, and Look.

## Current status

This repository contains an **early source prototype**, not a tested release. No installable JAR is available yet. The 26.2/26.3 Fabric Loader, Loom, mappings, Fabric API, and Java coordinates must be verified against published releases before builds can be considered supported.

Spotify detection and the Windows media-session companion are **not implemented yet**. The Minecraft client currently polls a local companion endpoint at `http://127.0.0.1:8765/lyrics`; that endpoint is only an interface contract, not a working Spotify integration.

## Controls

- J: toggle lyrics
- K: cycle display mode
- [: shift timing -100 ms
- ]: shift timing +100 ms

## Local bridge contract

The mod polls `GET http://127.0.0.1:8765/lyrics` and expects JSON:

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

The companion app still needs to be written. A Windows companion can read the active media session through Windows Global System Media Transport Controls (GSMTC), then obtain timed lyrics from a separate lyrics provider.

## Build status

The dependency values in `gradle.properties` are provisional and must not be treated as verified compatibility coordinates. Once matching 26.2 and 26.3 Fabric releases are confirmed, build with Gradle and the JDK required by those Minecraft versions. Output will be in `build/libs/`.

## License

MIT.
