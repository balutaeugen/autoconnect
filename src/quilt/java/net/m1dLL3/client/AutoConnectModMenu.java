package net.m1dLL3.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.util.Map;

public class AutoConnectModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (AutoConnectClothConfigScreenProvider.isAvailable()) {
            return new AutoConnectClothConfigScreenProvider()::getConfigScreen;
        }

        return AutoConnectConfigScreen::new;
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return Map.of("autoconnect", getModConfigScreenFactory());
    }
}
