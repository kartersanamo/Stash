package com.kartersanamo.stash.gui;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.api.item.ItemBuilder;
import com.kartersanamo.stash.vault.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

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
        int size = pages <= 9 ? 9 : (pages <= 18 ? 18 : (pages <= 27 ? 27 : (pages <= 36 ? 36 : 45)));
        Inventory inventory = Bukkit.createInventory(null, size, LIST_TITLE);
        for (int page = 1; page <= pages; page++) {
            VaultManager.PageDisplayMeta meta = plugin.getVaultManager().getPageDisplayMeta(player.getUniqueId(), page);
            Material material;
            try {
                material = Material.valueOf(meta.material().toUpperCase());
            } catch (Exception ignored) {
                material = Material.CHEST;
            }
            inventory.setItem(page - 1, new ItemBuilder(material)
                    .name("§b" + meta.name() + " §7(#" + page + ")")
                    .lore(List.of("§7" + meta.description(), "§aLeft click: Open", "§eRight click: Manage"))
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
        inventory.setItem(15, new ItemBuilder(Material.WRITABLE_BOOK).name("§aChange Description")
                .lore(List.of("§7Current: §f" + meta.description(), "§eClick to set via chat")).build());
        inventory.setItem(22, new ItemBuilder(Material.ARROW).name("§eBack to Vault List").build());
        player.openInventory(inventory);
    }
}
