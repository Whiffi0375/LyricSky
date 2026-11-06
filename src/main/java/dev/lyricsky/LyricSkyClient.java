package dev.lyricsky;

import dev.lyricsky.data.LyricTimeline;
import dev.lyricsky.data.LyricsBridgeClient;
import net.fabricmc.api.ClientModInitializer;

/**
 * Starts the local lyrics bridge. Rendering and keybind registration are
 * still pending the Minecraft 26.x client API integration.
 */
public final class LyricSkyClient implements ClientModInitializer {
    private static LyricTimeline timeline;

    @Override
    public void onInitializeClient() {
        timeline = new LyricTimeline();
        new LyricsBridgeClient(timeline).start();
    }

    public static LyricTimeline getTimeline() {
        return timeline;
    }
}
