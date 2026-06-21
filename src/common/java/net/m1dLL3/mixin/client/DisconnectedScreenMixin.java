package net.m1dLL3.mixin.client;

import net.m1dLL3.client.AutoConnectRetryWidget;
import net.m1dLL3.client.AutoConnectState;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {
    private static final int SIDE_BY_SIDE_BUTTON_WIDTH = 150;
    private static final int RETRY_STATUS_SPACER_HEIGHT = 6;

    @Shadow
    @Final
    private Screen parent;

    @Shadow
    @Final
    private LinearLayout layout;

    private DisconnectedScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void markConnectionAttemptFailed(CallbackInfo ci) {
        if (parent instanceof JoinMultiplayerScreen) {
            AutoConnectState.markConnectionAttemptFailed();
        }
    }

    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void addRetryStatusBelowTitle(CallbackInfo ci) {
        if (parent instanceof JoinMultiplayerScreen multiplayerScreen
                && AutoConnectState.shouldShowDisconnectedRetryStatus()) {
            AutoConnectState.prepareDisconnectedRetry();
            AutoConnectRetryWidget retryMessage = new AutoConnectRetryWidget(
                    AutoConnectState.disconnectedRetryMessage(),
                    font,
                    multiplayerScreen);
            retryMessage.setMaxWidth(width - 50);
            layout.addChild(retryMessage);
            layout.addChild(SpacerElement.height(RETRY_STATUS_SPACER_HEIGHT));
        }
    }

    @ModifyArg(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;",
                    ordinal = 2
            ),
            index = 0
    )
    private LayoutElement addReconnectButtonBesideBackButton(LayoutElement backButton) {
        if (!(parent instanceof JoinMultiplayerScreen multiplayerScreen) || !AutoConnectState.shouldShowDisconnectedControls()) {
            return backButton;
        }

        LinearLayout buttons = LinearLayout.horizontal().spacing(4);
        if (backButton instanceof Button button) {
            button.setWidth(SIDE_BY_SIDE_BUTTON_WIDTH);
        }

        buttons.addChild(backButton);
        buttons.addChild(Button.builder(Component.literal("Reconnect"), button ->
                AutoConnectState.reconnectManually(multiplayerScreen)
        ).width(SIDE_BY_SIDE_BUTTON_WIDTH).build());
        return buttons;
    }
}
