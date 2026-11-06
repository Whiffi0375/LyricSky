package dev.lyricsky.render;

/** Shared enable state for the world-space lyric renderer. */
public final class LyricRenderer {
    private static volatile boolean enabled = true;

    private LyricRenderer() {}

    public static void toggle() {
        enabled = !enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
