package com.kartersanamo.stash.storage;

import com.kartersanamo.stash.Stash;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class BackupManager {
    private final Stash plugin;

    public BackupManager(Stash plugin) {
        this.plugin = plugin;
    }

    public String createBackup() throws IOException {
        File dataFolder = plugin.getDataFolder();
        File backupDir = new File(dataFolder, "backups");
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            throw new IOException("Could not create backups directory.");
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        File vaults = new File(dataFolder, "vaults.yml");
        File audits = new File(dataFolder, "audits.yml");

        if (vaults.exists()) {
            Files.copy(vaults.toPath(), new File(backupDir, "vaults-" + timestamp + ".yml").toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        if (audits.exists()) {
            Files.copy(audits.toPath(), new File(backupDir, "audits-" + timestamp + ".yml").toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        writeSnapshotIndex(timestamp, vaults.exists(), audits.exists());
        return timestamp;
    }

    public List<String> listBackups() {
        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) {
            return List.of();
        }

        try (Stream<java.nio.file.Path> stream = Files.list(backupDir.toPath())) {
            return stream
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith("vaults-") && name.endsWith(".yml"))
                    .sorted()
                    .toList();
        } catch (IOException exception) {
            return List.of();
        }
    }

    public boolean restoreBackup(String vaultBackupName) throws IOException {
        File backupFile = new File(plugin.getDataFolder(), "backups/" + vaultBackupName);
        if (!backupFile.exists()) {
            return false;
        }

        File vaults = new File(plugin.getDataFolder(), "vaults.yml");
        Files.copy(backupFile.toPath(), vaults.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    public RestorePreview previewRestore(String vaultBackupName) {
        File backupFile = new File(plugin.getDataFolder(), "backups/" + vaultBackupName);
        if (!backupFile.exists()) {
            return new RestorePreview(false, vaultBackupName, 0, 0);
        }

        FileConfiguration current = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "vaults.yml"));
        FileConfiguration incoming = YamlConfiguration.loadConfiguration(backupFile);

        int currentPlayers = getPlayerCount(current);
        int backupPlayers = getPlayerCount(incoming);
        return new RestorePreview(true, vaultBackupName, currentPlayers, backupPlayers);
    }

    public List<String> listBackupIndexSummaries() {
        File indexFile = new File(plugin.getDataFolder(), "backups/index.yml");
        if (!indexFile.exists()) {
            return List.of();
        }
        FileConfiguration index = YamlConfiguration.loadConfiguration(indexFile);
        List<Map<?, ?>> snapshots = index.getMapList("snapshots");
        List<String> lines = new ArrayList<>();
        for (Map<?, ?> snapshot : snapshots.reversed()) {
            String timestamp = String.valueOf(snapshot.containsKey("timestamp") ? snapshot.get("timestamp") : "unknown");
            String vault = String.valueOf(snapshot.containsKey("vaultFile") ? snapshot.get("vaultFile") : "n/a");
            String audit = String.valueOf(snapshot.containsKey("auditFile") ? snapshot.get("auditFile") : "n/a");
            lines.add(timestamp + " | " + vault + " | " + audit);
        }
        return lines;
    }

    private void writeSnapshotIndex(String timestamp, boolean hasVault, boolean hasAudit) throws IOException {
        File backupDir = new File(plugin.getDataFolder(), "backups");
        File indexFile = new File(backupDir, "index.yml");
        FileConfiguration index = YamlConfiguration.loadConfiguration(indexFile);
        List<Map<?, ?>> existing = index.getMapList("snapshots");
        List<Map<String, Object>> snapshots = new ArrayList<>();
        for (Map<?, ?> map : existing) {
            Map<String, Object> converted = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                converted.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            snapshots.add(converted);
        }

        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("timestamp", timestamp);
        snapshot.put("vaultFile", hasVault ? "vaults-" + timestamp + ".yml" : "missing");
        snapshot.put("auditFile", hasAudit ? "audits-" + timestamp + ".yml" : "missing");
        snapshots.add(snapshot);

        index.set("snapshots", snapshots);
        index.save(indexFile);
    }

    private int getPlayerCount(FileConfiguration config) {
        if (config.getConfigurationSection("players") == null) {
            return 0;
        }
        return config.getConfigurationSection("players").getKeys(false).size();
    }

    public record RestorePreview(boolean exists, String backupName, int currentPlayerEntries, int backupPlayerEntries) {
    }
}
