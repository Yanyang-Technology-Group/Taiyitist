package org.bukkit.craftbukkit.v1_20_R1.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleLoggingTest {
    @TempDir Path directory;

    @Test
    void plainOutputDoesNotLeaveAnAlreadyPrintedEventQueuedForReplay() throws Exception {
        String output = runProbe(false);
        assertEquals(1, occurrences(output, "startup-marker"), output);
        assertEquals(1, occurrences(output, "runtime-marker"), output);
        assertEquals(1, occurrences(output, "shutdown-marker"), output);
        assertEquals(1, occurrences(output, "stdout-marker"), output);
        assertEquals(1, occurrences(output, "reconfigured-marker"), output);
        assertTrue(output.contains("pending-terminal-events=0"), output);
        assertFalse(output.contains("\u001b"), output);
        assertFalse(output.contains("§"), output);
    }

    @Test
    void interactiveOutputSharesTheReaderAndKeepsColourOutOfFiles() throws Exception {
        String output = runProbe(true);
        assertTrue(output.contains("reader-attached=true"), output);
        assertEquals(1, occurrences(output, "startup-marker"), output);
        assertEquals(1, occurrences(output, "runtime-marker"), output);
        assertEquals(1, occurrences(output, "shutdown-marker"), output);
        assertTrue(output.contains("\u001b["), output);
        assertTrue(output.contains("\u001b[0;38;2;255;0;128m"), output);
        assertEquals(1, occurrences(output, "stdout-marker"), output);
        assertEquals(1, occurrences(output, "reconfigured-marker"), output);
        for (String file : new String[]{"latest.log", "debug.log"}) {
            String log = Files.readString(directory.resolve("logs").resolve(file));
            assertFalse(log.contains("\u001b"), log);
            assertFalse(log.contains("§"), log);
            assertEquals(1, occurrences(log, "runtime-marker"), log);
            assertTrue(log.contains("test-stack-trace"), log);
        }
    }

    private String runProbe(boolean interactive) throws Exception {
        Path output = directory.resolve("output.txt");
        Process process = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Dterminal.jline=" + interactive, "-Dterminal.ansi=" + interactive,
                "-cp", System.getProperty("console.testClasspath"),
                ConsoleLoggingProbe.class.getName(), System.getProperty("console.logConfig"),
                Boolean.toString(interactive))
                .directory(directory.toFile()).redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            assertTrue(process.waitFor(15, TimeUnit.SECONDS), "Logging probe did not exit");
            String text = Files.readString(output);
            assertEquals(0, process.exitValue(), text);
            return text;
        } finally {
            process.destroyForcibly();
        }
    }

    private static int occurrences(String text, String marker) {
        return text.split(marker, -1).length - 1;
    }
}
