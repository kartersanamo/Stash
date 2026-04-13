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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VaultListener implements Listener {
    private final Stash plugin;
    private final VaultGUI vaultGUI;
    private final Map<UUID, Long> guiClearArmed = new HashMap<>();

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
        if (event.getRawSlot() < navStart && !context.readOnly()) {
            return;
        }
        if (event.getRawSlot() < navStart) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        if (event.getRawSlot() == navStart + 2 && !context.readOnly()) {
            boolean currentlyLocked = plugin.getVaultManager().isPageLocked(context.owner(), context.page());
            plugin.getVaultManager().setPageLocked(context.owner(), context.page(), !currentlyLocked);
            plugin.getAuditManager().log(player.getUniqueId(), currentlyLocked ? "UNLOCK_PAGE_GUI" : "LOCK_PAGE_GUI",
                    "owner=" + context.owner() + ",page=" + context.page());
            plugin.getMessagesUtil().send(player, currentlyLocked ? "vault.unlocked" : "vault.locked",
                    "%value%", String.valueOf(context.page()));
            return;
        }
        if (event.getRawSlot() == navStart + 6 && !context.readOnly()) {
            long now = System.currentTimeMillis();
            long expires = guiClearArmed.getOrDefault(player.getUniqueId(), 0L);
            if (expires < now) {
                guiClearArmed.put(player.getUniqueId(), now + 10000);
                plugin.getMessagesUtil().send(player, "gui.clear-confirm");
                return;
            }
            ItemStack[] cleared = new ItemStack[context.rows() * 9];
            plugin.getVaultManager().setPageContents(context.owner(), context.page(), cleared);
            plugin.getVaultManager().save();
            plugin.getAuditManager().log(player.getUniqueId(), "CLEAR_PAGE_GUI",
                    "owner=" + context.owner() + ",page=" + context.page());
            plugin.getMessagesUtil().send(player, "vault.cleared", "%value%", String.valueOf(context.page()));
            guiClearArmed.remove(player.getUniqueId());
            plugin.getGuiManager().clearOpenVault(player.getUniqueId());
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> vaultGUI.open(player, context.owner(), context.page(), context.maxPages(), context.rows(),
                            context.ownerName(), context.readOnly()));
            return;
        }
        if (event.getRawSlot() == navStart + 3 && context.page() > 1) {
            if (!context.readOnly()) {
                saveStorageSection(context, event.getInventory().getContents());
            }
            plugin.getGuiManager().clearOpenVault(player.getUniqueId());
            int nextPage = context.page() - 1;
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> vaultGUI.open(player, context.owner(), nextPage, context.maxPages(), context.rows(),
                            context.ownerName(), context.readOnly()));
            return;
        }

        if (event.getRawSlot() == navStart + 5 && context.page() < context.maxPages()) {
            if (!context.readOnly()) {
                saveStorageSection(context, event.getInventory().getContents());
            }
            plugin.getGuiManager().clearOpenVault(player.getUniqueId());
            int nextPage = context.page() + 1;
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> vaultGUI.open(player, context.owner(), nextPage, context.maxPages(), context.rows(),
                            context.ownerName(), context.readOnly()));
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

        if (!context.readOnly()) {
            saveStorageSection(context, event.getInventory().getContents());
        }
        guiClearArmed.remove(player.getUniqueId());
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
