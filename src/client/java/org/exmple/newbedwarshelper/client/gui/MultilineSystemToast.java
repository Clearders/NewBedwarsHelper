package org.exmple.newbedwarshelper.client.gui;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;

public final class MultilineSystemToast implements Toast {
    private static final Identifier BACKGROUND_SPRITE = Identifier.withDefaultNamespace("toast/system");
    private static final int MAX_LINE_SIZE = 200;
    private static final int LINE_SPACING = 12;
    private static final int MARGIN = 10;
    private static final long DISPLAY_TIME_MS = 5000L;
    private static final long CONTAINER_DEFER_TIMEOUT_MS = 10000L;
    private static final Deque<PendingToast> pendingToasts = new ArrayDeque<>();

    private final Component title;
    private final Component message;
    private final List<FormattedCharSequence> messageLines;
    private final int width;
    private final long displayTimeMs;
    private final long eventTimestampMs;
    private Visibility wantedVisibility = Visibility.HIDE;
    private boolean pausedForContainer;
    private long pausedRemainingMs;
    private long pausedExpiresAtMs;

    private MultilineSystemToast(
            Minecraft client,
            Component title,
            Component message,
            Component displayMessage,
            long displayTimeMs,
            long eventTimestampMs
    ) {
        Font font = client.font;
        this.title = title;
        this.message = message;
        this.messageLines = font.split(displayMessage, MAX_LINE_SIZE);
        int messageWidth = this.messageLines.stream().mapToInt(font::width).max().orElse(MAX_LINE_SIZE);
        this.width = Math.max(MAX_LINE_SIZE, Math.max(font.width(title), messageWidth)) + MARGIN * 3;
        this.displayTimeMs = displayTimeMs;
        this.eventTimestampMs = eventTimestampMs;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(MultilineSystemToast::onClientTick);
    }

    public static void add(Minecraft client, Component title, Component message) {
        long eventTimestampMs = Util.getMillis();
        long displayTimeMs = getDisplayTimeMs(client);
        if (isContainerScreenOpen(client)) {
            addPending(title, message, displayTimeMs, eventTimestampMs);
            return;
        }

        client.gui.toastManager().addToast(new MultilineSystemToast(
                client,
                title,
                message,
                message,
                displayTimeMs,
                eventTimestampMs
        ));
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return 20 + Math.max(this.messageLines.size(), 1) * LINE_SPACING;
    }

    @Override
    public float yPos(int firstSlotIndex) {
        return firstSlotIndex * Toast.SLOT_HEIGHT;
    }

    @Override
    public Visibility getWantedVisibility() {
        return this.wantedVisibility;
    }

    @Override
    public void update(ToastManager manager, long fullyVisibleForMs) {
        if (isContainerScreenOpen(manager.getMinecraft())) {
            if (!this.pausedForContainer) {
                this.pausedForContainer = true;
                this.pausedRemainingMs = Math.max(1L, this.displayTimeMs - fullyVisibleForMs);
                this.pausedExpiresAtMs = Util.getMillis() + CONTAINER_DEFER_TIMEOUT_MS;
            }

            this.wantedVisibility = Visibility.HIDE;
            return;
        }

        if (this.pausedForContainer) {
            this.wantedVisibility = Visibility.HIDE;
            return;
        }

        this.wantedVisibility = fullyVisibleForMs < this.displayTimeMs ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
        graphics.text(font, this.title, 18, 7, -256, false);

        for (int i = 0; i < this.messageLines.size(); i++) {
            graphics.text(font, this.messageLines.get(i), 18, 18 + i * LINE_SPACING, -1, false);
        }
    }

    @Override
    public void onFinishedRendering() {
        if (this.pausedForContainer && this.pausedRemainingMs > 0) {
            addPending(this.title, this.message, this.pausedRemainingMs, this.eventTimestampMs, this.pausedExpiresAtMs);
        }
    }

    private static void onClientTick(Minecraft client) {
        if (pendingToasts.isEmpty()) {
            return;
        }

        long now = Util.getMillis();
        pendingToasts.removeIf(toast -> toast.expiresAtMs <= now);
        if (pendingToasts.isEmpty() || isContainerScreenOpen(client)) {
            return;
        }

        while (!pendingToasts.isEmpty()) {
            PendingToast pendingToast = pendingToasts.removeFirst();
            Component displayMessage = appendElapsedTime(pendingToast.message, pendingToast.eventTimestampMs, now);
            client.gui.toastManager().addToast(new MultilineSystemToast(
                    client,
                    pendingToast.title,
                    pendingToast.message,
                    displayMessage,
                    pendingToast.displayTimeMs,
                    pendingToast.eventTimestampMs
            ));
        }
    }

    private static void addPending(Component title, Component message, long displayTimeMs, long eventTimestampMs) {
        addPending(title, message, displayTimeMs, eventTimestampMs, Util.getMillis() + CONTAINER_DEFER_TIMEOUT_MS);
    }

    private static void addPending(Component title, Component message, long displayTimeMs, long eventTimestampMs, long expiresAtMs) {
        pendingToasts.addLast(new PendingToast(
                title,
                message,
                displayTimeMs,
                expiresAtMs,
                eventTimestampMs
        ));
    }

    private static Component appendElapsedTime(Component message, long eventTimestampMs, long displayTimestampMs) {
        long elapsedSeconds = Math.max(0L, (displayTimestampMs - eventTimestampMs) / 1000L);
        return Component.empty()
                .append(message)
                .append(Component.literal(" (" + elapsedSeconds + "s ago)"));
    }

    private static boolean isContainerScreenOpen(Minecraft client) {
        return client.gui.screen() instanceof ContainerScreen;
    }

    private static long getDisplayTimeMs(Minecraft client) {
        return (long) (DISPLAY_TIME_MS * client.options.notificationDisplayTime().get());
    }

    private record PendingToast(Component title, Component message, long displayTimeMs, long expiresAtMs, long eventTimestampMs) {
    }
}
