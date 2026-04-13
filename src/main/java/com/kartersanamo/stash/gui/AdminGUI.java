package com.kartersanamo.stash.gui;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.api.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminGUI {
    public static final String MAIN_TITLE = "Stash Admin Control Center";
    public static final String PLAYERS_TITLE = "Stash Admin Players";
    public static final String BACKUPS_TITLE = "Stash Admin Backups";
    public static final String AUDIT_TITLE = "Stash Admin Audits";
    public static final String PROFILE_TITLE = "Stash Admin Profile";
    public static final String CONFIRM_TITLE = "Stash Admin Confirm";
    private final Stash plugin;

    public AdminGUI(Stash plugin) {
        this.plugin = plugin;
    }

    public void openMain(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, MAIN_TITLE);
        applyFrame(inventory, Material.BLUE_STAINED_GLASS_PANE);
        int playerCount = plugin.getVaultManager().listLoadedPlayers().size();
        int backupCount = plugin.getBackupManager().listBackups().size();
        int auditCount = plugin.getAuditManager().getTotalEvents();

        inventory.setItem(10, new ItemBuilder(Material.CHEST).name("§bVault Browser")
                .lore(List.of("§7Browse all known player vaults.", "§7Profiles: §f" + playerCount))
                .build());
        inventory.setItem(12, new ItemBuilder(Material.WRITABLE_BOOK).name("§eAudit Explorer")
                .lore(List.of("§7Inspect audit events in pages.", "§7Logged events: §f" + auditCount))
                .build());
        inventory.setItem(14, new ItemBuilder(Material.ENDER_CHEST).name("§dBackups & Restore")
                .lore(List.of("§7Manage snapshots safely.", "§7Snapshots: §f" + backupCount))
                .build());
        inventory.setItem(16, new ItemBuilder(Material.REDSTONE).name("§aSystem Actions")
                .lore(List.of("§7Reload plugin resources", "§7with confirmation prompts."))
                .build());
        player.openInventory(inventory);
    }

    public void openPlayers(Player player, int page) {
        List<String> playerNames = new ArrayList<>(plugin.getVaultManager().listLoadedPlayers());
        int pageSize = 28;
        int totalPages = Math.max(1, (int) Math.ceil((double) playerNames.size() / pageSize));
        int safePage = Math.max(1, Math.min(page, totalPages));
        int from = (safePage - 1) * pageSize;
        int to = Math.min(from + pageSize, playerNames.size());

        Inventory inventory = Bukkit.createInventory(null, 54, PLAYERS_TITLE + " [" + safePage + "/" + totalPages + "]");
        applyFrame(inventory, Material.GRAY_STAINED_GLASS_PANE);
        List<Integer> contentSlots = getInteriorContentSlots(inventory.getSize());
        int slotIndex = 0;
        for (int i = from; i < to; i++) {
            String name = playerNames.get(i);
            OfflinePlayer target = resolvePlayerByName(name);
            int pages = plugin.getVaultManager().getAccessiblePages(target.getUniqueId(), target.getPlayer());
            int rows = plugin.getVaultManager().getRows(target.getUniqueId(), target.getPlayer());
            inventory.setItem(contentSlots.get(slotIndex++), new ItemBuilder(Material.PLAYER_HEAD).name("§f" + name)
                    .lore(List.of("§7Pages: §f" + pages, "§7Rows: §f" + rows, "§bClick to manage profile"))
                    .build());
        }
        inventory.setItem(45, new ItemBuilder(Material.ARROW).name("§ePrevious").build());
        inventory.setItem(49, new ItemBuilder(Material.BARRIER).name("§cBack").build());
        inventory.setItem(53, new ItemBuilder(Material.ARROW).name("§eNext").build());
        player.openInventory(inventory);
    }

    public void openProfile(Player player, UUID targetId, int lockPage) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);
        String targetName = target.getName() == null ? targetId.toString() : target.getName();
        int effectivePages = plugin.getVaultManager().getAccessiblePages(targetId, target.getPlayer());
        int effectiveRows = plugin.getVaultManager().getRows(targetId, target.getPlayer());
        int safeLockPage = Math.max(1, Math.min(lockPage, effectivePages));
        boolean locked = plugin.getVaultManager().isPageLocked(targetId, safeLockPage);
        Integer pagesOverride = plugin.getVaultManager().getPagesOverrideValue(targetId);
        Integer rowsOverride = plugin.getVaultManager().getRowsOverrideValue(targetId);

        Inventory inventory = Bukkit.createInventory(null, 54, PROFILE_TITLE + " [" + targetName + "]");
        applyFrame(inventory, Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        inventory.setItem(4, new ItemBuilder(Material.PLAYER_HEAD).name("§b" + targetName)
                .lore(List.of("§7UUID: §f" + targetId, "§7Pages override: §f" + (pagesOverride == null ? "none" : pagesOverride),
                        "§7Rows override: §f" + (rowsOverride == null ? "none" : rowsOverride)))
                .build());

        inventory.setItem(19, new ItemBuilder(Material.RED_DYE).name("§cPages -1").build());
        inventory.setItem(20, new ItemBuilder(Material.BOOK).name("§eEffective Pages: §f" + effectivePages)
                .lore(List.of("§7Click +/- to change override")).build());
        inventory.setItem(21, new ItemBuilder(Material.LIME_DYE).name("§aPages +1").build());

        inventory.setItem(28, new ItemBuilder(Material.RED_DYE).name("§cRows -1").build());
        inventory.setItem(29, new ItemBuilder(Material.CHEST).name("§eEffective Rows: §f" + effectiveRows)
                .lore(List.of("§7Click +/- to change override")).build());
        inventory.setItem(30, new ItemBuilder(Material.LIME_DYE).name("§aRows +1").build());

        inventory.setItem(23, new ItemBuilder(Material.ARROW).name("§eLock Page -1").build());
        inventory.setItem(24, new ItemBuilder(Material.IRON_DOOR)
                .name(locked ? "§cLocked Page: §f" + safeLockPage : "§aUnlocked Page: §f" + safeLockPage)
                .lore(List.of("§7Click to toggle lock state")).build());
        inventory.setItem(25, new ItemBuilder(Material.ARROW).name("§eLock Page +1").build());

        inventory.setItem(33, new ItemBuilder(Material.ENDER_EYE).name("§bInspect Vault")
                .lore(List.of("§7Open read-only vault inspector")).build());
        inventory.setItem(49, new ItemBuilder(Material.BARRIER).name("§cBack").build());
        player.openInventory(inventory);
    }

    public void openBackups(Player player) {
        List<String> backups = plugin.getBackupManager().listBackups();
        Inventory inventory = Bukkit.createInventory(null, 54, BACKUPS_TITLE);
        applyFrame(inventory, Material.PURPLE_STAINED_GLASS_PANE);
        List<Integer> contentSlots = getInteriorContentSlots(inventory.getSize());
        int index = 0;
        for (String backup : backups.reversed()) {
            if (index >= contentSlots.size()) {
                break;
            }
            var preview = plugin.getBackupManager().previewRestore(backup);
            inventory.setItem(contentSlots.get(index++), new ItemBuilder(Material.PAPER).name("§f" + backup)
                    .lore(List.of("§7Players: §f" + preview.current().players() + " -> " + preview.backup().players(),
                            "§7Pages: §f" + preview.current().pages() + " -> " + preview.backup().pages(),
                            "§7Items: §f" + preview.current().items() + " -> " + preview.backup().items(),
                            "§cClick to restore (confirmation required)"))
                    .build());
        }
        inventory.setItem(45, new ItemBuilder(Material.ANVIL).name("§aCreate Backup").build());
        inventory.setItem(46, new ItemBuilder(Material.TOTEM_OF_UNDYING).name("§cRestore Latest Snapshot")
                .lore(List.of("§7One-click rollback target", "§cRequires confirmation")).build());
        inventory.setItem(47, new ItemBuilder(Material.CLOCK).name("§eRollback History")
                .lore(List.of("§7Shows indexed snapshot history")).build());
        inventory.setItem(49, new ItemBuilder(Material.BARRIER).name("§cBack").build());
        player.openInventory(inventory);
    }

    public void openAudit(Player player, int page) {
        var auditPage = plugin.getAuditManager().getFilteredPage(page, 28, null, null);
        Inventory inventory = Bukkit.createInventory(null, 45, AUDIT_TITLE + " [" + auditPage.page() + "/" + auditPage.totalPages() + "]");
        applyFrame(inventory, Material.YELLOW_STAINED_GLASS_PANE);
        List<Integer> contentSlots = getInteriorContentSlots(inventory.getSize());
        int slot = 0;
        for (String line : auditPage.lines()) {
            if (slot >= contentSlots.size()) {
                break;
            }
            inventory.setItem(contentSlots.get(slot++), new ItemBuilder(Material.BOOK).name("§fAudit Event")
                    .lore(List.of(line.replace('&', '§')))
                    .build());
        }
        inventory.setItem(36, new ItemBuilder(Material.ARROW).name("§ePrevious").build());
        inventory.setItem(40, new ItemBuilder(Material.BARRIER).name("§cBack").build());
        inventory.setItem(42, new ItemBuilder(Material.WRITABLE_BOOK).name("§aExport CSV")
                .lore(List.of("§7Export current audit history")).build());
        inventory.setItem(44, new ItemBuilder(Material.ARROW).name("§eNext").build());
        player.openInventory(inventory);
    }

    public void openConfirm(Player player, String actionName, List<String> details) {
        Inventory inventory = Bukkit.createInventory(null, 27, CONFIRM_TITLE);
        applyFrame(inventory, Material.RED_STAINED_GLASS_PANE);
        inventory.setItem(13, new ItemBuilder(Material.PAPER).name("§e" + actionName).lore(details).build());
        inventory.setItem(11, new ItemBuilder(Material.LIME_WOOL).name("§aConfirm").build());
        inventory.setItem(15, new ItemBuilder(Material.RED_WOOL).name("§cCancel").build());
        player.openInventory(inventory);
    }

    public OfflinePlayer resolvePlayerByName(String name) {
        return Bukkit.getOfflinePlayer(ChatColor.stripColor(name));
    }

    private void applyFrame(Inventory inventory, Material material) {
        ItemStack filler = new ItemBuilder(material).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == (inventory.getSize() / 9) - 1 || col == 0 || col == 8) {
                if (inventory.getItem(i) == null) {
                    inventory.setItem(i, filler);
                }
            }
        }
    }

    private List<Integer> getInteriorContentSlots(int size) {
        List<Integer> slots = new ArrayList<>();
        int rows = size / 9;
        for (int row = 1; row < rows - 1; row++) {
            for (int col = 1; col < 8; col++) {
                slots.add(row * 9 + col);
            }
        }
        return slots;
    }
}
