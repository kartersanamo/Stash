package com.kartersanamo.stash.vault;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.api.config.ConfigUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class VaultManager {
    private final Stash plugin;
    private File file;
    private FileConfiguration data;

    public VaultManager(Stash plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.file = new File(plugin.getDataFolder(), "vaults.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Failed to save vaults.yml: " + exception.getMessage());
        }
    }

    public ItemStack[] getPageContents(UUID owner, int page, int rows) {
        ItemStack[] items = new ItemStack[rows * 9];
        String basePath = "players." + owner + ".pages." + page;
        ConfigurationSection slotsSection = data.getConfigurationSection(basePath + ".slots");
        if (slotsSection != null) {
            for (String key : slotsSection.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key);
                    if (slot < 0 || slot >= items.length) {
                        continue;
                    }
                    items[slot] = data.getItemStack(basePath + ".slots." + key);
                } catch (NumberFormatException ignored) {
                    // Ignore malformed keys.
                }
            }
            return items;
        }

        // Backward compatibility: migrate older list-based saves on read.
        String legacyPath = basePath + ".contents";
        List<ItemStack> legacyContents = data.getList(legacyPath, new ArrayList<>())
                .stream()
                .filter(ItemStack.class::isInstance)
                .map(ItemStack.class::cast)
                .toList();
        for (int i = 0; i < items.length && i < legacyContents.size(); i++) {
            items[i] = legacyContents.get(i);
        }
        return items;
    }

    public void setPageContents(UUID owner, int page, ItemStack[] contents) {
        String basePath = "players." + owner + ".pages." + page;
        data.set(basePath + ".contents", null); // Remove legacy compacting format.
        data.set(basePath + ".slots", null);

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType().isAir()) {
                continue;
            }
            data.set(basePath + ".slots." + i, item);
        }
    }

    public void clearPage(UUID owner, int page, int rows) {
        setPageContents(owner, page, new ItemStack[rows * 9]);
        save();
    }

    public int getAccessiblePages(Player player) {
        return getAccessiblePages(player.getUniqueId(), player);
    }

    public int getAccessiblePages(UUID playerId, Player onlinePlayer) {
        Integer override = getPageOverride(playerId);
        int maxPages = plugin.getConfigUtil().getMaxPages();
        if (override != null) {
            return Math.max(1, Math.min(override, maxPages));
        }

        ConfigUtil config = plugin.getConfigUtil();
        int pages = config.getDefaultPages();
        if (onlinePlayer == null) {
            return pages;
        }

        for (int i = 1; i <= config.getMaxPages(); i++) {
            if (onlinePlayer.hasPermission("stash.pages." + i)) {
                pages = Math.max(pages, i);
            }
        }
        return pages;
    }

    public int getRows(UUID playerId, Player onlinePlayer) {
        Integer rowOverride = getRowsOverride(playerId);
        if (rowOverride != null) {
            return ConfigUtil.clampRows(rowOverride);
        }

        int rows = plugin.getConfigUtil().getDefaultRows();
        if (onlinePlayer == null) {
            return ConfigUtil.clampRows(rows);
        }

        int[] allowed = new int[]{1, 2, 3, 4, 5};
        for (int row : allowed) {
            if (onlinePlayer.hasPermission("stash.size." + row)) {
                rows = Math.max(rows, row);
            }
        }
        return ConfigUtil.clampRows(rows);
    }

    public int getRows(Player player) {
        Integer override = getRowsOverride(player.getUniqueId());
        if (override != null) {
            return ConfigUtil.clampRows(override);
        }

        int rows = plugin.getConfigUtil().getDefaultRows();
        int[] allowed = new int[]{1, 2, 3, 4, 5};
        for (int row : allowed) {
            if (player.hasPermission("stash.size." + row)) {
                rows = Math.max(rows, row);
            }
        }
        return ConfigUtil.clampRows(rows);
    }

    public List<Integer> searchPages(UUID owner, int rows, int maxPages, String query) {
        String normalized = query.toLowerCase(Locale.ROOT);
        List<Integer> matches = new ArrayList<>();

        for (int page = 1; page <= maxPages; page++) {
            ItemStack[] contents = getPageContents(owner, page, rows);
            boolean matched = false;
            for (ItemStack item : contents) {
                if (item == null || item.getType().isAir()) {
                    continue;
                }

                String material = item.getType().name().toLowerCase(Locale.ROOT);
                if (material.contains(normalized)) {
                    matched = true;
                    break;
                }

                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName()
                        && meta.getDisplayName().toLowerCase(Locale.ROOT).contains(normalized)) {
                    matched = true;
                    break;
                }
            }

            if (matched) {
                matches.add(page);
            }
        }

        return matches;
    }

    public void setRowsOverride(UUID playerId, int rows) {
        data.set("players." + playerId + ".settings.rows", ConfigUtil.clampRows(rows));
        save();
    }

    public void setPagesOverride(UUID playerId, int pages) {
        int clamped = Math.max(1, Math.min(pages, plugin.getConfigUtil().getMaxPages()));
        data.set("players." + playerId + ".settings.pages", clamped);
        save();
    }

    public boolean isPageLocked(UUID playerId, int page) {
        return data.getBoolean("players." + playerId + ".locks." + page, false);
    }

    public void setPageLocked(UUID playerId, int page, boolean locked) {
        data.set("players." + playerId + ".locks." + page, locked);
        save();
    }

    public PageDisplayMeta getPageDisplayMeta(UUID playerId, int page) {
        String base = "players." + playerId + ".pages." + page + ".display";
        String name = data.getString(base + ".name", "Vault " + page);
        String material = data.getString(base + ".material", "CHEST");
        String description = data.getString(base + ".description", "Click to open this vault.");
        return new PageDisplayMeta(name, material, description);
    }

    public void setPageDisplayMeta(UUID playerId, int page, PageDisplayMeta meta) {
        String base = "players." + playerId + ".pages." + page + ".display";
        data.set(base + ".name", meta.name());
        data.set(base + ".material", meta.material());
        data.set(base + ".description", meta.description());
        save();
    }

    public Integer getRowsOverrideValue(UUID playerId) {
        return getRowsOverride(playerId);
    }

    public Integer getPagesOverrideValue(UUID playerId) {
        return getPageOverride(playerId);
    }

    private Integer getRowsOverride(UUID playerId) {
        if (!data.contains("players." + playerId + ".settings.rows")) {
            return null;
        }
        return data.getInt("players." + playerId + ".settings.rows");
    }

    private Integer getPageOverride(UUID playerId) {
        if (!data.contains("players." + playerId + ".settings.pages")) {
            return null;
        }
        return data.getInt("players." + playerId + ".settings.pages");
    }

    public List<String> listLoadedPlayers() {
        ConfigurationSection section = data.getConfigurationSection("players");
        if (section == null) {
            return List.of();
        }

        return section.getKeys(false).stream()
                .map(uuid -> {
                    OfflinePlayer player = plugin.getServer().getOfflinePlayer(UUID.fromString(uuid));
                    return player.getName() == null ? uuid : player.getName();
                })
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    public record PageDisplayMeta(String name, String material, String description) {
    }
}
