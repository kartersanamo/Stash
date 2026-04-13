package com.kartersanamo.stash.listeners;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.api.chat.ColorUtil;
import com.kartersanamo.stash.gui.AdminGUI;
import com.kartersanamo.stash.gui.VaultGUI;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminGUIListener implements Listener {
    private final Stash plugin;
    private final AdminGUI adminGUI;
    private final VaultGUI vaultGUI;
    private final Map<UUID, Integer> playerBrowserPage = new HashMap<>();
    private final Map<UUID, Integer> auditPage = new HashMap<>();
    private final Map<UUID, UUID> profileTarget = new HashMap<>();
    private final Map<UUID, Integer> profileLockPage = new HashMap<>();
    private final Map<UUID, PendingAction> pendingActions = new HashMap<>();

    public AdminGUIListener(Stash plugin) {
        this.plugin = plugin;
        this.adminGUI = new AdminGUI(plugin);
        this.vaultGUI = new VaultGUI(plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (!title.startsWith("Stash Admin")) {
            return;
        }
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) {
            return;
        }
        String name = event.getCurrentItem().getItemMeta().getDisplayName().replace("§", "&");
        String plain = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        if (title.equals(AdminGUI.MAIN_TITLE)) {
            if (plain.contains("Vault Browser")) {
                playerBrowserPage.put(player.getUniqueId(), 1);
                adminGUI.openPlayers(player, 1);
            } else if (plain.contains("Audit Explorer")) {
                auditPage.put(player.getUniqueId(), 1);
                adminGUI.openAudit(player, 1);
            } else if (plain.contains("Backups")) {
                adminGUI.openBackups(player);
            } else if (plain.contains("System Actions")) {
                pendingActions.put(player.getUniqueId(), new PendingAction(ActionType.RELOAD, null));
                adminGUI.openConfirm(player, "Reload System", java.util.List.of("§7Reload configs/messages/data safely."));
            }
            return;
        }

        if (title.startsWith(AdminGUI.PLAYERS_TITLE)) {
            int currentPage = playerBrowserPage.getOrDefault(player.getUniqueId(), 1);
            if (plain.contains("Previous")) {
                currentPage = Math.max(1, currentPage - 1);
                playerBrowserPage.put(player.getUniqueId(), currentPage);
                adminGUI.openPlayers(player, currentPage);
                return;
            }
            if (plain.contains("Next")) {
                currentPage = currentPage + 1;
                playerBrowserPage.put(player.getUniqueId(), currentPage);
                adminGUI.openPlayers(player, currentPage);
                return;
            }
            if (plain.contains("Back")) {
                adminGUI.openMain(player);
                return;
            }
            String playerName = plain.trim();
            var target = adminGUI.resolvePlayerByName(playerName);
            profileTarget.put(player.getUniqueId(), target.getUniqueId());
            profileLockPage.put(player.getUniqueId(), 1);
            adminGUI.openProfile(player, target.getUniqueId(), 1);
            return;
        }

        if (title.startsWith(AdminGUI.PROFILE_TITLE)) {
            UUID targetId = profileTarget.get(player.getUniqueId());
            if (targetId == null) {
                adminGUI.openPlayers(player, playerBrowserPage.getOrDefault(player.getUniqueId(), 1));
                return;
            }
            int lockPage = profileLockPage.getOrDefault(player.getUniqueId(), 1);
            OfflinePlayer target = plugin.getServer().getOfflinePlayer(targetId);
            int currentPages = plugin.getVaultManager().getAccessiblePages(targetId, target.getPlayer());
            int currentRows = plugin.getVaultManager().getRows(targetId, target.getPlayer());

            if (plain.contains("Pages -1")) {
                plugin.getVaultManager().setPagesOverride(targetId, Math.max(1, currentPages - 1));
                adminGUI.openProfile(player, targetId, lockPage);
                return;
            }
            if (plain.contains("Pages +1")) {
                plugin.getVaultManager().setPagesOverride(targetId,
                        Math.min(plugin.getConfigUtil().getMaxPages(), currentPages + 1));
                adminGUI.openProfile(player, targetId, lockPage);
                return;
            }
            if (plain.contains("Rows -1")) {
                plugin.getVaultManager().setRowsOverride(targetId, Math.max(1, currentRows - 1));
                adminGUI.openProfile(player, targetId, lockPage);
                return;
            }
            if (plain.contains("Rows +1")) {
                plugin.getVaultManager().setRowsOverride(targetId, Math.min(6, currentRows + 1));
                adminGUI.openProfile(player, targetId, lockPage);
                return;
            }
            if (plain.contains("Lock Page -1")) {
                profileLockPage.put(player.getUniqueId(), Math.max(1, lockPage - 1));
                adminGUI.openProfile(player, targetId, profileLockPage.get(player.getUniqueId()));
                return;
            }
            if (plain.contains("Lock Page +1")) {
                int maxPages = plugin.getVaultManager().getAccessiblePages(targetId, target.getPlayer());
                profileLockPage.put(player.getUniqueId(), Math.min(maxPages, lockPage + 1));
                adminGUI.openProfile(player, targetId, profileLockPage.get(player.getUniqueId()));
                return;
            }
            if (plain.contains("Locked Page") || plain.contains("Unlocked Page")) {
                boolean locked = plugin.getVaultManager().isPageLocked(targetId, lockPage);
                plugin.getVaultManager().setPageLocked(targetId, lockPage, !locked);
                adminGUI.openProfile(player, targetId, lockPage);
                return;
            }
            if (plain.contains("Inspect Vault")) {
                int rows = plugin.getVaultManager().getRows(targetId, target.getPlayer());
                int maxPages = plugin.getVaultManager().getAccessiblePages(targetId, target.getPlayer());
                String nameToUse = target.getName() == null ? targetId.toString() : target.getName();
                vaultGUI.open(player, targetId, 1, maxPages, rows, nameToUse, true);
                return;
            }
            if (plain.contains("Back")) {
                adminGUI.openPlayers(player, playerBrowserPage.getOrDefault(player.getUniqueId(), 1));
            }
            return;
        }

        if (title.startsWith(AdminGUI.AUDIT_TITLE)) {
            int current = auditPage.getOrDefault(player.getUniqueId(), 1);
            if (plain.contains("Previous")) {
                current = Math.max(1, current - 1);
                auditPage.put(player.getUniqueId(), current);
                adminGUI.openAudit(player, current);
                return;
            }
            if (plain.contains("Next")) {
                current = current + 1;
                auditPage.put(player.getUniqueId(), current);
                adminGUI.openAudit(player, current);
                return;
            }
            if (plain.contains("Back")) {
                adminGUI.openMain(player);
                return;
            }
            if (plain.contains("Export CSV")) {
                File exportDir = new File(plugin.getDataFolder(), "exports");
                if (!exportDir.exists() && !exportDir.mkdirs()) {
                    plugin.getMessagesUtil().send(player, "admin.audit-export-failed");
                    return;
                }
                File output = new File(exportDir, "audit-gui-" + System.currentTimeMillis() + ".csv");
                try {
                    int count = plugin.getAuditManager().exportCsv(output, null, null);
                    plugin.getMessagesUtil().send(player, "admin.audit-export-success",
                            "%value%", output.getName() + " (" + count + " events)");
                } catch (IOException exception) {
                    plugin.getMessagesUtil().send(player, "admin.audit-export-failed");
                }
            }
            return;
        }

        if (title.equals(AdminGUI.BACKUPS_TITLE)) {
            if (plain.contains("Create Backup")) {
                try {
                    String timestamp = plugin.getBackupManager().createBackup();
                    plugin.getMessagesUtil().send(player, "stash.backup-created", "%value%", timestamp);
                } catch (IOException exception) {
                    plugin.getMessagesUtil().send(player, "stash.backup-failed");
                }
                adminGUI.openBackups(player);
                return;
            }
            if (plain.contains("Restore Latest Snapshot")) {
                String latest = plugin.getBackupManager().getLatestBackupName();
                if (latest == null) {
                    plugin.getMessagesUtil().send(player, "stash.restore-missing");
                    return;
                }
                pendingActions.put(player.getUniqueId(), new PendingAction(ActionType.RESTORE_BACKUP, latest));
                var preview = plugin.getBackupManager().previewRestore(latest);
                adminGUI.openConfirm(player, "Restore Latest Snapshot",
                        java.util.List.of("§7" + latest,
                                "§7Players: §f" + preview.current().players() + " -> " + preview.backup().players(),
                                "§7Pages: §f" + preview.current().pages() + " -> " + preview.backup().pages(),
                                "§7Items: §f" + preview.current().items() + " -> " + preview.backup().items()));
                return;
            }
            if (plain.contains("Rollback History")) {
                plugin.getMessagesUtil().send(player, "stash.backup-list", "%value%", "");
                for (String row : plugin.getBackupManager().listBackupIndexSummaries()) {
                    player.sendMessage(ColorUtil.color("&7- &f" + row));
                }
                return;
            }
            if (plain.contains("Back")) {
                adminGUI.openMain(player);
                return;
            }
            String backupName = plain.trim();
            var preview = plugin.getBackupManager().previewRestore(backupName);
            if (!preview.exists()) {
                plugin.getMessagesUtil().send(player, "stash.restore-missing");
                return;
            }
            pendingActions.put(player.getUniqueId(), new PendingAction(ActionType.RESTORE_BACKUP, backupName));
            adminGUI.openConfirm(player, "Restore Backup",
                    java.util.List.of("§7" + backupName,
                            "§7Players: §f" + preview.current().players() + " -> " + preview.backup().players(),
                            "§7Pages: §f" + preview.current().pages() + " -> " + preview.backup().pages(),
                            "§7Items: §f" + preview.current().items() + " -> " + preview.backup().items()));
            return;
        }

        if (title.equals(AdminGUI.CONFIRM_TITLE)) {
            if (plain.contains("Cancel")) {
                pendingActions.remove(player.getUniqueId());
                adminGUI.openMain(player);
                return;
            }
            if (!plain.contains("Confirm")) {
                return;
            }
            PendingAction pending = pendingActions.remove(player.getUniqueId());
            if (pending == null) {
                adminGUI.openMain(player);
                return;
            }
            if (pending.type == ActionType.RELOAD) {
                plugin.reloadPlugin();
                plugin.getMessagesUtil().send(player, "stash.reloaded");
                adminGUI.openMain(player);
                return;
            }
            if (pending.type == ActionType.RESTORE_BACKUP && pending.value != null) {
                try {
                    boolean restored = plugin.getBackupManager().restoreBackup(pending.value);
                    if (!restored) {
                        plugin.getMessagesUtil().send(player, "stash.restore-missing");
                    } else {
                        plugin.getVaultManager().load();
                        plugin.getMessagesUtil().send(player, "stash.restore-success", "%value%", pending.value);
                    }
                } catch (IOException exception) {
                    plugin.getMessagesUtil().send(player, "stash.restore-failed");
                }
                adminGUI.openBackups(player);
            }
        }
    }

    private record PendingAction(ActionType type, String value) {
    }

    private enum ActionType {
        RELOAD,
        RESTORE_BACKUP
    }
}
