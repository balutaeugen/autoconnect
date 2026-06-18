package net.m1dLL3.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class AutoConnectModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!AutoConnectClothConfigScreenProvider.isAvailable()) {
            return ModMenuApi.super.getModConfigScreenFactory();
        }

        return new AutoConnectClothConfigScreenProvider()::getConfigScreen;
    }
}
