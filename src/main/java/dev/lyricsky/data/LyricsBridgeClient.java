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

public final class LyricsBridgeClient {
    private final LyricTimeline timeline;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(300)).build();
    private volatile String lastPayload = "";

    public LyricsBridgeClient(LyricTimeline timeline) { this.timeline = timeline; }

    public void start() {
        Thread worker = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:8765/lyrics"))
                            .timeout(Duration.ofMillis(500)).GET().build();
                    HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200 && !response.body().equals(lastPayload)) {
                        apply(response.body());
                        lastPayload = response.body();
                    }
                } catch (Exception ignored) {
                    // The companion is optional; retry quietly.
                }
                try { Thread.sleep(1500); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "LyricSky-bridge-poll");
        worker.setDaemon(true);
        worker.start();
    }

    private void apply(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray arr = root.getAsJsonArray("lines");
        List<LyricLine> parsed = new ArrayList<>();
        arr.forEach(item -> {
            JsonObject obj = item.getAsJsonObject();
            parsed.add(new LyricLine(obj.get("timeMs").getAsLong(), obj.get("text").getAsString()));
        });
        timeline.replace(parsed);
    }
}
