package dev.lyricsky;

import dev.lyricsky.data.LyricTimeline;
import dev.lyricsky.data.LyricsBridgeClient;
import dev.lyricsky.render.LyricRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.InputConstants;

public final class LyricSkyClient implements ClientModInitializer {
    private static LyricTimeline timeline;
    private static KeyMapping toggleKey;
    private static KeyMapping modeKey;
    private static KeyMapping offsetBackKey;
    private static KeyMapping offsetForwardKey;

    @Override
    public void onInitializeClient() {
        timeline = new LyricTimeline();
        new LyricsBridgeClient(timeline).start();

        KeyMapping.Category category = KeyMapping.Category.register(
                ResourceLocation.fromNamespaceAndPath("lyricsky", "controls"));
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.lyricsky.toggle", InputConstants.Type.KEYSYM, InputConstants.KEY_J, category));
        modeKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.lyricsky.mode", InputConstants.Type.KEYSYM, InputConstants.KEY_K, category));
        offsetBackKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.lyricsky.offset_back", InputConstants.Type.KEYSYM, InputConstants.KEY_LEFT_BRACKET, category));
        offsetForwardKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.lyricsky.offset_forward", InputConstants.Type.KEYSYM, InputConstants.KEY_RIGHT_BRACKET, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) LyricRenderer.toggle();
            while (modeKey.consumeClick()) LyricRenderer.cycleMode();
            while (offsetBackKey.consumeClick()) timeline.setOffsetMs(timeline.getOffsetMs() - 100);
            while (offsetForwardKey.consumeClick()) timeline.setOffsetMs(timeline.getOffsetMs() + 100);
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                ResourceLocation.fromNamespaceAndPath("lyricsky", "lyrics"),
                (graphics, deltaTracker) -> LyricRenderer.render(graphics, timeline));
    }
}
