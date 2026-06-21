package net.m1dLL3.mixin.client;

import net.m1dLL3.client.AutoConnectConfig;
import net.m1dLL3.client.AutoConnectState;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public class JoinMultiplayerScreenMixin {
    @Unique
    private boolean autoConnect$checkedAutoConnect;

    @Inject(method = "init", at = @At("TAIL"))
    private void autoConnectAfterOpeningMultiplayer(CallbackInfo ci) {
        if (autoConnect$checkedAutoConnect) {
            return;
        }

        autoConnect$checkedAutoConnect = true;
        AutoConnectState.tryConnect((JoinMultiplayerScreen) (Object) this);
    }

    @Inject(method = "join", at = @At("HEAD"))
    private void rememberJoinedServer(ServerData server, CallbackInfo ci) {
        if (server != null) {
            AutoConnectState.beginConnectionAttempt();
            AutoConnectConfig.get().useServerForAutoConnect(server.ip);
        }
    }
}
