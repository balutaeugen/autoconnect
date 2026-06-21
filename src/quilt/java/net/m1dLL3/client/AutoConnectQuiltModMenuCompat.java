package net.m1dLL3.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;

public final class AutoConnectQuiltModMenuCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger("AutoConnect");
    private static boolean initialized;

    private AutoConnectQuiltModMenuCompat() {
    }

    public static void initializeModMenuIfNeeded() {
        if (initialized) {
            return;
        }

        try {
            Class<?> modMenuClass = Class.forName("com.terraformersmc.modmenu.ModMenu");
            Map<?, ?> mods = readMap(modMenuClass, "MODS");
            if (!mods.isEmpty()) {
                initialized = true;
                return;
            }

            Object modMenu = modMenuClass.getDeclaredConstructor().newInstance();
            modMenuClass.getMethod("onInitializeClient").invoke(modMenu);
            initialized = true;
            LOGGER.info("Initialized Mod Menu for Quilt compatibility.");
        } catch (ClassNotFoundException ignored) {
            initialized = true;
        } catch (ReflectiveOperationException exception) {
            LOGGER.warn("Failed to initialize Mod Menu for Quilt compatibility.", exception);
            initialized = true;
        }
    }

    private static Map<?, ?> readMap(Class<?> owner, String fieldName) throws ReflectiveOperationException {
        Field field = owner.getField(fieldName);
        return (Map<?, ?>) field.get(null);
    }
}
