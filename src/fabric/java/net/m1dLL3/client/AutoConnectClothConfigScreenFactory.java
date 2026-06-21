package net.m1dLL3.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AutoConnectClothConfigScreenFactory {
    private final AutoConnectConfig config = AutoConnectConfig.get();

    public Screen getConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("AutoConnect"))
                .transparentBackground()
                .setSavingRunnable(config::save);

        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory category = builder.getOrCreateCategory(Component.literal("Connection"));

        category.addEntry(entries.startTextDescription(Component.literal("Controls when AutoConnect should join a server, which server it uses, and whether failed joins should be retried.")).build());

        category.addEntry(entries.startBooleanToggle(Component.literal("Enabled"), config.enabled)
                .setDefaultValue(true)
                .setTooltip(
                        Component.literal("Turns AutoConnect on or off."),
                        Component.literal("When enabled, opening Multiplayer automatically starts one connection attempt."))
                .setSaveConsumer(value -> config.enabled = value)
                .build());

        category.addEntry(entries.startStrField(Component.literal("Server Address"), config.connectAddress())
                .setDefaultValue("")
                .setTooltip(
                        Component.literal("Server address to join automatically."),
                        Component.literal("Leave blank to skip auto-connect until a server is configured."),
                        Component.literal(lastServerDescription()))
                .setSaveConsumer(value -> config.serverAddress = value == null ? "" : value.trim())
                .build());

        var retryOnFailure = entries.startBooleanToggle(Component.literal("Retry on Failure"), config.retryOnFailure)
                .setDefaultValue(false)
                .setTooltip(
                        Component.literal("Controls whether AutoConnect should try again after a failed connection."),
                        Component.literal("When enabled, retries start automatically from the disconnect screen."))
                .setSaveConsumer(value -> config.retryOnFailure = value)
                .build();
        category.addEntry(retryOnFailure);

        category.addEntry(entries.startIntField(Component.literal("Retries Count"), config.retryCount)
                .setDefaultValue(0)
                .setMin(0)
                .setMax(AutoConnectConfig.MAX_RETRY_COUNT)
                .setRequirement(retryOnFailure::getValue)
                .setTooltip(
                        Component.literal("Additional attempts after the first failed connection."),
                        Component.literal("For example, 2 means the first attempt plus two retries."))
                .setSaveConsumer(value -> config.retryCount = Math.max(0, value))
                .build());

        category.addEntry(entries.startIntField(Component.literal("Automatic Retry Timeout (in seconds)"), config.retryDelaySeconds)
                .setDefaultValue(0)
                .setMin(0)
                .setMax(AutoConnectConfig.MAX_RETRY_DELAY_SECONDS)
                .setRequirement(retryOnFailure::getValue)
                .setTooltip(
                        Component.literal("Seconds to wait before an automatic retry on the disconnect screen."),
                        Component.literal("0 means retry as soon as the disconnect screen opens."))
                .setSaveConsumer(value -> config.retryDelaySeconds = Math.max(0, value))
                .build());

        return builder.build();
    }

    private String lastServerDescription() {
        if (config.lastServerAddress == null || config.lastServerAddress.isBlank()) {
            return "Last connected server: none remembered yet.";
        }

        return "Last connected server: " + config.lastServerAddress;
    }
}
