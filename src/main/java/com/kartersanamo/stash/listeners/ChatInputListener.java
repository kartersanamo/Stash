package com.kartersanamo.stash.listeners;

import com.kartersanamo.stash.Stash;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatInputListener implements Listener {
    private final Stash plugin;

    public ChatInputListener(Stash plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        var pending = plugin.getChatInputManager().get(event.getPlayer().getUniqueId());
        if (pending == null) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            pending.callback().accept(message);
            plugin.getChatInputManager().clear(event.getPlayer().getUniqueId());
        });
    }
}
