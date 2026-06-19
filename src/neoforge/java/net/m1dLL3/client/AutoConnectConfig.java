package net.m1dLL3.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoConnectConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("autoconnect.json");
    private static AutoConnectConfig instance;

    public boolean enabled = true;
    public String serverAddress = "";
    public String lastServerAddress = "";
    public boolean retryOnFailure = false;
    public int retryCount = 0;
    public int retryDelaySeconds = 0;

    public static AutoConnectConfig get() {
        if (instance == null) {
            instance = load();
        }

        return instance;
    }

    private static AutoConnectConfig load() {
        if (!Files.exists(PATH)) {
            AutoConnectConfig config = new AutoConnectConfig();
            config.save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(PATH)) {
            AutoConnectConfig config = GSON.fromJson(reader, AutoConnectConfig.class);
            return config == null ? new AutoConnectConfig() : config.sanitize();
        } catch (IOException exception) {
            return new AutoConnectConfig();
        }
    }

    public void save() {
        sanitize();

        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {
        }
    }

    public void rememberServer(String address) {
        if (address == null || address.isBlank()) {
            return;
        }

        lastServerAddress = address.trim();
        save();
    }

    public void useServerForAutoConnect(String address) {
        if (address == null || address.isBlank()) {
            return;
        }

        String trimmedAddress = address.trim();
        lastServerAddress = trimmedAddress;
        serverAddress = trimmedAddress;
        save();
    }

    public String connectAddress() {
        return serverAddress == null ? "" : serverAddress.trim();
    }

    private AutoConnectConfig sanitize() {
        if (serverAddress == null) {
            serverAddress = "";
        }
        if (lastServerAddress == null) {
            lastServerAddress = "";
        }
        retryCount = Math.max(0, retryCount);
        retryDelaySeconds = Math.max(0, retryDelaySeconds);
        return this;
    }
}
