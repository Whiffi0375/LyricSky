package dev.lyricsky;

import dev.lyricsky.data.LyricTimeline;
import dev.lyricsky.data.LyricsBridgeClient;
import dev.lyricsky.render.FloatingWordRenderer;
import net.fabricmc.api.ClientModInitializer;

public final class LyricSkyClient implements ClientModInitializer {
    private static LyricTimeline timeline;

    @Override
    public void onInitializeClient() {
        timeline = new LyricTimeline();
        new LyricsBridgeClient(timeline).start();
        FloatingWordRenderer.register(timeline);
    }
}
