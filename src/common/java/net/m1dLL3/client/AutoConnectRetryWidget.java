package net.m1dLL3.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;

public class AutoConnectRetryWidget extends StringWidget {
    private final JoinMultiplayerScreen multiplayerScreen;

    public AutoConnectRetryWidget(Component message, Font font, JoinMultiplayerScreen multiplayerScreen) {
        super(message, font);
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        AutoConnectState.tickDisconnectedRetry(multiplayerScreen);
        setMessage(AutoConnectState.disconnectedRetryMessage());
        super.extractWidgetRenderState(extractor, mouseX, mouseY, partialTick);
    }
}
