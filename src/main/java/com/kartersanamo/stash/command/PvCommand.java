package com.kartersanamo.stash.command;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.gui.PersonalVaultGUI;
import com.kartersanamo.stash.gui.VaultGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PvCommand implements CommandExecutor, TabCompleter {
    private final Stash plugin;
    private final VaultGUI vaultGUI;
    private final PersonalVaultGUI personalVaultGUI;
    private final Map<UUID, PendingClear> pendingClears = new HashMap<>();

    public PvCommand(Stash plugin) {
        this.plugin = plugin;
        this.vaultGUI = new VaultGUI(plugin);
        this.personalVaultGUI = new PersonalVaultGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessagesUtil().send(sender, "general.players-only");
            return true;
        }

        if (!player.hasPermission("stash.use")) {
            plugin.getMessagesUtil().send(player, "general.no-permission");
            return true;
        }

        if (args.length == 0) {
            personalVaultGUI.openList(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            personalVaultGUI.openList(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("search")) {
            if (args.length < 2) {
                plugin.getMessagesUtil().send(player, "vault.search-usage");
                return true;
            }
            String query = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            int rows = plugin.getVaultManager().getRows(player);
            int maxPages = plugin.getVaultManager().getAccessiblePages(player);
            List<Integer> matches = plugin.getVaultManager().searchPages(player.getUniqueId(), rows, maxPages, query);
            if (matches.isEmpty()) {
                plugin.getMessagesUtil().send(player, "vault.search-none", "%query%", query);
            } else {
                plugin.getMessagesUtil().send(player, "vault.search-results",
                        "%value%", matches.toString().replace("[", "").replace("]", ""));
            }
            plugin.getAuditManager().log(player.getUniqueId(), "SEARCH", "query=" + query + ",matches=" + matches.size());
            return true;
        }

        if (args[0].equalsIgnoreCase("lock") || args[0].equalsIgnoreCase("unlock")) {
            if (args.length < 2) {
                plugin.getMessagesUtil().send(player, "vault.lock-usage");
                return true;
            }
            int page;
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                plugin.getMessagesUtil().send(player, "vault.invalid-page");
                return true;
            }
            int unlockedPages = plugin.getVaultManager().getAccessiblePages(player);
            if (page < 1 || page > unlockedPages) {
                plugin.getMessagesUtil().send(player, "vault.page-locked", "%value%", String.valueOf(unlockedPages));
                return true;
            }

            boolean lockState = args[0].equalsIgnoreCase("lock");
            plugin.getVaultManager().setPageLocked(player.getUniqueId(), page, lockState);
            plugin.getMessagesUtil().send(player, lockState ? "vault.locked" : "vault.unlocked",
                    "%value%", String.valueOf(page));
            plugin.getAuditManager().log(player.getUniqueId(), lockState ? "LOCK_PAGE" : "UNLOCK_PAGE", "page=" + page);
            return true;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            if (args.length < 2) {
                plugin.getMessagesUtil().send(player, "vault.clear-usage");
                return true;
            }
            int page;
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                plugin.getMessagesUtil().send(player, "vault.invalid-page");
                return true;
            }
            int unlockedPages = plugin.getVaultManager().getAccessiblePages(player);
            if (page < 1 || page > unlockedPages) {
                plugin.getMessagesUtil().send(player, "vault.page-locked", "%value%", String.valueOf(unlockedPages));
                return true;
            }

            pendingClears.put(player.getUniqueId(), new PendingClear(page, System.currentTimeMillis() + 30000));
            plugin.getMessagesUtil().send(player, "vault.clear-confirm", "%value%", String.valueOf(page));
            return true;
        }

        if (args[0].equalsIgnoreCase("confirmclear")) {
            PendingClear pending = pendingClears.get(player.getUniqueId());
            if (pending == null || pending.expiresAt() < System.currentTimeMillis()) {
                pendingClears.remove(player.getUniqueId());
                plugin.getMessagesUtil().send(player, "vault.clear-none");
                return true;
            }

            int rows = plugin.getVaultManager().getRows(player);
            plugin.getVaultManager().clearPage(player.getUniqueId(), pending.page(), rows);
            plugin.getMessagesUtil().send(player, "vault.cleared", "%value%", String.valueOf(pending.page()));
            plugin.getAuditManager().log(player.getUniqueId(), "CLEAR_PAGE", "page=" + pending.page());
            pendingClears.remove(player.getUniqueId());
            return true;
        }

        if (args[0].equalsIgnoreCase("admin")) {
            return handleAdmin(player, args);
        }

        try {
            int page = Integer.parseInt(args[0]);
            openOwnVault(player, page);
        } catch (NumberFormatException exception) {
            plugin.getMessagesUtil().send(player, "vault.invalid-page");
        }
        return true;
    }

    private boolean handleAdmin(Player player, String[] args) {
        if (!player.hasPermission("stash.admin")) {
            plugin.getMessagesUtil().send(player, "general.no-permission");
            return true;
        }

        if (args.length < 2) {
            plugin.getMessagesUtil().send(player, "admin.usage");
            return true;
        }

        if (args[1].equalsIgnoreCase("inspect")) {
            if (args.length < 3) {
                plugin.getMessagesUtil().send(player, "admin.inspect-usage");
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            int page = 1;
            if (args.length >= 4) {
                try {
                    page = Integer.parseInt(args[3]);
                } catch (NumberFormatException exception) {
                    plugin.getMessagesUtil().send(player, "vault.invalid-page");
                    return true;
                }
            }

            int rows = plugin.getVaultManager().getRows(target.getUniqueId(), target.getPlayer());
            int maxPages = plugin.getVaultManager().getAccessiblePages(target.getUniqueId(), target.getPlayer());
            vaultGUI.open(player, target.getUniqueId(), Math.max(1, page), maxPages, rows, args[2], true);
            plugin.getMessagesUtil().send(player, "admin.inspect-opened", "%player%", args[2]);
            plugin.getAuditManager().log(player.getUniqueId(), "ADMIN_INSPECT", "target=" + args[2] + ",page=" + page);
            return true;
        }

        if (args[1].equalsIgnoreCase("setpages") && args.length >= 4) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            int pages;
            try {
                pages = Math.max(1, Integer.parseInt(args[3]));
            } catch (NumberFormatException exception) {
                plugin.getMessagesUtil().send(player, "admin.number-required");
                return true;
            }
            int maxPages = plugin.getConfigUtil().getMaxPages();
            if (pages > maxPages) {
                pages = maxPages;
            }
            plugin.getVaultManager().setPagesOverride(target.getUniqueId(), pages);
            plugin.getMessagesUtil().send(player, "admin.updated-pages", "%value%", String.valueOf(pages));
            plugin.getAuditManager().log(player.getUniqueId(), "ADMIN_SET_PAGES",
                    "target=" + args[2] + ",value=" + pages);
            return true;
        }

        if (args[1].equalsIgnoreCase("setrows") && args.length >= 4) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            int rows;
            try {
                rows = Integer.parseInt(args[3]);
            } catch (NumberFormatException exception) {
                plugin.getMessagesUtil().send(player, "admin.number-required");
                return true;
            }
            plugin.getVaultManager().setRowsOverride(target.getUniqueId(), rows);
            plugin.getMessagesUtil().send(player, "admin.updated-rows", "%value%", String.valueOf(rows));
            plugin.getAuditManager().log(player.getUniqueId(), "ADMIN_SET_ROWS",
                    "target=" + args[2] + ",value=" + rows);
            return true;
        }

        if (args[1].equalsIgnoreCase("audit")) {
            int limit = 10;
            int page = 1;
            String actorFilter = null;
            String actionFilter = null;
            if (args.length >= 3) {
                try {
                    page = Integer.parseInt(args[2]);
                } catch (NumberFormatException exception) {
                    plugin.getMessagesUtil().send(player, "admin.number-required");
                    return true;
                }
            }
            if (args.length >= 4) {
                try {
                    limit = Integer.parseInt(args[3]);
                } catch (NumberFormatException exception) {
                    plugin.getMessagesUtil().send(player, "admin.number-required");
                    return true;
                }
            }
            if (args.length >= 5 && !args[4].equalsIgnoreCase("*")) {
                actorFilter = args[4];
            }
            if (args.length >= 6 && !args[5].equalsIgnoreCase("*")) {
                actionFilter = args[5];
            }
            var auditPage = plugin.getAuditManager().getFilteredPage(page, limit, actorFilter, actionFilter);
            plugin.getMessagesUtil().send(player, "admin.audit-header-page", "%value%",
                    auditPage.page() + "/" + auditPage.totalPages() + " (" + auditPage.totalMatches() + " matches)");
            for (String line : auditPage.lines()) {
                plugin.getMessagesUtil().sendRaw(player, line, false);
            }
            plugin.getAuditManager().log(player.getUniqueId(), "ADMIN_AUDIT_VIEW",
                    "page=" + page + ",limit=" + limit + ",actor=" + actorFilter + ",action=" + actionFilter);
            return true;
        }

        plugin.getMessagesUtil().send(player, "admin.usage");
        return true;
    }

    private void openOwnVault(Player player, int page) {
        int sanitizedPage = Math.max(1, page);
        int unlockedPages = plugin.getVaultManager().getAccessiblePages(player);
        if (sanitizedPage > unlockedPages) {
            plugin.getMessagesUtil().send(player, "vault.page-locked", "%value%", String.valueOf(unlockedPages));
            return;
        }
        if (plugin.getVaultManager().isPageLocked(player.getUniqueId(), sanitizedPage)
                && !player.hasPermission("stash.admin.bypass")) {
            plugin.getMessagesUtil().send(player, "vault.open-blocked-locked");
            return;
        }

        int rows = plugin.getVaultManager().getRows(player);
        vaultGUI.open(player, player.getUniqueId(), sanitizedPage, unlockedPages, rows, player.getName());
        plugin.getAuditManager().log(player.getUniqueId(), "OPEN_VAULT", "page=" + sanitizedPage);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("list", "search", "lock", "unlock", "clear", "confirmclear", "admin", "1", "2", "3");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            return List.of("inspect", "setpages", "setrows", "audit");
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("admin")) {
            List<String> names = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
            return names;
        }

        return List.of();
    }

    private record PendingClear(int page, long expiresAt) {
    }
}
