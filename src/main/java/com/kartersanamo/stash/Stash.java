package com.kartersanamo.stash;

import com.kartersanamo.stash.api.command.CommandManager;
import com.kartersanamo.stash.api.config.ConfigUtil;
import com.kartersanamo.stash.api.config.MessagesUtil;
import com.kartersanamo.stash.api.gui.GUIManager;
import com.kartersanamo.stash.command.PvCommand;
import com.kartersanamo.stash.command.StashCommand;
import com.kartersanamo.stash.listeners.VaultListener;
import com.kartersanamo.stash.vault.VaultManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Stash extends JavaPlugin {
    private static Stash instance;
    private ConfigUtil configUtil;
    private MessagesUtil messagesUtil;
    private GUIManager guiManager;
    private VaultManager vaultManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResourceIfMissing("messages.yml");
        saveResourceIfMissing("vaults.yml");

        this.configUtil = new ConfigUtil(this);
        this.messagesUtil = new MessagesUtil(this);
        this.guiManager = new GUIManager(this);
        this.vaultManager = new VaultManager(this);

        this.vaultManager.load();
        this.messagesUtil.reload();

        CommandManager commandManager = new CommandManager(this);
        commandManager.register("pv", new PvCommand(this));
        commandManager.register("stash", new StashCommand(this));

        getServer().getPluginManager().registerEvents(new VaultListener(this), this);
    }

    @Override
    public void onDisable() {
        if (vaultManager != null) {
            vaultManager.save();
        }
    }

    public static Stash getInstance() {
        return instance;
    }

    public ConfigUtil getConfigUtil() {
        return configUtil;
    }

    public MessagesUtil getMessagesUtil() {
        return messagesUtil;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public void reloadPlugin() {
        reloadConfig();
        configUtil.reload();
        messagesUtil.reload();
        vaultManager.load();
    }

    private void saveResourceIfMissing(String fileName) {
        if (!new java.io.File(getDataFolder(), fileName).exists()) {
            saveResource(fileName, false);
        }
    }
}
