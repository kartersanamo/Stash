package com.kartersanamo.stash.api.gui;

import com.kartersanamo.stash.Stash;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager {
    private final Stash plugin;
    private final Map<UUID, OpenVaultContext> openVaults = new HashMap<>();

    public GUIManager(Stash plugin) {
        this.plugin = plugin;
    }

    public Stash getPlugin() {
        return plugin;
    }

    public void setOpenVault(UUID viewer, OpenVaultContext context) {
        openVaults.put(viewer, context);
    }

    public OpenVaultContext getOpenVault(UUID viewer) {
        return openVaults.get(viewer);
    }

    public void clearOpenVault(UUID viewer) {
        openVaults.remove(viewer);
    }
}
