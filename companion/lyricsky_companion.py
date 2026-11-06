#!/usr/bin/env python3
"""LyricSky Windows companion: reads the active Windows media session and serves synced lyrics."""
from __future__ import annotations

import asyncio
import json
import logging
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlencode
from urllib.request import Request, urlopen

HOST = "127.0.0.1"
PORT = 8765
POLL_SECONDS = 1.0
USER_AGENT = "LyricSky/0.2 (https://github.com/Whiffi0375/LyricSky)"
state_lock = threading.Lock()
state = {
    "title": "", "artist": "", "album": "", "playing": False,
    "positionMs": 0, "durationMs": 0, "updatedAt": 0,
    "lines": [], "status": "Waiting for a Windows media session"
}
lyrics_cache: dict[str, list[dict]] = {}

def lrclib_lookup(artist: str, title: str, duration_ms: int) -> list[dict]:
    key = f"{artist.casefold()}|{title.casefold()}|{duration_ms // 1000}"
    if key in lyrics_cache:
        return lyrics_cache[key]
    params = {"artist_name": artist, "track_name": title}
    if duration_ms > 0:
        params["duration"] = str(round(duration_ms / 1000))
    url = "https://lrclib.net/api/get?" + urlencode(params)
    try:
        req = Request(url, headers={"User-Agent": USER_AGENT})
        with urlopen(req, timeout=6) as response:
            payload = json.loads(response.read().decode("utf-8"))
        synced = payload.get("syncedLyrics") or ""
        parsed = []
        for raw in synced.splitlines():
            raw = raw.strip()
            if not raw.startswith("[") or "]" not in raw:
                continue
            stamp, text = raw[1:].split("]", 1)
            try:
                mm, ss = stamp.split(":", 1)
                seconds, dot, fraction = ss.partition(".")
                millis = int((fraction + "000")[:3]) if dot else 0
                time_ms = (int(mm) * 60 + int(seconds)) * 1000 + millis
                if text.strip():
                    parsed.append({"timeMs": time_ms, "text": text.strip()})
            except (ValueError, TypeError):
                continue
        parsed.sort(key=lambda line: line["timeMs"])
        lyrics_cache[key] = parsed
        return parsed
    except Exception as exc:
        logging.info("Lyrics lookup failed: %s", exc)
        return []

async def read_session():
    from winsdk.windows.media.control import (
        GlobalSystemMediaTransportControlsSessionManager as Manager
    )
    manager = await Manager.request_async()
    session = manager.get_current_session()
    if session is None:
        return None
    props = await session.try_get_media_properties_async()
    playback = session.get_playback_info()
    timeline = session.get_timeline_properties()
    status = str(playback.playback_status).lower()
    playing = "playing" in status
    position = int(timeline.position.total_seconds() * 1000)
    duration = int(timeline.end_time.total_seconds() * 1000)
    return {
        "title": props.title or "",
        "artist": props.artist or "",
        "album": props.album_title or "",
        "playing": playing,
        "positionMs": max(0, position),
        "durationMs": max(0, duration),
    }

def poll_media():
    while True:
        try:
            media = asyncio.run(read_session())
            if not media or not media["title"]:
                with state_lock:
                    state.update(title="", artist="", album="", playing=False,
                                 positionMs=0, durationMs=0, lines=[],
                                 status="No active Windows media session", updatedAt=time.time())
            else:
                key = (media["artist"], media["title"], media["durationMs"])
                with state_lock:
                    same_track = (state["artist"], state["title"], state["durationMs"]) == key
                lines = state["lines"] if same_track else lrclib_lookup(*key)
                with state_lock:
                    state.update(media)
                    state["lines"] = lines
                    state["status"] = "Lyrics synced" if lines else "Track detected; synced lyrics not found"
                    state["updatedAt"] = time.time()
        except Exception as exc:
            logging.warning("Windows media session read failed: %s", exc)
            with state_lock:
                state["status"] = f"Media session error: {exc}"
        time.sleep(POLL_SECONDS)

class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path not in ("/lyrics", "/health"):
            self.send_error(404)
            return
        with state_lock:
            payload = dict(state)
        if self.path == "/health":
            payload = {"ok": True, "status": payload["status"]}
        data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def log_message(self, fmt, *args):
        logging.info("%s - %s", self.address_string(), fmt % args)

def main():
    logging.basicConfig(level=logging.INFO, format="[LyricSky] %(message)s")
    threading.Thread(target=poll_media, daemon=True, name="media-session").start()
    server = ThreadingHTTPServer((HOST, PORT), Handler)
    logging.info("Companion running at http://%s:%d/lyrics", HOST, PORT)
    logging.info("Listening only on localhost; press Ctrl+C to stop.")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()

if __name__ == "__main__":
    main()
