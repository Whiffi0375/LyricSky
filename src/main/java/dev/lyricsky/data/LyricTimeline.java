package dev.lyricsky.data;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class LyricTimeline {
    private final AtomicReference<List<LyricLine>> lines = new AtomicReference<>(List.of());
    private volatile long startedAtNanos = System.nanoTime();
    private volatile long offsetMs = 0;
    private volatile boolean playing = true;

    public void replace(List<LyricLine> next) {
        lines.set(List.copyOf(next));
        startedAtNanos = System.nanoTime();
    }

    public void setOffsetMs(long offset) { offsetMs = offset; }
    public void setPlaying(boolean value) { playing = value; }

    public String currentLine() {
        List<LyricLine> snapshot = lines.get();
        if (snapshot.isEmpty()) return "LyricSky • waiting for lyrics";
        long elapsed = playing ? (System.nanoTime() - startedAtNanos) / 1_000_000L : 0;
        long now = Math.max(0, elapsed + offsetMs);
        String current = "";
        for (LyricLine line : snapshot) {
            if (line.timeMs() > now) break;
            current = line.text();
        }
        return current;
    }
}
