package org.bukkit.craftbukkit.v1_20_R1.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import org.jline.reader.Candidate;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleCompleterTest {
    @Test
    void completesOnlyUpToCursorAndPreservesLeadingSlash() throws Exception {
        var queue = new ConcurrentLinkedQueue<Runnable>();
        var requested = new AtomicReference<String>();
        var completer = new ConsoleCompleter(queue, buffer -> {
            requested.set(buffer);
            return List.of("help", "help", "");
        }, 1000);
        var offers = new ArrayList<Candidate>();
        var line = new DefaultParser().parse("/he later", 3, Parser.ParseContext.COMPLETE);
        Thread input = new Thread(() -> completer.complete(null, line, offers));
        input.start();
        try {
            long deadline = System.nanoTime() + 1_000_000_000L;
            while (queue.isEmpty() && System.nanoTime() < deadline) Thread.onSpinWait();
            assertFalse(queue.isEmpty());
            queue.remove().run();
            input.join(1500);
            assertFalse(input.isAlive());
            assertEquals("he", requested.get());
            assertEquals(List.of("/help"), offers.stream().map(Candidate::value).toList());
        } finally {
            input.interrupt();
            input.join(1500);
        }
    }

    @Test
    void stalledServerDoesNotFreezeInputOrAccumulateQueuedCompletions() {
        var queue = new ConcurrentLinkedQueue<Runnable>();
        var completer = new ConsoleCompleter(queue, buffer -> List.of("help"), 25);
        var offers = new ArrayList<Candidate>();
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(2), () ->
                completer.complete(null, new DefaultParser().parse("he", 2), offers));
        assertTrue(offers.isEmpty());
        assertTrue(queue.isEmpty());
    }
}
