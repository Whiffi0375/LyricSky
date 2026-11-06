package dev.lyricsky;

import net.fabricmc.api.ClientModInitializer;

/**
 * Minimal 26.x-compatible client entrypoint.
 *
 * The rendering and input integrations are being ported to the 26.x Mojang
 * mappings. Keeping this entrypoint deliberately small ensures the mod can
 * be packaged against both supported game versions while that work proceeds.
 */
public final class LyricSkyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Client-only initialization. Feature registration is added in the
        // next compatibility pass once the 26.x rendering APIs are wired up.
    }
}
