package org.exmple.newbedwarshelper.client.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

public final class MultilineSystemToast implements Toast {
    private static final Identifier BACKGROUND_SPRITE = Identifier.withDefaultNamespace("toast/system");
    private static final int MAX_LINE_SIZE = 200;
    private static final int LINE_SPACING = 12;
    private static final int MARGIN = 10;
    private static final long DISPLAY_TIME_MS = 5000L;

    private final Component title;
    private final List<FormattedCharSequence> messageLines;
    private final int width;
    private Visibility wantedVisibility = Visibility.HIDE;

    private MultilineSystemToast(Minecraft client, Component title, Component message) {
        Font font = client.font;
        this.title = title;
        this.messageLines = font.split(message, MAX_LINE_SIZE);
        int messageWidth = this.messageLines.stream().mapToInt(font::width).max().orElse(MAX_LINE_SIZE);
        this.width = Math.max(MAX_LINE_SIZE, Math.max(font.width(title), messageWidth)) + MARGIN * 3;
    }

    public static void add(Minecraft client, Component title, Component message) {
        client.getToastManager().addToast(new MultilineSystemToast(client, title, message));
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
        this.wantedVisibility = fullyVisibleForMs < DISPLAY_TIME_MS * manager.getNotificationDisplayTimeMultiplier()
                ? Visibility.SHOW
                : Visibility.HIDE;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
        graphics.text(font, this.title, 18, 7, -256, false);

        for (int i = 0; i < this.messageLines.size(); i++) {
            graphics.text(font, this.messageLines.get(i), 18, 18 + i * LINE_SPACING, -1, false);
        }
    }
}
