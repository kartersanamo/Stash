package com.kartersanamo.stash.gui;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.api.gui.OpenVaultContext;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class VaultGUI {
    private final Stash plugin;

    public VaultGUI(Stash plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer, UUID owner, int page, int rows, String ownerName) {
        String title = plugin.getMessagesUtil().get("gui.vault-title")
                .replace("%owner%", ownerName)
                .replace("%page%", String.valueOf(page));

        Inventory inventory = Bukkit.createInventory(null, rows * 9, title);
        inventory.setContents(plugin.getVaultManager().getPageContents(owner, page, rows));

        plugin.getGuiManager().setOpenVault(viewer.getUniqueId(), new OpenVaultContext(owner, page, rows, title));
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
