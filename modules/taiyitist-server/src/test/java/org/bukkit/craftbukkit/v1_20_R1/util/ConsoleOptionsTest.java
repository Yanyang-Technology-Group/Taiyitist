package org.bukkit.craftbukkit.v1_20_R1.util;

import org.bukkit.craftbukkit.Main;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleOptionsTest {
    @Test
    void inputOptionsAreAppliedEvenWhenTheJavaVersionIsNewerThanSupported() {
        String version = System.getProperty("java.class.version");
        boolean useConsole = Main.useConsole;
        boolean useJline = Main.useJline;
        try {
            System.setProperty("java.class.version", "69.0"); // Java 25
            Main parser = new Main();
            Main.useConsole = true;
            Main.useJline = true;
            Main.handleParser(parser, parser.parse("--nojline"));
            assertFalse(Main.useJline);
            assertTrue(Main.useConsole);

            Main.useJline = true;
            Main.handleParser(parser, parser.parse("--noconsole"));
            assertFalse(Main.useConsole);
            assertFalse(Main.useJline);
        } finally {
            System.setProperty("java.class.version", version);
            Main.useConsole = useConsole;
            Main.useJline = useJline;
        }
    }
}
