package net.m1dLL3.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

final class AutoConnectConfigFile {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = Logger.getLogger("AutoConnect");

    private AutoConnectConfigFile() {
    }

    static <T extends AutoConnectConfigBase> T load(Path path, Class<T> configClass) {
        T config = null;
        boolean shouldSave = !Files.exists(path);

        boolean loadedBrokenConfig = false;
        if (!shouldSave) {
            try (Reader reader = Files.newBufferedReader(path)) {
                config = GSON.fromJson(reader, configClass);
            } catch (IOException exception) {
                LOGGER.log(Level.WARNING, "Failed to read AutoConnect config at " + path + ".", exception);
                loadedBrokenConfig = true;
                shouldSave = true;
            } catch (JsonParseException | IllegalStateException exception) {
                LOGGER.log(Level.WARNING, "Failed to parse AutoConnect config at " + path + ".", exception);
                loadedBrokenConfig = true;
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
            if (loadedBrokenConfig) {
                backupBrokenConfig(path);
            }
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
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to write AutoConnect config at " + path + ".", exception);
        }
    }

    private static void backupBrokenConfig(Path path) {
        if (!Files.exists(path)) {
            return;
        }

        Path backup = path.resolveSibling(path.getFileName() + ".bak");
        try {
            Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            LOGGER.warning("Backed up broken AutoConnect config to " + backup + ".");
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to back up broken AutoConnect config at " + path + ".", exception);
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
