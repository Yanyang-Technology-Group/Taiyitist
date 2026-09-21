package org.bukkit.craftbukkit.v1_20_R1.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.util.ConsoleCompleter;
import org.bukkit.event.server.TabCompleteEvent;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

public class ConsoleCommandCompleter implements Completer {

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        CraftServer server = Bukkit.getServer() instanceof CraftServer craftServer ? craftServer : null;
        if (server == null || server.getServer().isStopped() || !server.getServer().isRunning()) {
            return;
        }

        ConsoleCompleter completer = new ConsoleCompleter(server.getServer().bridge$processQueue(), buffer -> {
            List<String> offers = server.getCommandMap().tabComplete(server.getConsoleSender(), buffer);

            // Plugins are allowed to mutate the event's list, including when no command matched.
            TabCompleteEvent tabEvent = new TabCompleteEvent(server.getConsoleSender(), buffer,
                    offers == null ? new ArrayList<>() : new ArrayList<>(offers));
            server.getPluginManager().callEvent(tabEvent);

            return tabEvent.isCancelled() ? Collections.emptyList() : new ArrayList<>(tabEvent.getCompletions());
        }, 1000);
        try {
            completer.complete(reader, line, candidates);
        } catch (IllegalStateException e) {
            // JLine holds its redraw lock here. Logging while holding it can deadlock with
            // another thread printing above the prompt through TerminalConsoleAppender.
            java.util.concurrent.ForkJoinPool.commonPool().execute(() ->
                    server.getLogger().log(Level.WARNING, "Unhandled exception when tab completing", e));
        }
    }
}
