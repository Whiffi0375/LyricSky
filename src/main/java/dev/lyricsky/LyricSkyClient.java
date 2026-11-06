package dev.lyricsky;

import dev.lyricsky.data.LyricTimeline;
import dev.lyricsky.data.LyricsBridgeClient;
import dev.lyricsky.data.LrcParser;
import dev.lyricsky.render.LyricRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;

public final class LyricSkyClient implements ClientModInitializer {
    public static final LyricTimeline TIMELINE = new LyricTimeline();
    public static boolean enabled = true;
    public static int mode = 0;
    public static long timingOffsetMs = 0;
    private LyricsBridgeClient bridge;

    private KeyBinding toggleKey, modeKey, offsetBackKey, offsetForwardKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lyricsky.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J,
                "category.lyricsky"));
        modeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lyricsky.mode", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K,
                "category.lyricsky"));
        offsetBackKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lyricsky.offset_back", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_BRACKET,
                "category.lyricsky"));
        offsetForwardKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lyricsky.offset_forward", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_BRACKET,
                "category.lyricsky"));

        bridge = new LyricsBridgeClient(TIMELINE);
        bridge.start();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) enabled = !enabled;
            while (modeKey.wasPressed()) mode = (mode + 1) % 3;
            while (offsetBackKey.wasPressed()) timingOffsetMs -= 100;
            while (offsetForwardKey.wasPressed()) timingOffsetMs += 100;
            TIMELINE.setOffsetMs(timingOffsetMs);
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (enabled && context.matrixStack() != null) {
                LyricRenderer.render(context, TIMELINE.currentLine(), mode);
            }
        });
    }

    public static void loadLrc(Path path) throws Exception {
        TIMELINE.replace(LrcParser.parse(path));
    }
}
