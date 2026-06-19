package net.m1dLL3.mixin.client;

import net.m1dLL3.client.AutoConnectQuiltModMenuCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class QuiltMinecraftMixin {
    @Inject(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/Minecraft;instance:Lnet/minecraft/client/Minecraft;",
                    opcode = Opcodes.PUTSTATIC,
                    shift = At.Shift.AFTER
            )
    )
    private void initializeModMenuForQuilt(GameConfig gameConfig, CallbackInfo ci) {
        AutoConnectQuiltModMenuCompat.initializeModMenuIfNeeded();
    }
}
