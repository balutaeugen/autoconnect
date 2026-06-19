package net.m1dLL3.mixin.client;

import net.m1dLL3.client.AutoConnectState;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void resetAutoConnectAttempts(CallbackInfo ci) {
        AutoConnectState.resetAttempts();
    }
}
