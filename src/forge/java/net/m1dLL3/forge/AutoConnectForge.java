package net.m1dLL3.forge;

import net.m1dLL3.client.AutoConnectConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AutoConnectForge.MOD_ID)
public final class AutoConnectForge {
    public static final String MOD_ID = "autoconnect";

    public AutoConnectForge(FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(AutoConnectConfigScreen::new));
    }
}
