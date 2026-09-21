package org.bukkit.craftbukkit.v1_20_R1.util;

import java.nio.file.Path;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.jline.reader.Completer;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Reference;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;

/** Shared reader settings for the Minecraft console (not a shell command language). */
public final class ConsoleReaderFactory {
    private ConsoleReaderFactory() {
    }

    public static LineReader create(Terminal terminal, Path historyFile, Completer completer, Highlighter highlighter) {
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .appName("Taiyitist")
                // Preserve quotes, backslashes, selectors and JSON verbatim when completing.
                .parser(new DefaultParser().quoteChars(new char[0]).escapeChars(new char[0]))
                .completer(completer)
                .highlighter(highlighter)
                .variable(LineReader.HISTORY_FILE, historyFile)
                .variable(LineReader.HISTORY_SIZE, 1000)
                .variable(LineReader.HISTORY_FILE_SIZE, 1000)
                .variable(LineReader.BELL_STYLE, "none")
                .option(LineReader.Option.COMPLETE_IN_WORD, true)
                .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                .option(LineReader.Option.INSERT_TAB, false)
                .option(LineReader.Option.AUTO_FRESH_LINE, true)
                .option(LineReader.Option.AUTO_LIST, true)
                .option(LineReader.Option.AUTO_MENU, true)
                .option(LineReader.Option.DISABLE_HIGHLIGHTER, !TerminalConsoleAppender.isAnsiSupported())
                .build();
        // Some panels send normal cursor sequences even while xterm application mode is on.
        var keys = reader.getKeyMaps().get(LineReader.MAIN);
        keys.bind(new Reference(LineReader.UP_LINE_OR_HISTORY), "\u001b[A");
        keys.bind(new Reference(LineReader.DOWN_LINE_OR_HISTORY), "\u001b[B");
        keys.bind(new Reference(LineReader.FORWARD_CHAR), "\u001b[C");
        keys.bind(new Reference(LineReader.BACKWARD_CHAR), "\u001b[D");
        keys.bind(new Reference(LineReader.BEGINNING_OF_LINE), "\u001b[H", "\u001b[1~");
        keys.bind(new Reference(LineReader.END_OF_LINE), "\u001b[F", "\u001b[4~");
        return reader;
    }
}
