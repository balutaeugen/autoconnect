package net.m1dLL3.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class AutoConnectRetryWidget extends StringWidget {
    private long displayedSeconds = -1L;

    public AutoConnectRetryWidget(Component message, Font font) {
        super(message, font);
        updateMessage();
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        updateMessage();
        super.extractWidgetRenderState(extractor, mouseX, mouseY, partialTick);
    }

    public void refresh() {
        updateMessage();
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
