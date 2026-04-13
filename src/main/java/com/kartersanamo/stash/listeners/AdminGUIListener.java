package com.kartersanamo.stash.listeners;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.gui.AdminGUI;
import com.kartersanamo.stash.gui.VaultGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

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

        if (title.equals(AdminGUI.MAIN_TITLE)) {
            if (name.contains("Vault Browser")) {
                playerBrowserPage.put(player.getUniqueId(), 1);
                adminGUI.openPlayers(player, 1);
            } else if (name.contains("Audit Explorer")) {
                auditPage.put(player.getUniqueId(), 1);
                adminGUI.openAudit(player, 1);
            } else if (name.contains("Backups")) {
                adminGUI.openBackups(player);
            } else if (name.contains("System Actions")) {
                plugin.reloadPlugin();
                plugin.getMessagesUtil().send(player, "stash.reloaded");
            }
            return;
        }

        if (title.startsWith(AdminGUI.PLAYERS_TITLE)) {
            int currentPage = playerBrowserPage.getOrDefault(player.getUniqueId(), 1);
            if (name.contains("Previous")) {
                currentPage = Math.max(1, currentPage - 1);
                playerBrowserPage.put(player.getUniqueId(), currentPage);
                adminGUI.openPlayers(player, currentPage);
                return;
            }
            if (name.contains("Next")) {
                currentPage = currentPage + 1;
                playerBrowserPage.put(player.getUniqueId(), currentPage);
                adminGUI.openPlayers(player, currentPage);
                return;
            }
            if (name.contains("Back")) {
                adminGUI.openMain(player);
                return;
            }
            String playerName = name.replace("&f", "").trim();
            var target = adminGUI.resolvePlayerByName(playerName);
            int rows = plugin.getVaultManager().getRows(target.getUniqueId(), target.getPlayer());
            int maxPages = plugin.getVaultManager().getAccessiblePages(target.getUniqueId(), target.getPlayer());
            vaultGUI.open(player, target.getUniqueId(), 1, maxPages, rows, playerName, true);
            return;
        }

        if (title.startsWith(AdminGUI.AUDIT_TITLE)) {
            int current = auditPage.getOrDefault(player.getUniqueId(), 1);
            if (name.contains("Previous")) {
                current = Math.max(1, current - 1);
                auditPage.put(player.getUniqueId(), current);
                adminGUI.openAudit(player, current);
                return;
            }
            if (name.contains("Next")) {
                current = current + 1;
                auditPage.put(player.getUniqueId(), current);
                adminGUI.openAudit(player, current);
                return;
            }
            if (name.contains("Back")) {
                adminGUI.openMain(player);
            }
            return;
        }

        if (title.equals(AdminGUI.BACKUPS_TITLE)) {
            if (name.contains("Create Backup")) {
                try {
                    String timestamp = plugin.getBackupManager().createBackup();
                    plugin.getMessagesUtil().send(player, "stash.backup-created", "%value%", timestamp);
                } catch (IOException exception) {
                    plugin.getMessagesUtil().send(player, "stash.backup-failed");
                }
                adminGUI.openBackups(player);
                return;
            }
            if (name.contains("Restore Latest Snapshot")) {
                String latest = plugin.getBackupManager().getLatestBackupName();
                if (latest == null) {
                    plugin.getMessagesUtil().send(player, "stash.restore-missing");
                    return;
                }
                try {
                    plugin.getBackupManager().restoreBackup(latest);
                    plugin.getVaultManager().load();
                    plugin.getMessagesUtil().send(player, "stash.restore-success", "%value%", latest);
                } catch (IOException exception) {
                    plugin.getMessagesUtil().send(player, "stash.restore-failed");
                }
                return;
            }
            if (name.contains("Back")) {
                adminGUI.openMain(player);
                return;
            }
            String backupName = name.replace("&f", "").trim();
            var preview = plugin.getBackupManager().previewRestore(backupName);
            if (!preview.exists()) {
                plugin.getMessagesUtil().send(player, "stash.restore-missing");
                return;
            }
            plugin.getMessagesUtil().send(player, "stash.restore-preview", "%value%",
                    preview.backupName() + " | players " + preview.current().players() + "->" + preview.backup().players()
                            + ", pages " + preview.current().pages() + "->" + preview.backup().pages()
                            + ", items " + preview.current().items() + "->" + preview.backup().items());
            try {
                plugin.getBackupManager().restoreBackup(backupName);
                plugin.getVaultManager().load();
                plugin.getMessagesUtil().send(player, "stash.restore-success", "%value%", backupName);
            } catch (IOException exception) {
                plugin.getMessagesUtil().send(player, "stash.restore-failed");
            }
        }
    }
}
