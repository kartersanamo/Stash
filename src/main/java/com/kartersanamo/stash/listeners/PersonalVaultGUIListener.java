package com.kartersanamo.stash.listeners;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.gui.PersonalVaultGUI;
import com.kartersanamo.stash.gui.VaultGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PersonalVaultGUIListener implements Listener {
    private final Stash plugin;
    private final PersonalVaultGUI personalVaultGUI;
    private final VaultGUI vaultGUI;
    private final Map<UUID, Integer> managingPage = new HashMap<>();

    public PersonalVaultGUIListener(Stash plugin) {
        this.plugin = plugin;
        this.personalVaultGUI = new PersonalVaultGUI(plugin);
        this.vaultGUI = new VaultGUI(plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        String title = event.getView().getTitle();
        if (!title.startsWith(PersonalVaultGUI.LIST_TITLE) && !title.startsWith(PersonalVaultGUI.MANAGE_TITLE)) {
            return;
        }
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getRawSlot() < 0) {
            return;
        }

        if (title.equals(PersonalVaultGUI.LIST_TITLE)) {
            int page = event.getRawSlot() + 1;
            int unlocked = plugin.getVaultManager().getAccessiblePages(player);
            if (page > unlocked) {
                return;
            }
            if (event.getClick() == ClickType.RIGHT) {
                managingPage.put(player.getUniqueId(), page);
                personalVaultGUI.openManage(player, page);
                return;
            }
            int rows = plugin.getVaultManager().getRows(player);
            vaultGUI.open(player, player.getUniqueId(), page, unlocked, rows, player.getName());
            return;
        }

        Integer page = managingPage.get(player.getUniqueId());
        if (page == null) {
            personalVaultGUI.openList(player);
            return;
        }

        Material type = event.getCurrentItem().getType();
        if (type == Material.ARROW) {
            personalVaultGUI.openList(player);
            return;
        }
        if (type == Material.NAME_TAG) {
            player.closeInventory();
            plugin.getMessagesUtil().send(player, "vault.manage-enter-name");
            plugin.getChatInputManager().await(player.getUniqueId(), "name", input -> {
                var old = plugin.getVaultManager().getPageDisplayMeta(player.getUniqueId(), page);
                plugin.getVaultManager().setPageDisplayMeta(player.getUniqueId(), page,
                        new com.kartersanamo.stash.vault.VaultManager.PageDisplayMeta(input, old.material(), old.description()));
                plugin.getMessagesUtil().send(player, "vault.manage-name-updated");
                personalVaultGUI.openManage(player, page);
            });
            return;
        }
        if (type == Material.ITEM_FRAME) {
            player.closeInventory();
            plugin.getMessagesUtil().send(player, "vault.manage-enter-material");
            plugin.getChatInputManager().await(player.getUniqueId(), "material", input -> {
                try {
                    Material.valueOf(input.toUpperCase());
                } catch (Exception exception) {
                    plugin.getMessagesUtil().send(player, "vault.manage-invalid-material");
                    personalVaultGUI.openManage(player, page);
                    return;
                }
                var old = plugin.getVaultManager().getPageDisplayMeta(player.getUniqueId(), page);
                plugin.getVaultManager().setPageDisplayMeta(player.getUniqueId(), page,
                        new com.kartersanamo.stash.vault.VaultManager.PageDisplayMeta(old.name(), input.toUpperCase(), old.description()));
                plugin.getMessagesUtil().send(player, "vault.manage-material-updated");
                personalVaultGUI.openManage(player, page);
            });
            return;
        }
        if (type == Material.WRITABLE_BOOK) {
            player.closeInventory();
            plugin.getMessagesUtil().send(player, "vault.manage-enter-description");
            plugin.getChatInputManager().await(player.getUniqueId(), "description", input -> {
                var old = plugin.getVaultManager().getPageDisplayMeta(player.getUniqueId(), page);
                plugin.getVaultManager().setPageDisplayMeta(player.getUniqueId(), page,
                        new com.kartersanamo.stash.vault.VaultManager.PageDisplayMeta(old.name(), old.material(), input));
                plugin.getMessagesUtil().send(player, "vault.manage-description-updated");
                personalVaultGUI.openManage(player, page);
            });
        }
    }
}
