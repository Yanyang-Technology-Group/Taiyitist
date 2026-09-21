package org.bukkit.craftbukkit.v1_20_R1.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.junit.jupiter.api.Test;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import static org.junit.jupiter.api.Assertions.*;

class ConsoleHighlighterTest {
    @Test
    void coloursParsedArgumentsAndInvalidInputWithoutChangingText() {
        var dispatcher = new CommandDispatcher<Object>();
        dispatcher.register(literal("time").then(literal("set")
                .then(argument("ticks", IntegerArgumentType.integer(0)).executes(context -> 1))));
        var highlighter = new ConsoleHighlighter(line -> dispatcher.parse(line, new Object()));
        AttributedString valid = highlighter.highlight(null, "time set 1200");
        assertEquals("time set 1200", valid.toString());
        assertEquals(AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN), valid.styleAt(9));
        AttributedString invalid = highlighter.highlight(null, "time set invalid");
        assertEquals(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED), invalid.styleAt(9));
        assertEquals("time set invalid", invalid.toString());
    }
}
