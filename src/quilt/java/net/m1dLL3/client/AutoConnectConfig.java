package net.m1dLL3.client;

import org.quiltmc.loader.api.QuiltLoader;

import java.nio.file.Path;

public class AutoConnectConfig extends AutoConnectConfigBase {
    private static final Path PATH = QuiltLoader.getConfigDir().resolve("autoconnect.json");
    private static AutoConnectConfig instance;

    public static AutoConnectConfig get() {
        if (instance == null) {
            instance = load();
        }

        return instance;
    }

    private static AutoConnectConfig load() {
        return AutoConnectConfigFile.load(PATH, AutoConnectConfig.class);
    }

    public void save() {
        AutoConnectConfigFile.save(PATH, this);
    }
}
