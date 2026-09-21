package org.teneted.taiyitist.console;

import java.util.List;
import java.util.regex.Pattern;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.layout.PatternLayout;

/** Renders legacy plugin colours at the output boundary, keeping files and panels plain. */
@Plugin(name = "TaiyitistConsoleText", category = PatternConverter.CATEGORY)
@ConverterKeys("consoleText")
public final class ConsoleTextConverter extends LogEventPatternConverter {
    private static final Pattern ANSI = Pattern.compile("\u001B(?:\\[[0-?]*[ -/]*[@-~]|\\][^\u0007\u001B]*(?:\u0007|\u001B\\\\))");
    private static final String CODES = "0123456789abcdefklmnor";
    private static final Pattern TRAILING_NEWLINE = Pattern.compile("(\\r?\\n)(?:\u001b\\[[0-9;]*m)*$");
    private static final String[] STYLES = {
            "0;30", "0;34", "0;32", "0;36", "0;31", "0;35", "0;33", "0;37",
            "0;90", "0;94", "0;92", "0;96", "0;91", "0;95", "0;93", "0;97",
            "", "1", "9", "4", "3", "0"
    };
    private final List<PatternFormatter> formatters;
    private final boolean strip;

    private ConsoleTextConverter(List<PatternFormatter> formatters, boolean strip) {
        super("consoleText", null);
        this.formatters = formatters;
        this.strip = strip;
    }

    public static ConsoleTextConverter newInstance(Configuration configuration, String[] options) {
        if (options == null || options.length == 0) {
            return null;
        }
        return new ConsoleTextConverter(PatternLayout.createPatternParser(configuration).parse(options[0]),
                options.length > 1 && "strip".equalsIgnoreCase(options[1]));
    }

    @Override
    public void format(LogEvent event, StringBuilder output) {
        int start = output.length();
        StringBuilder formatted = new StringBuilder();
        for (PatternFormatter formatter : formatters) {
            formatter.format(event, formatted);
        }
        boolean ansi = !strip && TerminalConsoleAppender.isAnsiSupported();
        String text = ansi ? formatted.toString() : ANSI.matcher(formatted).replaceAll("");
        boolean styled = ansi && text.indexOf('\u001b') >= 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '§' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                if (code == 'x' && i + 13 < text.length()) {
                    int rgb = 0;
                    boolean valid = true;
                    for (int digit = 0; digit < 6; digit++) {
                        int pos = i + 2 + digit * 2;
                        int value = Character.digit(text.charAt(pos + 1), 16);
                        if (text.charAt(pos) != '§' || value < 0) {
                            valid = false;
                            break;
                        }
                        rgb = (rgb << 4) | value;
                    }
                    if (valid) {
                        if (ansi) {
                            output.append("\u001b[0;38;2;").append(rgb >> 16).append(';')
                                    .append((rgb >> 8) & 255).append(';').append(rgb & 255).append('m');
                            styled = true;
                        }
                        i += 13;
                        continue;
                    }
                }
                int style = CODES.indexOf(code);
                if (style >= 0) {
                    if (ansi && !STYLES[style].isEmpty()) {
                        output.append("\u001b[").append(STYLES[style]).append('m');
                        styled = true;
                    }
                    i++;
                    continue;
                }
            }
            output.append(text.charAt(i));
        }
        if (styled) {
            var newline = TRAILING_NEWLINE.matcher(output.substring(start));
            if (newline.find()) {
                // printAbove must see a real trailing newline, not several reset codes.
                output.setLength(start + newline.start());
                output.append("\u001b[0m").append(newline.group(1));
            } else {
                output.append("\u001b[0m");
            }
        }
    }

    @Override
    public boolean handlesThrowable() {
        return formatters.stream().anyMatch(PatternFormatter::handlesThrowable);
    }
}
