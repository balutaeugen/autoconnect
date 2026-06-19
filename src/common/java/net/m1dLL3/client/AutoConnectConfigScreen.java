package net.m1dLL3.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.List;

public class AutoConnectConfigScreen extends Screen {
    private static final int MAX_CONTENT_WIDTH = 520;
    private static final int MIN_CONTENT_WIDTH = 260;
    private static final int HORIZONTAL_MARGIN = 24;
    private static final int TOP_MARGIN = 24;
    private static final int TITLE_HEIGHT = 20;
    private static final int FOOTER_HEIGHT = 32;
    private static final int FOOTER_MARGIN = 12;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 6;
    private static final int MAX_RETRY_COUNT = 99;
    private static final int MAX_RETRY_DELAY_SECONDS = 300;

    private final Screen parent;
    private final AutoConnectConfig config = AutoConnectConfig.get();

    private EditBox serverAddress;
    private EditBox retryCount;
    private EditBox retryDelaySeconds;
    private Button enabledButton;
    private Button retryButton;

    public AutoConnectConfigScreen(Screen parent) {
        super(Component.literal("AutoConnect"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int contentWidth = contentWidth();
        int contentX = (width - contentWidth) / 2;
        int footerY = height - FOOTER_HEIGHT - FOOTER_MARGIN;
        int scrollY = TOP_MARGIN + TITLE_HEIGHT + SPACING;
        int scrollHeight = Math.max(BUTTON_HEIGHT, footerY - scrollY - SPACING);

        addRenderableWidget(new WrappedTextWidget(contentX, TOP_MARGIN, contentWidth, TITLE_HEIGHT, title, true));

        LinearLayout settings = LinearLayout.vertical().spacing(SPACING);
        settings.addChild(description("Controls when AutoConnect joins a server and how failed joins are retried."));
        enabledButton = Button.builder(enabledLabel(), button -> {
            config.enabled = !config.enabled;
            button.setMessage(enabledLabel());
        }).width(contentWidth).build();
        settings.addChild(enabledButton);
        settings.addChild(descriptionSpacer("When enabled, opening Multiplayer starts one automatic connection attempt."));

        settings.addChild(label("Server Address"));
        serverAddress = new EditBox(font, contentWidth, BUTTON_HEIGHT, Component.literal("Server Address"));
        serverAddress.setValue(config.connectAddress());
        settings.addChild(serverAddress);
        settings.addChild(description(lastServerDescription()));
        settings.addChild(spacer(4));

        retryButton = Button.builder(retryLabel(), button -> {
            config.retryOnFailure = !config.retryOnFailure;
            button.setMessage(retryLabel());
        }).width(contentWidth).build();
        settings.addChild(retryButton);
        settings.addChild(descriptionSpacer("When enabled, AutoConnect retries automatically from the disconnect screen."));

        settings.addChild(label("Retries Count"));
        retryCount = new EditBox(font, contentWidth, BUTTON_HEIGHT, Component.literal("Retries Count"));
        retryCount.setValue(Integer.toString(config.retryCount));
        settings.addChild(retryCount);
        settings.addChild(descriptionSpacer("Additional attempts after the first failed connection."));

        settings.addChild(label("Automatic Retry Timeout (in seconds)"));
        retryDelaySeconds = new EditBox(font, contentWidth, BUTTON_HEIGHT, Component.literal("Automatic Retry Timeout"));
        retryDelaySeconds.setValue(Integer.toString(config.retryDelaySeconds));
        settings.addChild(retryDelaySeconds);
        settings.addChild(descriptionSpacer("0 retries as soon as the disconnect screen opens."));

        settings.setX(contentX);
        settings.setY(scrollY);
        settings.arrangeElements();
        addRenderableWidget(new SettingsScrollArea(contentX, scrollY, contentWidth, scrollHeight, settings));

        LinearLayout buttons = LinearLayout.horizontal().spacing(SPACING);
        buttons.addChild(Button.builder(Component.literal("Save"), button -> {
            saveConfig();
            onClose();
        }).width((contentWidth - SPACING) / 2).build());
        buttons.addChild(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .width((contentWidth - SPACING) / 2)
                .build());
        buttons.setX(contentX);
        buttons.setY(footerY);
        buttons.arrangeElements();
        buttons.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            setScreenCompat(parent);
        }
    }

    private void setScreenCompat(Screen screen) {
        if (trySetScreen(minecraft, screen)) {
            return;
        }

        trySetScreen(minecraft.gui, screen);
    }

    private static boolean trySetScreen(Object target, Screen screen) {
        try {
            target.getClass().getMethod("setScreen", Screen.class).invoke(target, screen);
            return true;
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    private void saveConfig() {
        config.serverAddress = serverAddress.getValue().trim();
        config.retryCount = parseClampedInt(retryCount.getValue(), 0, MAX_RETRY_COUNT);
        config.retryDelaySeconds = parseClampedInt(retryDelaySeconds.getValue(), 0, MAX_RETRY_DELAY_SECONDS);
        config.save();
    }

    private static int parseClampedInt(String value, int min, int max) {
        try {
            int parsed = Integer.parseInt(value.trim());
            return Math.max(min, Math.min(max, parsed));
        } catch (NumberFormatException exception) {
            return min;
        }
    }

    private int contentWidth() {
        int availableWidth = Math.max(MIN_CONTENT_WIDTH, width - HORIZONTAL_MARGIN * 2);
        return Math.min(MAX_CONTENT_WIDTH, availableWidth);
    }

    private WrappedTextWidget label(String text) {
        return new WrappedTextWidget(0, 0, contentWidth(), textHeight(text, contentWidth()), Component.literal(text), false);
    }

    private WrappedTextWidget description(String text) {
        return new WrappedTextWidget(0, 0, contentWidth(), textHeight(text, contentWidth()), Component.literal(text), false);
    }

    private AbstractWidget descriptionSpacer(String text) {
        return spacer(textHeight(text, contentWidth()));
    }

    private AbstractWidget spacer(int height) {
        AbstractWidget spacer = new AbstractWidget(0, 0, contentWidth(), height, Component.empty()) {
            @Override
            protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput output) {
            }
        };
        spacer.active = false;
        return spacer;
    }

    private int textHeight(String text, int contentWidth) {
        return Math.max(font.lineHeight, font.wordWrapHeight(Component.literal(text), contentWidth));
    }

    private Component enabledLabel() {
        return Component.literal("Enabled: " + (config.enabled ? "On" : "Off"));
    }

    private Component retryLabel() {
        return Component.literal("Retry on Failure: " + (config.retryOnFailure ? "On" : "Off"));
    }

    private String lastServerDescription() {
        if (config.lastServerAddress == null || config.lastServerAddress.isBlank()) {
            return "Last connected server: none remembered yet.";
        }

        return "Last connected server: " + config.lastServerAddress;
    }

    private final class WrappedTextWidget extends AbstractWidget {
        private static final int TITLE_COLOR = 0xFFFFFFFF;
        private static final int DESCRIPTION_COLOR = 0xFFA0A0A0;

        private final boolean centered;

        private WrappedTextWidget(int x, int y, int width, int height, Component message, boolean centered) {
            super(x, y, width, height, message);
            this.centered = centered;
            this.active = false;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if (centered) {
                graphics.centeredText(font, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - font.lineHeight) / 2, TITLE_COLOR);
                return;
            }

            graphics.textWithWordWrap(font, (FormattedText) getMessage(), getX(), getY(), getWidth(), DESCRIPTION_COLOR);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
        }
    }

    private final class SettingsScrollArea extends AbstractWidget {
        private static final int SCROLLBAR_WIDTH = 4;
        private static final int SCROLL_RATE = 18;

        private final LinearLayout content;
        private final List<AbstractWidget> children = new ArrayList<>();
        private GuiEventListener focused;
        private int scrollAmount;

        private SettingsScrollArea(int x, int y, int width, int height, LinearLayout content) {
            super(x, y, width, height, Component.empty());
            this.content = content;
            content.visitWidgets(children::add);
            updateContentPosition();
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            updateContentPosition();
            graphics.enableScissor(getX(), getY(), getRight(), getBottom());
            for (AbstractWidget child : children) {
                child.visible = child.getBottom() >= getY() && child.getY() <= getBottom();
                child.extractRenderState(graphics, mouseX, mouseY, partialTick);
                child.visible = true;
            }
            graphics.disableScissor();
            renderScrollbar(graphics);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (!isMouseOver(mouseX, mouseY) || maxScrollAmount() <= 0) {
                return false;
            }

            setScrollAmount(scrollAmount - (int) Math.round(scrollY * SCROLL_RATE));
            return true;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (!isMouseOver(event.x(), event.y())) {
                return false;
            }

            updateContentPosition();
            for (AbstractWidget child : children) {
                if (isChildVisible(child) && child.mouseClicked(event, doubleClick)) {
                    focused = child;
                    return true;
                }
            }
            return true;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            return focused != null && focused.mouseReleased(event);
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
            return focused != null && focused.mouseDragged(event, dragX, dragY);
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            return focused != null && focused.keyPressed(event);
        }

        @Override
        public boolean charTyped(CharacterEvent event) {
            return focused != null && focused.charTyped(event);
        }

        @Override
        public void setFocused(boolean focused) {
            if (!focused && this.focused != null) {
                this.focused.setFocused(false);
                this.focused = null;
            }
        }

        @Override
        public boolean isFocused() {
            return focused != null && focused.isFocused();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
        }

        private void updateContentPosition() {
            content.setX(getX());
            content.setY(getY() - scrollAmount);
        }

        private boolean isChildVisible(AbstractWidget child) {
            return child.getBottom() >= getY() && child.getY() <= getBottom();
        }

        private void setScrollAmount(int scrollAmount) {
            this.scrollAmount = Math.max(0, Math.min(maxScrollAmount(), scrollAmount));
            updateContentPosition();
        }

        private int maxScrollAmount() {
            return Math.max(0, content.getHeight() - getHeight());
        }

        private void renderScrollbar(GuiGraphicsExtractor graphics) {
            int maxScroll = maxScrollAmount();
            if (maxScroll <= 0) {
                return;
            }

            int scrollbarX = getRight() - SCROLLBAR_WIDTH;
            int trackHeight = getHeight();
            int thumbHeight = Math.max(16, trackHeight * trackHeight / content.getHeight());
            int thumbY = getY() + scrollAmount * (trackHeight - thumbHeight) / maxScroll;
            graphics.fill(scrollbarX, getY(), getRight(), getBottom(), 0x44000000);
            graphics.fill(scrollbarX, thumbY, getRight(), thumbY + thumbHeight, 0xFF888888);
        }
    }
}
