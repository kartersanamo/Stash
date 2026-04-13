package com.kartersanamo.stash.api.command;

import com.kartersanamo.stash.Stash;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

public class CommandManager {
    private final Stash plugin;

    public CommandManager(Stash plugin) {
        this.plugin = plugin;
    }

    public void register(String commandName, CommandExecutor executor) {
        PluginCommand command = plugin.getCommand(commandName);
        if (command == null) {
            plugin.getLogger().warning("Command not found in plugin.yml: " + commandName);
            return;
        }

        command.setExecutor(executor);
        if (executor instanceof TabCompleter tabCompleter) {
            command.setTabCompleter(tabCompleter);
        }
    }
}
