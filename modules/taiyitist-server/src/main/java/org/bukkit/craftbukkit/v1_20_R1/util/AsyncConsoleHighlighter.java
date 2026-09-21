package org.bukkit.craftbukkit.v1_20_R1.util;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.utils.AttributedString;

/** Keeps game/mod parsers out of JLine's input and log-redraw locks. */
public final class AsyncConsoleHighlighter extends DefaultHighlighter {
    private final Highlighter delegate;
    private volatile Result result;
    private final AtomicReference<Request> requested = new AtomicReference<>();

    public AsyncConsoleHighlighter(Highlighter delegate) {
        this.delegate = delegate;
    }

    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        if (reader != null && (reader.getSearchTerm() != null || reader.getRegionActive() != LineReader.RegionType.NONE)) {
            return super.highlight(reader, buffer);
        }
        if (buffer.isEmpty()) {
            return AttributedString.EMPTY;
        }
        Result cached = result;
        if (cached != null && cached.buffer.equals(buffer)) {
            return cached.text;
        }
        // Coalesce typing/log redraws; the server processes at most one request each tick.
        requested.set(new Request(reader, buffer));
        return new AttributedString(buffer);
    }

    /** Called once per server tick, never from the console or logging threads. */
    public void update() {
        Request request = requested.getAndSet(null);
        if (request == null) {
            return;
        }
        Result cached = result;
        if (cached != null && cached.buffer.equals(request.buffer)) {
            return;
        }
        AttributedString text;
        try {
            // Selection/search state belongs to the input thread; the delegate only parses text.
            text = delegate.highlight(null, request.buffer);
        } catch (RuntimeException failure) {
            text = new AttributedString(request.buffer);
        }
        result = new Result(request.buffer, text);
        if (request.reader != null) {
            // Never take the JLine lock on the server thread: Tab completion may be waiting for it.
            ForkJoinPool.commonPool().execute(() -> {
                try {
                    if (request.reader.isReading()) {
                        request.reader.callWidget(LineReader.REDISPLAY);
                    }
                } catch (IllegalStateException ignored) {
                    // The current read may have ended between the check and redisplay.
                }
            });
        }
    }

    private record Request(LineReader reader, String buffer) {}
    private record Result(String buffer, AttributedString text) {}
}
