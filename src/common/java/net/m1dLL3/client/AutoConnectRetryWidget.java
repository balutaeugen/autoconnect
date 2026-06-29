package net.m1dLL3.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class AutoConnectRetryWidget extends StringWidget {
    private long displayedSeconds = -1L;
    private final Runnable retryReady;

    public AutoConnectRetryWidget(Component message, Font font) {
        this(message, font, () -> {
        });
    }

    public AutoConnectRetryWidget(Component message, Font font, Runnable retryReady) {
        super(message, font);
        this.retryReady = retryReady;
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
            if (seconds == 0L) {
                retryReady.run();
            }
            return;
        }

        displayedSeconds = seconds;
        setMessage(AutoConnectState.disconnectedRetryMessage());
        if (seconds == 0L) {
            retryReady.run();
        }
    }
}
