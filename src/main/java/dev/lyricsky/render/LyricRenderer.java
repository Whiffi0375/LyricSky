package dev.lyricsky.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;

public final class LyricRenderer {
    private LyricRenderer() {}

    public static void render(WorldRenderContext context, String line, int mode) {
        if (line == null || line.isBlank()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.textRenderer == null) return;

        MatrixStack matrices = context.matrixStack();
        matrices.push();
        var camera = context.camera();
        matrices.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
        var look = camera.getPos().add(camera.getRotation().transform(new org.joml.Vector3f(0, 0, -1)).mul(18));
        matrices.translate(look.x, look.y + (mode == 0 ? 2.0 : 0.0), look.z);
        matrices.multiply(new Quaternionf(camera.getRotation()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.scale(0.025f, -0.025f, 0.025f);

        TextRenderer tr = mc.textRenderer;
        VertexConsumerProvider.Immediate consumers = mc.getBufferBuilders().getEntityVertexConsumers();
        Text text = Text.literal(line);
        int width = tr.getWidth(text);

        if (mode == 1) {
            matrices.translate((line.length() % 5 - 2) * 12.0, (line.length() % 3 - 1) * 10.0, 0);
        } else if (mode == 2) {
            matrices.translate(0, -18, -20);
        }

        tr.draw(text, -width / 2f, 0, 0xFFFFFFFF, true, matrices.peek().getPositionMatrix(),
                consumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x55000000, 15728880);
        consumers.draw();
        matrices.pop();
    }
}
