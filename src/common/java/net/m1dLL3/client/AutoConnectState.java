package net.m1dLL3.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AutoConnectState {
    private static final Logger LOGGER = Logger.getLogger("AutoConnect");
    private static final AutoConnectRetryCounters RETRY_COUNTERS = new AutoConnectRetryCounters();

    private static long retryAtMillis = -1L;
    private static boolean connectionAttemptInProgress;
    private static boolean connectedSuccessfully;
    private static AutoConnectRetryCounters.AttemptCounter activeAttemptCounter = RETRY_COUNTERS.autoConnect();

    private AutoConnectState() {
    }

    public static void resetAttempts() {
        RETRY_COUNTERS.reset();
        activeAttemptCounter = RETRY_COUNTERS.autoConnect();
        connectionAttemptInProgress = false;
        connectedSuccessfully = false;
        cancelAutomaticRetry();
    }

    public static boolean tryConnect(JoinMultiplayerScreen screen) {
        cancelAutomaticRetry();

        if (connectedSuccessfully) {
            return false;
        }

        AutoConnectConfig config = AutoConnectConfig.get();
        AutoConnectRetryCounters.AttemptCounter attemptCounter = RETRY_COUNTERS.autoConnect();
        if (!canRetry(config, attemptCounter)) {
            return false;
        }

        return connect(screen, config, attemptCounter);
    }

    public static boolean reconnectManually(JoinMultiplayerScreen screen) {
        cancelAutomaticRetry();

        AutoConnectConfig config = AutoConnectConfig.get();
        if (!canUseAutoConnect(config)) {
            return false;
        }

        RETRY_COUNTERS.reconnect().reset();
        return connect(screen, config, RETRY_COUNTERS.reconnect());
    }

    public static void beginManualConnectionAttempt() {
        beginConnectionAttempt(RETRY_COUNTERS.manualJoin());
    }

    private static void beginConnectionAttempt(AutoConnectRetryCounters.AttemptCounter attemptCounter) {
        activeAttemptCounter = attemptCounter;
        connectionAttemptInProgress = true;
        connectedSuccessfully = false;
    }

    private static boolean connect(JoinMultiplayerScreen screen, AutoConnectConfig config, AutoConnectRetryCounters.AttemptCounter attemptCounter) {
        String configuredAddress = config.connectAddress();
        if (!AutoConnectServerAddress.isUsable(configuredAddress)) {
            showInvalidAddressFeedback(configuredAddress);
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ServerData target = new ServerData("AutoConnect", configuredAddress, ServerData.Type.OTHER);
        ServerAddress address = ServerAddress.parseString(target.ip);
        attemptCounter.recordAttempt();
        beginConnectionAttempt(attemptCounter);
        config.rememberServer(target.ip);
        ConnectScreen.startConnecting(screen, minecraft, address, target, false, null);
        return true;
    }

    public static void markConnectedSuccessfullyIfAttempting() {
        if (!connectionAttemptInProgress) {
            return;
        }

        connectionAttemptInProgress = false;
        connectedSuccessfully = true;
        cancelAutomaticRetry();
    }

    public static void markConnectionAttemptFailed() {
        connectionAttemptInProgress = false;
    }

    public static void prepareDisconnectedRetry() {
        AutoConnectConfig config = AutoConnectConfig.get();
        if (!canRetry(config, activeAttemptCounter)) {
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
            retryDisconnected(screen);
        }
    }

    public static Component disconnectedRetryMessage() {
        long seconds = disconnectedRetrySeconds();
        if (seconds == 0L) {
            return Component.translatable("text.autoconnect.reconnecting");
        }

        return Component.translatable("text.autoconnect.reconnect_countdown", seconds);
    }

    public static long disconnectedRetrySeconds() {
        return Math.max(0L, (millisUntilRetry() + 999L) / 1000L);
    }

    public static boolean shouldShowDisconnectedControls() {
        return canUseAutoConnect(AutoConnectConfig.get());
    }

    public static boolean shouldShowDisconnectedRetryStatus() {
        AutoConnectConfig config = AutoConnectConfig.get();
        return !connectedSuccessfully && config.retryOnFailure && canRetry(config, activeAttemptCounter);
    }

    private static boolean canRetry(AutoConnectConfig config, AutoConnectRetryCounters.AttemptCounter attemptCounter) {
        if (!canUseAutoConnect(config)) {
            return false;
        }

        if (!AutoConnectServerAddress.isUsable(config.connectAddress())) {
            return false;
        }

        return attemptCounter.canAttempt(config);
    }

    private static boolean canUseAutoConnect(AutoConnectConfig config) {
        return config.enabled && !config.connectAddress().isBlank();
    }

    private static boolean retryDisconnected(JoinMultiplayerScreen screen) {
        cancelAutomaticRetry();

        AutoConnectConfig config = AutoConnectConfig.get();
        if (!canRetry(config, activeAttemptCounter)) {
            return false;
        }

        return connect(screen, config, activeAttemptCounter);
    }

    private static long millisUntilRetry() {
        return retryAtMillis - System.currentTimeMillis();
    }

    private static void showInvalidAddressFeedback(String address) {
        Component message = Component.translatable("text.autoconnect.server_address.invalid", address);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && trySetOverlayMessage(minecraft, message)) {
            return;
        }

        LOGGER.warning("AutoConnect saved server address is invalid: " + address);
    }

    private static boolean trySetOverlayMessage(Minecraft minecraft, Component message) {
        try {
            minecraft.gui.getClass().getMethod("setOverlayMessage", Component.class, boolean.class)
                    .invoke(minecraft.gui, message, false);
            return true;
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            LOGGER.log(Level.FINE, "Minecraft overlay message API is unavailable.", exception);
            return false;
        } catch (InvocationTargetException exception) {
            LOGGER.log(Level.FINE, "Failed to show AutoConnect invalid-address feedback.", exception);
            return false;
        }
    }
}
