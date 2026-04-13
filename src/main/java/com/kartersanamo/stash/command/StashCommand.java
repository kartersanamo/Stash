package com.kartersanamo.stash.command;

import com.kartersanamo.stash.Stash;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.io.IOException;
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

        if (!sender.hasPermission("stash.reload")) {
            plugin.getMessagesUtil().send(sender, "general.no-permission");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            plugin.getMessagesUtil().send(sender, "stash.reloaded");
            return true;
        }

        if (args[0].equalsIgnoreCase("backup")) {
            try {
                String timestamp = plugin.getBackupManager().createBackup();
                plugin.getMessagesUtil().send(sender, "stash.backup-created", "%value%", timestamp);
            } catch (IOException exception) {
                plugin.getMessagesUtil().send(sender, "stash.backup-failed");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("backups")) {
            List<String> backups = plugin.getBackupManager().listBackups();
            plugin.getMessagesUtil().send(sender, "stash.backup-list", "%value%",
                    backups.isEmpty() ? "none" : String.join(", ", backups));
            return true;
        }

        if (args[0].equalsIgnoreCase("restore")) {
            if (args.length < 2) {
                plugin.getMessagesUtil().send(sender, "stash.restore-usage");
                return true;
            }
            try {
                boolean restored = plugin.getBackupManager().restoreBackup(args[1]);
                if (!restored) {
                    plugin.getMessagesUtil().send(sender, "stash.restore-missing");
                    return true;
                }
                plugin.getVaultManager().load();
                plugin.getMessagesUtil().send(sender, "stash.restore-success", "%value%", args[1]);
            } catch (IOException exception) {
                plugin.getMessagesUtil().send(sender, "stash.restore-failed");
            }
            return true;
        }

        plugin.getMessagesUtil().send(sender, "stash.usage");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "backup", "backups", "restore");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("restore")) {
            return plugin.getBackupManager().listBackups();
        }
        return List.of();
    }
}
