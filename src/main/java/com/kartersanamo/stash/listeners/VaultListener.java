package com.kartersanamo.stash.listeners;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.api.gui.OpenVaultContext;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class VaultListener implements Listener {
    private final Stash plugin;

    public VaultListener(Stash plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        OpenVaultContext context = plugin.getGuiManager().getOpenVault(player.getUniqueId());
        if (context == null) {
            return;
        }

        if (!event.getView().getTitle().equals(context.title())) {
            return;
        }

        plugin.getVaultManager().setPageContents(context.owner(), context.page(), event.getInventory().getContents());
        plugin.getVaultManager().save();
        plugin.getGuiManager().clearOpenVault(player.getUniqueId());
    }
}
