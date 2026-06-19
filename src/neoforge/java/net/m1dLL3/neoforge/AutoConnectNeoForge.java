package net.m1dLL3.neoforge;

import net.m1dLL3.client.AutoConnectConfigScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(AutoConnectNeoForge.MOD_ID)
public class AutoConnectNeoForge {
    public static final String MOD_ID = "autoconnect";

    public AutoConnectNeoForge(ModContainer container) {
        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (IConfigScreenFactory) (modContainer, modListScreen) -> new AutoConnectConfigScreen(modListScreen));
    }
}
