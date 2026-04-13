package com.kartersanamo.stash.command;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.api.chat.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StashCommand implements CommandExecutor, TabCompleter {
    private final Stash plugin;
    private final Map<String, PendingRestore> pendingRestores = new HashMap<>();

    public StashCommand(Stash plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            plugin.getMessagesUtil().send(sender, "stash.usage");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("stash.reload")) {
                plugin.getMessagesUtil().send(sender, "general.no-permission");
                return true;
            }
            plugin.reloadPlugin();
            plugin.getMessagesUtil().send(sender, "stash.reloaded");
            return true;
        }

        if (args[0].equalsIgnoreCase("backup")) {
            if (!sender.hasPermission("stash.admin.restore")) {
                plugin.getMessagesUtil().send(sender, "general.no-permission");
                return true;
            }
            try {
                String timestamp = plugin.getBackupManager().createBackup();
                plugin.getMessagesUtil().send(sender, "stash.backup-created", "%value%", timestamp);
            } catch (IOException exception) {
                plugin.getMessagesUtil().send(sender, "stash.backup-failed");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("backups")) {
            if (!sender.hasPermission("stash.admin.restore")) {
                plugin.getMessagesUtil().send(sender, "general.no-permission");
                return true;
            }
            List<String> backups = plugin.getBackupManager().listBackupIndexSummaries();
            if (backups.isEmpty()) {
                backups = plugin.getBackupManager().listBackups();
            }
            plugin.getMessagesUtil().send(sender, "stash.backup-list", "%value%", backups.isEmpty() ? "none" : "");
            for (String backup : backups) {
                sender.sendMessage(ColorUtil.color("&7- &f" + backup));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("restore")) {
            if (!sender.hasPermission("stash.admin.restore")) {
                plugin.getMessagesUtil().send(sender, "general.no-permission");
                return true;
            }
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

        if (args[0].equalsIgnoreCase("previewrestore")) {
            if (!sender.hasPermission("stash.admin.restore")) {
                plugin.getMessagesUtil().send(sender, "general.no-permission");
                return true;
            }
            if (args.length < 2) {
                plugin.getMessagesUtil().send(sender, "stash.restore-usage");
                return true;
            }
            var preview = plugin.getBackupManager().previewRestore(args[1]);
            if (!preview.exists()) {
                plugin.getMessagesUtil().send(sender, "stash.restore-missing");
                return true;
            }

            pendingRestores.put(sender.getName(), new PendingRestore(args[1], System.currentTimeMillis() + 30000));
            plugin.getMessagesUtil().send(sender, "stash.restore-preview", "%value%",
                    preview.backupName() + " | current players=" + preview.currentPlayerEntries()
                            + ", backup players=" + preview.backupPlayerEntries());
            plugin.getMessagesUtil().send(sender, "stash.restore-preview-confirm");
            return true;
        }

        if (args[0].equalsIgnoreCase("confirmrestore")) {
            if (!sender.hasPermission("stash.admin.restore")) {
                plugin.getMessagesUtil().send(sender, "general.no-permission");
                return true;
            }
            PendingRestore pending = pendingRestores.get(sender.getName());
            if (pending == null || pending.expiresAt() < System.currentTimeMillis()) {
                pendingRestores.remove(sender.getName());
                plugin.getMessagesUtil().send(sender, "stash.restore-confirm-missing");
                return true;
            }
            try {
                boolean restored = plugin.getBackupManager().restoreBackup(pending.backupName());
                if (!restored) {
                    plugin.getMessagesUtil().send(sender, "stash.restore-missing");
                    return true;
                }
                plugin.getVaultManager().load();
                plugin.getMessagesUtil().send(sender, "stash.restore-success", "%value%", pending.backupName());
            } catch (IOException exception) {
                plugin.getMessagesUtil().send(sender, "stash.restore-failed");
            } finally {
                pendingRestores.remove(sender.getName());
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("audit")) {
            if (!sender.hasPermission("stash.admin.audit")) {
                plugin.getMessagesUtil().send(sender, "general.no-permission");
                return true;
            }
            if (args.length >= 2 && args[1].equalsIgnoreCase("export")) {
                String actorFilter = null;
                String actionFilter = null;
                if (args.length >= 3 && !args[2].equalsIgnoreCase("*")) {
                    actorFilter = args[2];
                }
                if (args.length >= 4 && !args[3].equalsIgnoreCase("*")) {
                    actionFilter = args[3];
                }

                File exportDir = new File(plugin.getDataFolder(), "exports");
                if (!exportDir.exists() && !exportDir.mkdirs()) {
                    plugin.getMessagesUtil().send(sender, "admin.audit-export-failed");
                    return true;
                }
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
                File output = new File(exportDir, "audit-" + timestamp + ".csv");
                try {
                    int count = plugin.getAuditManager().exportCsv(output, actorFilter, actionFilter);
                    plugin.getMessagesUtil().send(sender, "admin.audit-export-success", "%value%",
                            output.getName() + " (" + count + " events)");
                } catch (IOException exception) {
                    plugin.getMessagesUtil().send(sender, "admin.audit-export-failed");
                }
                return true;
            }
            int limit = 10;
            int page = 1;
            String actorFilter = null;
            String actionFilter = null;
            if (args.length >= 2) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException exception) {
                    plugin.getMessagesUtil().send(sender, "admin.number-required");
                    return true;
                }
            }
            if (args.length >= 3) {
                try {
                    limit = Integer.parseInt(args[2]);
                } catch (NumberFormatException exception) {
                    plugin.getMessagesUtil().send(sender, "admin.number-required");
                    return true;
                }
            }
            if (args.length >= 4 && !args[3].equalsIgnoreCase("*")) {
                actorFilter = args[3];
            }
            if (args.length >= 5 && !args[4].equalsIgnoreCase("*")) {
                actionFilter = args[4];
            }

            var auditPage = plugin.getAuditManager().getFilteredPage(page, limit, actorFilter, actionFilter);
            plugin.getMessagesUtil().send(sender, "admin.audit-header-page", "%value%",
                    auditPage.page() + "/" + auditPage.totalPages() + " (" + auditPage.totalMatches() + " matches)");
            for (String line : auditPage.lines()) {
                sender.sendMessage(ColorUtil.color(line));
            }
            return true;
        }

        plugin.getMessagesUtil().send(sender, "stash.usage");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "backup", "backups", "restore", "previewrestore", "confirmrestore", "audit");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("restore")) {
            return plugin.getBackupManager().listBackups();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("previewrestore")) {
            return plugin.getBackupManager().listBackups();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("audit")) {
            return List.of("1", "2", "3", "export");
        }
        return List.of();
    }

    private record PendingRestore(String backupName, long expiresAt) {
    }
}
