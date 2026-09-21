package org.bukkit.craftbukkit.v1_20_R1.util;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.function.Function;
import org.jline.reader.LineReader;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/** Client-like argument colours, using the server's actual command grammar. */
public final class ConsoleHighlighter extends DefaultHighlighter {
    private static final int[] COLOURS = {
            AttributedStyle.CYAN, AttributedStyle.YELLOW, AttributedStyle.GREEN,
            AttributedStyle.MAGENTA, AttributedStyle.BLUE
    };
    private final Function<String, ParseResults<?>> parser;

    public ConsoleHighlighter(Function<String, ParseResults<?>> parser) {
        this.parser = parser;
    }

    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        if (reader != null && (reader.getSearchTerm() != null || reader.getRegionActive() != LineReader.RegionType.NONE)) {
            return super.highlight(reader, buffer);
        }
        try {
            ParseResults<?> parsed = parser.apply(buffer);
            if (parsed == null) {
                return new AttributedString(buffer);
            }
            var output = new AttributedStringBuilder();
            int position = 0;
            int colour = 0;
            for (CommandContextBuilder<?> context = parsed.getContext(); context != null; context = context.getChild()) {
                for (ParsedCommandNode<?> node : context.getNodes()) {
                    int start = node.getRange().getStart();
                    int end = Math.min(node.getRange().getEnd(), buffer.length());
                    output.append(buffer.substring(position, start), AttributedStyle.DEFAULT);
                    AttributedStyle style = node.getNode() instanceof LiteralCommandNode<?> ? AttributedStyle.DEFAULT
                            : AttributedStyle.DEFAULT.foreground(COLOURS[colour++ % COLOURS.length]);
                    output.append(buffer.substring(start, end), style);
                    position = end;
                }
            }
            output.append(buffer.substring(position), AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            return output.toAttributedString();
        } catch (RuntimeException ignored) {
            // Mod command parsers may be unavailable during startup/reload.
            return new AttributedString(buffer);
        }
    }
}
