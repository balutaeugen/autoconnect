package net.m1dLL3.forge;

import net.m1dLL3.client.AutoConnectConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(AutoConnectForge.MOD_ID)
public final class AutoConnectForge {
    public static final String MOD_ID = "autoconnect";

    public AutoConnectForge() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(AutoConnectConfigScreen::new));
    }
}
