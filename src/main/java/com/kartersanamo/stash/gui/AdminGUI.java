package com.kartersanamo.stash.gui;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.api.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class AdminGUI {
    public static final String MAIN_TITLE = "Stash Admin Control Center";
    public static final String PLAYERS_TITLE = "Stash Admin Players";
    public static final String BACKUPS_TITLE = "Stash Admin Backups";
    public static final String AUDIT_TITLE = "Stash Admin Audits";
    private final Stash plugin;

    public AdminGUI(Stash plugin) {
        this.plugin = plugin;
    }

    public void openMain(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, MAIN_TITLE);
        inventory.setItem(10, new ItemBuilder(Material.CHEST).name("§bVault Browser").build());
        inventory.setItem(12, new ItemBuilder(Material.WRITABLE_BOOK).name("§eAudit Explorer").build());
        inventory.setItem(14, new ItemBuilder(Material.ENDER_CHEST).name("§dBackups & Restore").build());
        inventory.setItem(16, new ItemBuilder(Material.REDSTONE).name("§aSystem Actions").build());
        player.openInventory(inventory);
    }

    public void openPlayers(Player player, int page) {
        List<String> playerNames = new ArrayList<>(plugin.getVaultManager().listLoadedPlayers());
        int pageSize = 45;
        int totalPages = Math.max(1, (int) Math.ceil((double) playerNames.size() / pageSize));
        int safePage = Math.max(1, Math.min(page, totalPages));
        int from = (safePage - 1) * pageSize;
        int to = Math.min(from + pageSize, playerNames.size());

        Inventory inventory = Bukkit.createInventory(null, 54, PLAYERS_TITLE + " [" + safePage + "/" + totalPages + "]");
        for (int i = from; i < to; i++) {
            String name = playerNames.get(i);
            inventory.setItem(i - from, new ItemBuilder(Material.PLAYER_HEAD).name("§f" + name).build());
        }
        inventory.setItem(45, new ItemBuilder(Material.ARROW).name("§ePrevious").build());
        inventory.setItem(49, new ItemBuilder(Material.BARRIER).name("§cBack").build());
        inventory.setItem(53, new ItemBuilder(Material.ARROW).name("§eNext").build());
        player.openInventory(inventory);
    }

    public void openBackups(Player player) {
        List<String> backups = plugin.getBackupManager().listBackups();
        Inventory inventory = Bukkit.createInventory(null, 54, BACKUPS_TITLE);
        int index = 0;
        for (String backup : backups.reversed()) {
            if (index >= 45) {
                break;
            }
            inventory.setItem(index++, new ItemBuilder(Material.PAPER).name("§f" + backup).build());
        }
        inventory.setItem(45, new ItemBuilder(Material.ANVIL).name("§aCreate Backup").build());
        inventory.setItem(46, new ItemBuilder(Material.TOTEM_OF_UNDYING).name("§cRestore Latest Snapshot").build());
        inventory.setItem(49, new ItemBuilder(Material.BARRIER).name("§cBack").build());
        player.openInventory(inventory);
    }

    public void openAudit(Player player, int page) {
        var auditPage = plugin.getAuditManager().getFilteredPage(page, 28, null, null);
        Inventory inventory = Bukkit.createInventory(null, 45, AUDIT_TITLE + " [" + auditPage.page() + "/" + auditPage.totalPages() + "]");
        int slot = 0;
        for (String line : auditPage.lines()) {
            if (slot >= 28) {
                break;
            }
            inventory.setItem(slot++, new ItemBuilder(Material.BOOK).name("§fAudit Event")
                    .lore(List.of(line.replace('&', '§')))
                    .build());
        }
        inventory.setItem(36, new ItemBuilder(Material.ARROW).name("§ePrevious").build());
        inventory.setItem(40, new ItemBuilder(Material.BARRIER).name("§cBack").build());
        inventory.setItem(44, new ItemBuilder(Material.ARROW).name("§eNext").build());
        player.openInventory(inventory);
    }

    public OfflinePlayer resolvePlayerByName(String name) {
        return Bukkit.getOfflinePlayer(name);
    }
}
