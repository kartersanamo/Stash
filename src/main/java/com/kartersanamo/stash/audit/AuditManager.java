package com.kartersanamo.stash.audit;

import com.kartersanamo.stash.Stash;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuditManager {
    private final Stash plugin;
    private File file;
    private FileConfiguration data;

    public AuditManager(Stash plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.file = new File(plugin.getDataFolder(), "audits.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Failed to save audits.yml: " + exception.getMessage());
        }
    }

    public void log(UUID actor, String action, String details) {
        List<Map<?, ?>> raw = data.getMapList("events");
        LinkedList<Map<?, ?>> events = new LinkedList<>(raw);

        Map<String, Object> event = new HashMap<>();
        event.put("timestamp", Instant.now().toString());
        event.put("actor", actor.toString());
        event.put("action", action);
        event.put("details", details);
        events.add(event);

        int maxEntries = plugin.getConfigUtil().getAuditMaxEntries();
        while (events.size() > maxEntries) {
            events.removeFirst();
        }

        data.set("events", events);
        save();
    }
}
