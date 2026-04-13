package com.kartersanamo.stash.command;

import com.kartersanamo.stash.Stash;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class StashCommand implements CommandExecutor, TabCompleter {
    private final Stash plugin;

    public StashCommand(Stash plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            plugin.getMessagesUtil().send(sender, "stash.usage");
            return true;
        }

        if (!args[0].equalsIgnoreCase("reload")) {
            plugin.getMessagesUtil().send(sender, "stash.usage");
            return true;
        }

        if (!sender.hasPermission("stash.reload")) {
            plugin.getMessagesUtil().send(sender, "general.no-permission");
            return true;
        }

        plugin.reloadPlugin();
        plugin.getMessagesUtil().send(sender, "stash.reloaded");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }
        return List.of();
    }
}
