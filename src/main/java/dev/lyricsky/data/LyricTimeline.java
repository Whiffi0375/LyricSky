package dev.lyricsky.data;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class LyricTimeline {
    private final AtomicReference<List<LyricLine>> lines = new AtomicReference<>(List.of());
    private volatile long anchorPositionMs;
    private volatile long anchorNanos = System.nanoTime();
    private volatile long offsetMs;
    private volatile boolean playing;

    public void replace(List<LyricLine> next) {
        lines.set(List.copyOf(next));
        anchorPositionMs = 0;
        anchorNanos = System.nanoTime();
    }

    public void setOffsetMs(long offset) { offsetMs = Math.max(-15000, Math.min(15000, offset)); }
    public long getOffsetMs() { return offsetMs; }

    public void setPlaybackPosition(long positionMs, boolean isPlaying) {
        anchorPositionMs = Math.max(0, positionMs);
        anchorNanos = System.nanoTime();
        playing = isPlaying;
    }

    public String currentLine() {
        List<LyricLine> snapshot = lines.get();
        if (snapshot.isEmpty()) return "";
        long elapsed = playing ? (System.nanoTime() - anchorNanos) / 1_000_000L : 0;
        long now = Math.max(0, anchorPositionMs + elapsed + offsetMs);
        String current = "";
        for (LyricLine line : snapshot) {
            if (line.timeMs() > now) break;
            current = line.text();
        }
        return current;
    }

    public String nextLine() {
        List<LyricLine> snapshot = lines.get();
        if (snapshot.isEmpty()) return "";
        long elapsed = playing ? (System.nanoTime() - anchorNanos) / 1_000_000L : 0;
        long now = Math.max(0, anchorPositionMs + elapsed + offsetMs);
        for (LyricLine line : snapshot) if (line.timeMs() > now) return line.text();
        return "";
    }

    public boolean hasLyrics() { return !lines.get().isEmpty(); }
}
