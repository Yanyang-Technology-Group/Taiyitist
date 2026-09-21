package org.bukkit.craftbukkit.v1_20_R1.util;

import com.mojang.logging.LogQueues;
import java.nio.file.Path;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.io.IoBuilder;
import org.jline.reader.LineReaderBuilder;

/** Runs in a fresh VM because the appender captures stdout and terminal properties on first use. */
public final class ConsoleLoggingProbe {
    public static void main(String[] args) throws Exception {
        var stdout = System.out;
        var stderr = System.err;
        try (var context = Configurator.initialize("console-test", null, Path.of(args[0]).toUri())) {
            var logger = context.getLogger("ConsoleProbe");
            logger.info("startup-marker");
            if (Boolean.parseBoolean(args[1]) && TerminalConsoleAppender.getTerminal() != null) {
                TerminalConsoleAppender.setReader(LineReaderBuilder.builder()
                        .terminal(TerminalConsoleAppender.getTerminal()).build());
            }
            stdout.println("reader-attached=" + (TerminalConsoleAppender.getReader() != null));
            System.setOut(IoBuilder.forLogger(context.getRootLogger()).buildPrintStream());
            System.setErr(IoBuilder.forLogger(context.getRootLogger()).buildPrintStream());
            System.out.println("stdout-marker");
            logger.warn("§aruntime-marker§r");
            logger.error("§x§f§f§0§0§8§0rgb \u001b[31mansi\u001b[0m", new IllegalStateException("test-stack-trace"));
            context.reconfigure();
            logger.info("reconfigured-marker");
            TerminalConsoleAppender.setReader(null);
            logger.info("shutdown-marker");
            stdout.println("pending-terminal-events=" + LogQueues.getOrCreateQueue("TerminalConsole").size());
        } finally {
            TerminalConsoleAppender.close();
            System.setOut(stdout);
            System.setErr(stderr);
        }
    }
}
