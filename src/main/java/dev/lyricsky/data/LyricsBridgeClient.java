package dev.lyricsky.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LyricsBridgeClient {
    private final LyricTimeline timeline;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(500)).build();
    private volatile String currentTrack = "";

    public LyricsBridgeClient(LyricTimeline timeline) { this.timeline = timeline; }

    public void start() {
        Thread worker = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:8765/lyrics"))
                            .timeout(Duration.ofSeconds(2)).GET().build();
                    HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) apply(response.body());
                } catch (Exception ignored) {
                    // The companion is optional; retry quietly.
                }
                try { Thread.sleep(1000); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "LyricSky-bridge-poll");
        worker.setDaemon(true);
        worker.start();
    }

    private void apply(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            String title = string(root, "title");
            String artist = string(root, "artist");
            String track = artist + "\u0000" + title;
            if (!Objects.equals(track, currentTrack)) {
                JsonArray arr = root.has("lines") && root.get("lines").isJsonArray()
                        ? root.getAsJsonArray("lines") : new JsonArray();
                List<LyricLine> parsed = new ArrayList<>();
                arr.forEach(item -> {
                    JsonObject obj = item.getAsJsonObject();
                    parsed.add(new LyricLine(obj.get("timeMs").getAsLong(), obj.get("text").getAsString()));
                });
                timeline.replace(parsed);
                currentTrack = track;
            }
            long position = root.has("positionMs") ? root.get("positionMs").getAsLong() : 0;
            boolean playing = root.has("playing") && root.get("playing").getAsBoolean();
            timeline.setPlaybackPosition(position, playing);
        } catch (RuntimeException ignored) {
            // Ignore partial or malformed responses and retry on the next poll.
        }
    }

    private static String string(JsonObject root, String key) {
        return root.has(key) && !root.get(key).isJsonNull() ? root.get(key).getAsString() : "";
    }
}
