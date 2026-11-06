package dev.lyricsky.render;

import dev.lyricsky.data.LyricTimeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class LyricRenderer {
    public enum Mode { CINEMATIC, SCATTER, LOOK }
    private static volatile boolean enabled = true;
    private static volatile Mode mode = Mode.CINEMATIC;

    private LyricRenderer() {}

    public static void toggle() { enabled = !enabled; }
    public static boolean isEnabled() { return enabled; }
    public static Mode getMode() { return mode; }
    public static void cycleMode() { mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length]; }

    public static void render(GuiGraphicsExtractor graphics, LyricTimeline timeline) {
        if (!enabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        String line = timeline.currentLine();
        String next = timeline.nextLine();
        if (line.isBlank()) line = "♪  Waiting for synced lyrics  ♪";

        switch (mode) {
            case CINEMATIC -> cinematic(graphics, mc, width, height, line, next);
            case SCATTER -> scatter(graphics, mc, width, height, line);
            case LOOK -> look(graphics, mc, width, height, line, next);
        }
        graphics.text(mc.font, "LYRICSKY  •  " + mode.name(), 8, height - 14, 0x99FFFFFF, false);
    }

    private static void cinematic(GuiGraphicsExtractor g, Minecraft mc, int w, int h, String line, String next) {
        int y = h - 78;
        g.fill(w / 2 - 190, y - 14, w / 2 + 190, y + 47, 0x66000000);
        centered(g, mc, line, w / 2, y, 0xFFFFFFFF, true);
        if (!next.isBlank()) centered(g, mc, next, w / 2, y + 25, 0x99FFFFFF, false);
    }

    private static void scatter(GuiGraphicsExtractor g, Minecraft mc, int w, int h, String line) {
        String[] words = line.split("\\s+");
        int count = Math.min(words.length, 9);
        int center = count / 2;
        for (int i = 0; i < count; i++) {
            int x = w / 2 + (i - center) * 72 - mc.font.width(words[i]) / 2;
            int y = h / 2 + (int)(Math.sin(i * 1.7) * 38);
            g.text(mc.font, words[i], x, y, 0xFFFFFFFF, true);
        }
    }

    private static void look(GuiGraphicsExtractor g, Minecraft mc, int w, int h, String line, String next) {
        int x = w / 2;
        int y = h / 2 - 34;
        g.fill(x - 165, y - 12, x + 165, y + 52, 0x55000000);
        g.outline(x - 165, y - 12, 330, 64, 0x88FFFFFF);
        centered(g, mc, line, x, y + 2, 0xFFFFFFFF, true);
        if (!next.isBlank()) centered(g, mc, next, x, y + 27, 0x99FFFFFF, false);
        g.horizontalLine(x - 12, y + 62, y + 62, 0xAAFFFFFF);
        g.verticalLine(x, y + 50, y + 74, 0xAAFFFFFF);
    }

    private static void centered(GuiGraphicsExtractor g, Minecraft mc, String text, int centerX, int y, int color, boolean shadow) {
        String clipped = mc.font.plainSubstrByWidth(text, 360);
        g.text(mc.font, clipped, centerX - mc.font.width(clipped) / 2, y, color, shadow);
    }
}
