package dev.lyricsky.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LrcParser {
    private static final Pattern LINE = Pattern.compile("^\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?](.*)$");
    private LrcParser() {}

    public static List<LyricLine> parse(Path path) throws Exception {
        List<LyricLine> result = new ArrayList<>();
        for (String raw : Files.readAllLines(path)) {
            Matcher m = LINE.matcher(raw);
            if (!m.matches()) continue;
            long minutes = Long.parseLong(m.group(1));
            long seconds = Long.parseLong(m.group(2));
            String fraction = m.group(3) == null ? "0" : m.group(3);
            long millis = Long.parseLong((fraction + "00").substring(0, 3));
            result.add(new LyricLine((minutes * 60 + seconds) * 1000 + millis, m.group(4).trim()));
        }
        result.sort(Comparator.comparingLong(LyricLine::timeMs));
        return result;
    }
}
