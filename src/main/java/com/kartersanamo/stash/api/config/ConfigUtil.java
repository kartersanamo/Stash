package com.kartersanamo.stash.api.config;

import com.kartersanamo.stash.Stash;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigUtil {
    private final Stash plugin;
    private FileConfiguration config;

    public ConfigUtil(Stash plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void reload() {
        this.config = plugin.getConfig();
    }

    public int getDefaultPages() {
        return Math.max(1, config.getInt("vault.default-pages", 1));
    }

    public int getMaxPages() {
        return Math.max(1, config.getInt("vault.max-pages", 6));
    }

    public int getDefaultRows() {
        int rows = config.getInt("vault.default-rows", 5);
        return clampRows(rows);
    }

    public boolean playOpenSound() {
        return config.getBoolean("effects.open-sound.enabled", true);
    }

    public String getOpenSound() {
        return config.getString("effects.open-sound.sound", "BLOCK_ENDER_CHEST_OPEN");
    }

    public float getOpenSoundVolume() {
        return (float) config.getDouble("effects.open-sound.volume", 1.0D);
    }

    public float getOpenSoundPitch() {
        return (float) config.getDouble("effects.open-sound.pitch", 1.0D);
    }

    public int getAuditMaxEntries() {
        return Math.max(100, config.getInt("audit.max-entries", 5000));
    }

    public static int clampRows(int rows) {
        if (rows < 1) {
            return 1;
        }
        if (rows > 5) {
            return 5;
        }
        return rows;
    }
}
