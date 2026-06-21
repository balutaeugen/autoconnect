package net.m1dLL3.mixin.client;

import net.m1dLL3.client.AutoConnectState;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void markSuccessfulConnection(ClientboundLoginPacket packet, CallbackInfo ci) {
        AutoConnectState.markConnectedSuccessfullyIfAttempting();
    }
}
