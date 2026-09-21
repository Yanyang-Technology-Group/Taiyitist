package org.bukkit.craftbukkit.v1_20_R1.util;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AsyncConsoleHighlighterTest {
    @Test
    void typingAndLogRedrawNeverWaitForTheServerParser() throws Exception {
        CountDownLatch parsing = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        var highlighter = new AsyncConsoleHighlighter(new DefaultHighlighter() {
            @Override
            public AttributedString highlight(org.jline.reader.LineReader reader, String buffer) {
                parsing.countDown();
                try {
                    assertTrue(release.await(5, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    throw new AssertionError(e);
                }
                return new AttributedString(buffer, AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
            }
        });
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
                assertEquals("say", highlighter.highlight(null, "say").toString()));

        try (var server = Executors.newSingleThreadExecutor()) {
            try {
                var parse = server.submit(highlighter::update);
                assertTrue(parsing.await(2, TimeUnit.SECONDS));
                assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
                    for (String text : new String[]{"say ", "say h", "say he", "say hello"}) {
                        assertEquals(text, highlighter.highlight(null, text).toString());
                    }
                });
                release.countDown();
                parse.get(2, TimeUnit.SECONDS);
                highlighter.update();
                assertEquals(AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN),
                        highlighter.highlight(null, "say hello").styleAt(0));
            } finally {
                release.countDown();
            }
        }
    }

    @Test
    void aBrokenModHighlighterDoesNotStopLaterRequests() {
        var highlighter = new AsyncConsoleHighlighter(new DefaultHighlighter() {
            @Override
            public AttributedString highlight(org.jline.reader.LineReader reader, String buffer) {
                if (buffer.equals("broken")) throw new IllegalStateException("mod parser");
                return new AttributedString(buffer, AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
            }
        });
        highlighter.highlight(null, "broken");
        assertDoesNotThrow(highlighter::update);
        assertEquals("broken", highlighter.highlight(null, "broken").toString());
        highlighter.highlight(null, "help");
        highlighter.update();
        assertEquals(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN), highlighter.highlight(null, "help").styleAt(0));
    }

    @Test
    void eachTickParsesOnlyTheLatestInputOnce() {
        var parsed = new java.util.ArrayList<String>();
        var highlighter = new AsyncConsoleHighlighter(new DefaultHighlighter() {
            @Override
            public AttributedString highlight(org.jline.reader.LineReader reader, String buffer) {
                parsed.add(buffer);
                return new AttributedString(buffer);
            }
        });
        for (String text : new String[]{"h", "he", "hel", "help"}) {
            highlighter.highlight(null, text);
        }
        highlighter.update();
        highlighter.update();
        highlighter.highlight(null, "help");
        highlighter.update();
        assertEquals(java.util.List.of("help"), parsed);
    }
}
