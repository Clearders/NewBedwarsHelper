package org.exmple.newbedwarshelper.client.statsfetcher;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public final class StatsCopyComponents {
    private static final String COPY_TOOLTIP_KEY = "commands.newbedwarshelper.statsfetcher.copy.tooltip";

    private StatsCopyComponents() {
    }

    public static MutableComponent appendCopyButton(Component base, String clipboardText) {
        return base.copy().append(createCopyButton(clipboardText));
    }

    public static MutableComponent createCopyButton(String clipboardText) {
        return Component.literal(" [C]")
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent.CopyToClipboard(sanitizeClipboardText(clipboardText)))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable(COPY_TOOLTIP_KEY))));
    }

    private static String sanitizeClipboardText(String clipboardText) {
        return clipboardText == null ? "" : clipboardText.replace('\r', ' ').replace('\n', ' ');
    }
}
