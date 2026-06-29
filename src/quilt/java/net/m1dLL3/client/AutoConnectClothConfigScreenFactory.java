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
                .setTitle(Component.translatable("title.autoconnect.config"))
                .transparentBackground()
                .setSavingRunnable(config::save);

        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("category.autoconnect.connection"));

        category.addEntry(entries.startTextDescription(Component.translatable("text.autoconnect.cloth.description")).build());

        category.addEntry(entries.startBooleanToggle(Component.translatable("option.autoconnect.enabled"), config.enabled)
                .setDefaultValue(true)
                .setTooltip(
                        Component.translatable("text.autoconnect.enabled.tooltip"),
                        Component.translatable("text.autoconnect.enabled.description"))
                .setSaveConsumer(value -> config.enabled = value)
                .build());

        category.addEntry(entries.startStrField(Component.translatable("option.autoconnect.server_address"), config.connectAddress())
                .setDefaultValue("")
                .setTooltip(
                        Component.translatable("text.autoconnect.server_address.tooltip"),
                        Component.translatable("text.autoconnect.server_address.blank"),
                        lastServerDescription())
                .setSaveConsumer(value -> config.serverAddress = value == null ? "" : value.trim())
                .build());

        var retryOnFailure = entries.startBooleanToggle(Component.translatable("option.autoconnect.retry_on_failure"), config.retryOnFailure)
                .setDefaultValue(false)
                .setTooltip(
                        Component.translatable("text.autoconnect.retry_on_failure.tooltip"),
                        Component.translatable("text.autoconnect.retry_on_failure.description"))
                .setSaveConsumer(value -> config.retryOnFailure = value)
                .build();
        category.addEntry(retryOnFailure);

        category.addEntry(entries.startIntField(Component.translatable("option.autoconnect.retry_count"), config.retryCount)
                .setDefaultValue(0)
                .setMin(0)
                .setMax(AutoConnectConfig.MAX_RETRY_COUNT)
                .setRequirement(retryOnFailure::getValue)
                .setTooltip(
                        Component.translatable("text.autoconnect.retry_count.description"),
                        Component.translatable("text.autoconnect.retry_count.example"))
                .setSaveConsumer(value -> config.retryCount = Math.max(0, value))
                .build());

        category.addEntry(entries.startIntField(Component.translatable("option.autoconnect.retry_delay_seconds"), config.retryDelaySeconds)
                .setDefaultValue(3)
                .setMin(0)
                .setMax(AutoConnectConfig.MAX_RETRY_DELAY_SECONDS)
                .setRequirement(retryOnFailure::getValue)
                .setTooltip(
                        Component.translatable("text.autoconnect.retry_delay_seconds.tooltip"),
                        Component.translatable("text.autoconnect.retry_delay_seconds.description"))
                .setSaveConsumer(value -> config.retryDelaySeconds = Math.max(0, value))
                .build());

        return builder.build();
    }

    private Component lastServerDescription() {
        if (config.lastServerAddress == null || config.lastServerAddress.isBlank()) {
            return Component.translatable("text.autoconnect.last_server.none");
        }

        return Component.translatable("text.autoconnect.last_server.value", config.lastServerAddress);
    }
}
