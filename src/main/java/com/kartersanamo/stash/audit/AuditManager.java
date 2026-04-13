package com.kartersanamo.stash.audit;

import com.kartersanamo.stash.Stash;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
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

    public List<String> getRecentFormatted(int limit) {
        AuditPage page = getFilteredPage(1, limit, null, null);
        return page.lines();
    }

    public AuditPage getFilteredPage(int page, int pageSize, String actorFilter, String actionFilter) {
        String normalizedActor = actorFilter == null ? null : actorFilter.toLowerCase(Locale.ROOT);
        String normalizedAction = actionFilter == null ? null : actionFilter.toLowerCase(Locale.ROOT);

        List<AuditEvent> events = new ArrayList<>();
        for (Map<?, ?> event : data.getMapList("events")) {
            String timestamp = String.valueOf(event.containsKey("timestamp") ? event.get("timestamp") : "unknown-time");
            String actor = String.valueOf(event.containsKey("actor") ? event.get("actor") : "unknown-actor");
            String action = String.valueOf(event.containsKey("action") ? event.get("action") : "UNKNOWN");
            String details = String.valueOf(event.containsKey("details") ? event.get("details") : "");

            if (normalizedActor != null && !actor.toLowerCase(Locale.ROOT).contains(normalizedActor)) {
                continue;
            }
            if (normalizedAction != null && !action.toLowerCase(Locale.ROOT).contains(normalizedAction)) {
                continue;
            }
            events.add(new AuditEvent(timestamp, actor, action, details));
        }

        events = events.reversed(); // newest first
        int safePageSize = Math.max(1, pageSize);
        int totalPages = Math.max(1, (int) Math.ceil((double) events.size() / safePageSize));
        int safePage = Math.min(Math.max(1, page), totalPages);
        int start = (safePage - 1) * safePageSize;
        int end = Math.min(start + safePageSize, events.size());

        List<String> lines = new ArrayList<>();
        for (int i = start; i < end; i++) {
            AuditEvent event = events.get(i);
            lines.add("&7[" + event.timestamp + "] &e" + event.action + " &f" + event.actor + " &8- &7" + event.details);
        }
        return new AuditPage(lines, safePage, totalPages, events.size());
    }

    public record AuditPage(List<String> lines, int page, int totalPages, int totalMatches) {
    }

    private record AuditEvent(String timestamp, String actor, String action, String details) {
    }
}
