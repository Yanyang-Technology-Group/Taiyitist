package org.bukkit.craftbukkit.v1_20_R1.util;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

/** Keeps command providers on the server thread while bounding the console's wait. */
public final class ConsoleCompleter implements Completer {
    private final Queue<Runnable> queue;
    private final Function<String, List<String>> provider;
    private final long timeoutMillis;

    public ConsoleCompleter(Queue<Runnable> queue, Function<String, List<String>> provider, long timeoutMillis) {
        this.queue = queue;
        this.provider = provider;
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String input = line.line().substring(0, line.cursor());
        boolean slash = input.startsWith("/");
        String command = slash ? input.substring(1) : input;
        FutureTask<List<String>> task = new FutureTask<>(() -> provider.apply(command));
        queue.add(task);
        try {
            List<String> offers = task.get(timeoutMillis, TimeUnit.MILLISECONDS);
            if (offers != null) {
                offers.stream().filter(offer -> offer != null && !offer.isEmpty()).distinct()
                        .map(offer -> slash && line.wordIndex() == 0 ? "/" + offer : offer)
                        .map(Candidate::new).forEach(candidates::add);
            }
        } catch (TimeoutException ignored) {
            // A busy/stopping server must not trap the operator in Tab completion.
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException failure) {
            throw new IllegalStateException("Unable to complete console command", failure.getCause());
        } finally {
            // Never interrupt the Minecraft thread when cancelling a late completion.
            task.cancel(false);
            queue.remove(task);
        }
    }
}
