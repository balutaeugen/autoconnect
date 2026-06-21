package net.m1dLL3.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

public final class AutoConnectState {
    private static int attempts;
    private static long retryAtMillis = -1L;

    private AutoConnectState() {
    }

    public static void resetAttempts() {
        attempts = 0;
        cancelAutomaticRetry();
    }

    public static boolean tryConnect(JoinMultiplayerScreen screen) {
        cancelAutomaticRetry();

        AutoConnectConfig config = AutoConnectConfig.get();
        if (!canRetry(config)) {
            return false;
        }

        connect(screen, config);
        return true;
    }

    public static boolean reconnectManually(JoinMultiplayerScreen screen) {
        cancelAutomaticRetry();

        AutoConnectConfig config = AutoConnectConfig.get();
        if (!canUseAutoConnect(config)) {
            return false;
        }

        connect(screen, config);
        return true;
    }

    private static void connect(JoinMultiplayerScreen screen, AutoConnectConfig config) {
        String configuredAddress = config.connectAddress();
        Minecraft minecraft = Minecraft.getInstance();
        ServerData target = new ServerData("AutoConnect", configuredAddress, ServerData.Type.OTHER);
        ServerAddress address = ServerAddress.parseString(target.ip);
        attempts++;
        config.rememberServer(target.ip);
        ConnectScreen.startConnecting(screen, minecraft, address, target, false, null);
    }

    public static void prepareDisconnectedRetry() {
        AutoConnectConfig config = AutoConnectConfig.get();
        if (!canRetry(config)) {
            cancelAutomaticRetry();
            return;
        }

        retryAtMillis = System.currentTimeMillis() + (config.retryDelaySeconds * 1000L);
    }

    public static void cancelAutomaticRetry() {
        retryAtMillis = -1L;
    }

    public static void tickDisconnectedRetry(JoinMultiplayerScreen screen) {
        if (retryAtMillis < 0L) {
            return;
        }

        if (millisUntilRetry() <= 0L) {
            tryConnect(screen);
        }
    }

    public static Component disconnectedRetryMessage() {
        long seconds = Math.max(0L, (millisUntilRetry() + 999L) / 1000L);
        if (seconds == 0L) {
            return Component.literal("AutoConnect is reconnecting...");
        }

        return Component.literal("AutoConnect will reconnect in " + seconds + "s.");
    }

    public static boolean shouldShowDisconnectedControls() {
        return canUseAutoConnect(AutoConnectConfig.get());
    }

    public static boolean shouldShowDisconnectedRetryStatus() {
        AutoConnectConfig config = AutoConnectConfig.get();
        return config.retryOnFailure && canRetry(config);
    }

    private static boolean canRetry(AutoConnectConfig config) {
        if (!canUseAutoConnect(config)) {
            return false;
        }

        if (!config.retryOnFailure && attempts > 0) {
            return false;
        }

        return !config.retryOnFailure || attempts < 1 + config.retryCount;
    }

    private static boolean canUseAutoConnect(AutoConnectConfig config) {
        return config.enabled && !config.connectAddress().isBlank();
    }

    private static long millisUntilRetry() {
        return retryAtMillis - System.currentTimeMillis();
    }
}
