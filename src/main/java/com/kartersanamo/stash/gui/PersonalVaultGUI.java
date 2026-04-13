package com.kartersanamo.stash.gui;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.api.item.ItemBuilder;
import com.kartersanamo.stash.vault.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class PersonalVaultGUI {
    public static final String LIST_TITLE = "Your Vaults";
    public static final String MANAGE_TITLE = "Manage Vault";
    private final Stash plugin;

    public PersonalVaultGUI(Stash plugin) {
        this.plugin = plugin;
    }

    public void openList(Player player) {
        int pages = plugin.getVaultManager().getAccessiblePages(player);
        int displayPages = Math.min(pages, 54);
        int size = displayPages <= 9 ? 9 : (displayPages <= 18 ? 18 : (displayPages <= 27 ? 27 : (displayPages <= 36 ? 36 : (displayPages <= 45 ? 45 : 54))));
        Inventory inventory = Bukkit.createInventory(null, size, LIST_TITLE);
        for (int page = 1; page <= displayPages; page++) {
            VaultManager.PageDisplayMeta meta = plugin.getVaultManager().getPageDisplayMeta(player.getUniqueId(), page);
            Material material;
            try {
                material = Material.valueOf(meta.material().toUpperCase());
            } catch (Exception ignored) {
                material = Material.CHEST;
            }
            List<String> lore = new ArrayList<>();
            for (String line : wrapDescription(meta.description(), 30)) {
                lore.add("§7" + line);
            }
            lore.add("§aLeft click: Open");
            lore.add("§eRight click: Manage");
            inventory.setItem(page - 1, new ItemBuilder(material)
                    .name("§b" + meta.name() + " §7(#" + page + ")")
                    .lore(lore)
                    .build());
        }
        player.openInventory(inventory);
    }

    public void openManage(Player player, int page) {
        VaultManager.PageDisplayMeta meta = plugin.getVaultManager().getPageDisplayMeta(player.getUniqueId(), page);
        Inventory inventory = Bukkit.createInventory(null, 27, MANAGE_TITLE + " #" + page);
        inventory.setItem(11, new ItemBuilder(Material.NAME_TAG).name("§bRename")
                .lore(List.of("§7Current: §f" + meta.name(), "§eClick to set via chat")).build());
        inventory.setItem(13, new ItemBuilder(Material.ITEM_FRAME).name("§dChange Icon")
                .lore(List.of("§7Current: §f" + meta.material(), "§eClick to set material in chat")).build());
        List<String> descriptionLore = new ArrayList<>();
        descriptionLore.add("§7Current:");
        for (String line : wrapDescription(meta.description(), 30)) {
            descriptionLore.add("§f" + line);
        }
        descriptionLore.add("§eClick to set via chat");
        inventory.setItem(15, new ItemBuilder(Material.WRITABLE_BOOK).name("§aChange Description")
                .lore(descriptionLore).build());
        inventory.setItem(22, new ItemBuilder(Material.ARROW).name("§eBack to Vault List").build());
        player.openInventory(inventory);
    }

    private List<String> wrapDescription(String input, int maxCharacters) {
        List<String> lines = new ArrayList<>();
        String[] words = input.split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (current.isEmpty()) {
                current.append(word);
                continue;
            }
            if (current.length() + 1 + word.length() <= maxCharacters) {
                current.append(' ').append(word);
            } else {
                lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        if (lines.isEmpty()) {
            lines.add("");
        }
        return lines;
    }
}
