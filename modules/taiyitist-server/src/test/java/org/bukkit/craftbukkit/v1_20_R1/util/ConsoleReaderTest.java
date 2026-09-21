package org.bukkit.craftbukkit.v1_20_R1.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.CompletingParsedLine;
import org.jline.reader.Parser;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleReaderTest {
    @TempDir Path directory;

    @Test
    void tabOnEmptyInputListsCommandsAndDoesNotEscapeMinecraftSyntax() throws Exception {
        try (var terminal = TerminalBuilder.builder().dumb(true).streams(
                new ByteArrayInputStream("\t\n".getBytes()), new ByteArrayOutputStream()).build()) {
            LineReader reader = ConsoleReaderFactory.create(terminal, directory.resolve("history"),
                    (r, line, candidates) -> candidates.add(new Candidate("help")), null);
            assertEquals("help ", reader.readLine("> "));
            String command = "tellraw @a {\"text\":\"Hello!\"}";
            var parsed = (CompletingParsedLine) reader.getParser().parse(command, command.length(), Parser.ParseContext.COMPLETE);
            assertEquals("{\"text\":\"Hello!\"}", parsed.word());
            assertEquals("@a[name=\"a b\"]", parsed.escape("@a[name=\"a b\"]", false).toString());
        }
    }

    @Test
    void historyCanBeRecalledWithoutExpandingExclamationMarks() throws Exception {
        Path history = directory.resolve("history");
        try (var terminal = TerminalBuilder.builder().dumb(true).streams(
                new ByteArrayInputStream("say hello!\n".getBytes()), new ByteArrayOutputStream()).build()) {
            var reader = ConsoleReaderFactory.create(terminal, history, (r, l, c) -> {}, null);
            assertEquals("say hello!", reader.readLine("> "));
            reader.getHistory().save();
        }
        try (var terminal = TerminalBuilder.builder().dumb(true).streams(
                new ByteArrayInputStream("\u001b[A\n".getBytes()), new ByteArrayOutputStream()).build()) {
            var reader = ConsoleReaderFactory.create(terminal, history, (r, l, c) -> {}, null);
            assertEquals("say hello!", reader.readLine("> "));
        }
    }
}
