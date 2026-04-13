package com.kartersanamo.stash.gui;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.api.gui.OpenVaultContext;
import com.kartersanamo.stash.api.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class VaultGUI {
    private final Stash plugin;

    public VaultGUI(Stash plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer, UUID owner, int page, int maxPages, int rows, String ownerName) {
        open(viewer, owner, page, maxPages, rows, ownerName, false);
    }

    public void open(Player viewer, UUID owner, int page, int maxPages, int rows, String ownerName, boolean readOnly) {
        String title = plugin.getMessagesUtil().get("gui.vault-title")
                .replace("%owner%", ownerName)
                .replace("%page%", String.valueOf(page));

        boolean hasControlRow = rows < 6;
        int inventorySize = hasControlRow ? (rows + 1) * 9 : rows * 9;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, title);
        ItemStack[] stored = plugin.getVaultManager().getPageContents(owner, page, rows);
        for (int i = 0; i < stored.length; i++) {
            inventory.setItem(i, stored[i]);
        }

        if (hasControlRow) {
            int navStart = rows * 9;
            inventory.setItem(navStart + 3, new ItemBuilder(Material.ARROW)
                    .name(plugin.getMessagesUtil().get("gui.prev-name"))
                    .lore(List.of(plugin.getMessagesUtil().get("gui.prev-lore")))
                    .build());
            inventory.setItem(navStart + 4, new ItemBuilder(Material.PAPER)
                    .name(plugin.getMessagesUtil().get("gui.page-info-name")
                            .replace("%page%", String.valueOf(page))
                            .replace("%max%", String.valueOf(maxPages)))
                    .build());
            inventory.setItem(navStart + 5, new ItemBuilder(Material.ARROW)
                    .name(plugin.getMessagesUtil().get("gui.next-name"))
                    .lore(List.of(plugin.getMessagesUtil().get("gui.next-lore")))
                    .build());
            if (!readOnly) {
                inventory.setItem(navStart + 2, new ItemBuilder(Material.IRON_DOOR)
                        .name(plugin.getMessagesUtil().get("gui.toggle-lock-name"))
                        .lore(List.of(plugin.getMessagesUtil().get("gui.toggle-lock-lore")))
                        .build());
                inventory.setItem(navStart + 6, new ItemBuilder(Material.BARRIER)
                        .name(plugin.getMessagesUtil().get("gui.clear-name"))
                        .lore(List.of(plugin.getMessagesUtil().get("gui.clear-lore")))
                        .build());
            }
            if (readOnly) {
                inventory.setItem(navStart + 7, new ItemBuilder(Material.BOOK)
                        .name(plugin.getMessagesUtil().get("gui.read-only-name"))
                        .lore(List.of(plugin.getMessagesUtil().get("gui.read-only-lore")))
                        .build());
            }
        }

        plugin.getGuiManager().setOpenVault(viewer.getUniqueId(),
                new OpenVaultContext(owner, ownerName, page, maxPages, rows, title, readOnly));
        viewer.openInventory(inventory);

        if (plugin.getConfigUtil().playOpenSound()) {
            try {
                Sound sound = Sound.valueOf(plugin.getConfigUtil().getOpenSound());
                viewer.playSound(viewer.getLocation(), sound, plugin.getConfigUtil().getOpenSoundVolume(),
                        plugin.getConfigUtil().getOpenSoundPitch());
            } catch (IllegalArgumentException ignored) {
                // Invalid sound in config should not break vault usage.
            }
        }
    }
}
