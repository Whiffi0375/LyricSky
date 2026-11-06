package dev.lyricsky.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lyricsky.data.LyricTimeline;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Camera-facing lyric words floating in front of the player in world space. */
public final class FloatingWordRenderer {
    private static volatile List<Word> words = List.of();
    private static LyricTimeline timeline;

    private FloatingWordRenderer() {}

    public static void register(LyricTimeline lyricTimeline) {
        timeline = lyricTimeline;
        LevelExtractionEvents.END_EXTRACTION.register(FloatingWordRenderer::extract);
        LevelRenderEvents.COLLECT_SUBMITS.register(FloatingWordRenderer::submit);
    }

    private static void extract(LevelExtractionContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (!LyricRenderer.isEnabled() || mc.player == null || mc.level == null || timeline == null) {
            words = List.of();
            return;
        }

        String lyric = timeline.currentLine();
        if (lyric == null || lyric.isBlank()) {
            words = List.of();
            return;
        }

        String[] parts = lyric.trim().split("\\s+");
        int count = Math.min(parts.length, 14);
        Vec3 eye = mc.player.getEyePosition();
        Vec3 look = mc.player.getLookAngle().normalize();
        Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0));
        if (right.lengthSqr() < 1.0E-5) right = new Vec3(1.0, 0.0, 0.0);
        else right = right.normalize();
        Vec3 up = right.cross(look).normalize();

        double time = (System.nanoTime() % 60_000_000_000L) / 1_000_000_000.0;
        List<Word> next = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double spread = (i - (count - 1) / 2.0) * 0.48;
            double bob = Math.sin(time * 1.35 + i * 0.72) * 0.10;
            Vec3 position = eye.add(look.scale(4.2))
                    .add(right.scale(spread))
                    .add(up.scale(bob + Math.sin(i * 1.1) * 0.10));
            next.add(new Word(parts[i], position, i));
        }
        words = List.copyOf(next);
    }

    private static void submit(LevelRenderContext context) {
        List<Word> snapshot = words;
        if (snapshot.isEmpty()) return;

        var camera = context.levelState().cameraRenderState;
        Vec3 cameraPos = camera.pos;
        PoseStack pose = context.poseStack();
        SubmitNodeCollector queue = context.submitNodeCollector();
        Font font = Minecraft.getInstance().font;

        for (Word word : snapshot) {
            pose.pushPose();
            pose.translate(word.position.x - cameraPos.x,
                    word.position.y - cameraPos.y,
                    word.position.z - cameraPos.z);
            pose.mulPose(camera.orientation);
            float scale = 0.026f + (float) (Math.sin(
                    word.index * 0.8 + System.nanoTime() / 700_000_000.0) * 0.0015);
            pose.scale(-scale, -scale, scale);

            String text = word.text;
            float width = font.width(text);
            queue.submitText(pose, -width / 2.0f, -font.lineHeight / 2.0f,
                    Component.literal(text).getVisualOrderText(), false,
                    Font.DisplayMode.SEE_THROUGH, 0x00F000F0,
                    0xFFFFFFFF, 0x55000000, 0xAAFFFFFF);
            pose.popPose();
        }
    }

    private record Word(String text, Vec3 position, int index) {}
}
