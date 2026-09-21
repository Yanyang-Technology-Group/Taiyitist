package org.bukkit.craftbukkit.v1_20_R1.command;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class ColouredConsoleSender extends CraftConsoleCommandSender {
    protected ColouredConsoleSender() {
        super();
    }

    @Override
    public void sendMessage(String message) {
        if (!conversationTracker.isConversingModaly()) {
            sendRawMessage(message);
        }
    }

    public static ConsoleCommandSender getInstance() {
        if (Bukkit.getConsoleSender() != null) {
            return Bukkit.getConsoleSender();
        } else {
            return new ColouredConsoleSender();
        }
    }
}
