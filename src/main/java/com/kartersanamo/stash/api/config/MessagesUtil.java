package com.kartersanamo.stash.api.config;

import com.kartersanamo.stash.Stash;
import com.kartersanamo.stash.api.chat.ColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessagesUtil {
    private final Stash plugin;
    private FileConfiguration messages;
    private String prefix;

    public MessagesUtil(Stash plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        this.messages = YamlConfiguration.loadConfiguration(file);
        this.prefix = ColorUtil.color(messages.getString("format.prefix", "&8[&bStash&8] &7"));
    }

    public String get(String key) {
        String raw = messages.getString(key, "&cMissing message: " + key);
        return ColorUtil.color(raw);
    }

    public String get(String key, String placeholder, String value) {
        return get(key).replace(placeholder, value);
    }

    public void send(CommandSender sender, String key) {
        sender.sendMessage(prefix + get(key));
    }

    public void send(CommandSender sender, String key, String placeholder, String value) {
        sender.sendMessage(prefix + get(key, placeholder, value));
    }

    public void sendRaw(CommandSender sender, String rawLine) {
        sender.sendMessage(prefix + ColorUtil.color(rawLine));
    }

    public void sendRaw(CommandSender sender, String rawLine, boolean withPrefix) {
        String formatted = ColorUtil.color(rawLine);
        sender.sendMessage(withPrefix ? prefix + formatted : formatted);
    }
}
