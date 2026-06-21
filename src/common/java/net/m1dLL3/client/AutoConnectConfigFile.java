package net.m1dLL3.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

final class AutoConnectConfigFile {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private AutoConnectConfigFile() {
    }

    static <T extends AutoConnectConfigBase> T load(Path path, Class<T> configClass) {
        T config = null;
        boolean shouldSave = !Files.exists(path);

        if (!shouldSave) {
            try (Reader reader = Files.newBufferedReader(path)) {
                config = GSON.fromJson(reader, configClass);
            } catch (IOException | JsonParseException | IllegalStateException exception) {
                shouldSave = true;
            }
        }

        if (config == null) {
            config = newConfig(configClass);
            shouldSave = true;
        }

        if (config.sanitize()) {
            shouldSave = true;
        }

        if (shouldSave) {
            save(path, config);
        }

        return config;
    }

    static void save(Path path, AutoConnectConfigBase config) {
        config.sanitize();

        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException ignored) {
        }
    }

    private static <T extends AutoConnectConfigBase> T newConfig(Class<T> configClass) {
        try {
            return configClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create AutoConnect config.", exception);
        }
    }
}
