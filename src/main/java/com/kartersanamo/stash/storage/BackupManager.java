package com.kartersanamo.stash.storage;

import com.kartersanamo.stash.Stash;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
}
