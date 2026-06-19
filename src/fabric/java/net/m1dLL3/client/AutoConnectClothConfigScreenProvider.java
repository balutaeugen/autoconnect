package net.m1dLL3.client;

import net.minecraft.client.gui.screens.Screen;

final class AutoConnectClothConfigScreenProvider {
    private AutoConnectClothConfigScreenFactory factory;

    static boolean isAvailable() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder", false, classLoader);
            Class.forName("me.shedaniel.clothconfig2.gui.entries.TextListEntry", true, classLoader);
            return true;
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }

    public Screen getConfigScreen(Screen parent) {
        if (factory == null) {
            factory = new AutoConnectClothConfigScreenFactory();
        }

        try {
            return factory.getConfigScreen(parent);
        } catch (LinkageError exception) {
            return null;
        }
    }
}
