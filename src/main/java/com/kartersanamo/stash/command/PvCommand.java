package com.kartersanamo.stash.command;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.gui.VaultGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PvCommand implements CommandExecutor, TabCompleter {
    private final Stash plugin;
    private final VaultGUI vaultGUI;

    public PvCommand(Stash plugin) {
        this.plugin = plugin;
        this.vaultGUI = new VaultGUI(plugin);
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
            openOwnVault(player, 1);
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            int pages = plugin.getVaultManager().getAccessiblePages(player);
            plugin.getMessagesUtil().send(player, "vault.list-pages", "%value%", String.valueOf(pages));
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

            int rows = target.getPlayer() == null ? plugin.getConfigUtil().getDefaultRows() :
                    plugin.getVaultManager().getRows(target.getPlayer());
            vaultGUI.open(player, target.getUniqueId(), Math.max(1, page), rows, args[2]);
            plugin.getMessagesUtil().send(player, "admin.inspect-opened", "%player%", args[2]);
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
            plugin.getVaultManager().setPagesOverride(target.getUniqueId(), pages);
            plugin.getMessagesUtil().send(player, "admin.updated-pages", "%value%", String.valueOf(pages));
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

        int rows = plugin.getVaultManager().getRows(player);
        vaultGUI.open(player, player.getUniqueId(), sanitizedPage, rows, player.getName());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("list", "admin", "1", "2", "3");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            return List.of("inspect", "setpages", "setrows");
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("admin")) {
            List<String> names = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
            return names;
        }

        return List.of();
    }
}
