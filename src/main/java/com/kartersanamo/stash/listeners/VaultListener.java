package com.kartersanamo.stash.listeners;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.api.gui.OpenVaultContext;
import com.kartersanamo.stash.gui.VaultGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class VaultListener implements Listener {
    private final Stash plugin;
    private final VaultGUI vaultGUI;

    public VaultListener(Stash plugin) {
        this.plugin = plugin;
        this.vaultGUI = new VaultGUI(plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        OpenVaultContext context = plugin.getGuiManager().getOpenVault(player.getUniqueId());
        if (context == null || !event.getView().getTitle().equals(context.title())) {
            return;
        }

        int navStart = context.rows() * 9;
        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }
        if (event.getRawSlot() < navStart) {
            return;
        }

        event.setCancelled(true);
        if (event.getRawSlot() == navStart + 3 && context.page() > 1) {
            saveStorageSection(context, event.getInventory().getContents());
            plugin.getGuiManager().clearOpenVault(player.getUniqueId());
            int nextPage = context.page() - 1;
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> vaultGUI.open(player, context.owner(), nextPage, context.maxPages(), context.rows(),
                            context.ownerName()));
            return;
        }

        if (event.getRawSlot() == navStart + 5 && context.page() < context.maxPages()) {
            saveStorageSection(context, event.getInventory().getContents());
            plugin.getGuiManager().clearOpenVault(player.getUniqueId());
            int nextPage = context.page() + 1;
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> vaultGUI.open(player, context.owner(), nextPage, context.maxPages(), context.rows(),
                            context.ownerName()));
        }
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

        saveStorageSection(context, event.getInventory().getContents());
        plugin.getGuiManager().clearOpenVault(player.getUniqueId());
    }

    private void saveStorageSection(OpenVaultContext context, ItemStack[] inventoryContents) {
        ItemStack[] storage = new ItemStack[context.rows() * 9];
        System.arraycopy(inventoryContents, 0, storage, 0, storage.length);
        plugin.getVaultManager().setPageContents(context.owner(), context.page(), storage);
        plugin.getVaultManager().save();
        plugin.getAuditManager().log(context.owner(), "SAVE_VAULT", "page=" + context.page());
    }
}
