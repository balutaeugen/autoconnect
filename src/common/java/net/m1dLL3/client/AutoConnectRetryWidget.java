package net.m1dLL3.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;

public class AutoConnectRetryWidget extends StringWidget {
    private final JoinMultiplayerScreen multiplayerScreen;
    private long displayedSeconds = -1L;

    public AutoConnectRetryWidget(Component message, Font font, JoinMultiplayerScreen multiplayerScreen) {
        super(message, font);
        this.multiplayerScreen = multiplayerScreen;
        updateMessage();
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        AutoConnectState.tickDisconnectedRetry(multiplayerScreen);
        updateMessage();
        super.extractWidgetRenderState(extractor, mouseX, mouseY, partialTick);
    }

    private void updateMessage() {
        long seconds = AutoConnectState.disconnectedRetrySeconds();
        if (seconds == displayedSeconds) {
            return;
        }

        displayedSeconds = seconds;
        setMessage(AutoConnectState.disconnectedRetryMessage());
    }
}
