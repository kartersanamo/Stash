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
        String path = "players." + owner + ".pages." + page + ".contents";
        List<ItemStack> contents = data.getList(path, new ArrayList<>())
                .stream()
                .filter(ItemStack.class::isInstance)
                .map(ItemStack.class::cast)
                .toList();

        ItemStack[] items = new ItemStack[rows * 9];
        for (int i = 0; i < items.length && i < contents.size(); i++) {
            items[i] = contents.get(i);
        }
        return items;
    }

    public void setPageContents(UUID owner, int page, ItemStack[] contents) {
        String path = "players." + owner + ".pages." + page + ".contents";
        List<ItemStack> list = new ArrayList<>(contents.length);
        for (ItemStack content : contents) {
            list.add(content);
        }
        data.set(path, list);
    }

    public int getAccessiblePages(Player player) {
        return getAccessiblePages(player.getUniqueId(), player);
    }

    public int getAccessiblePages(UUID playerId, Player onlinePlayer) {
        Integer override = getPageOverride(playerId);
        if (override != null) {
            return override;
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

        int[] allowed = new int[]{1, 2, 3, 4, 5, 6};
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
        int[] allowed = new int[]{1, 2, 3, 4, 5, 6};
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
        data.set("players." + playerId + ".settings.pages", Math.max(1, pages));
        save();
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
}
