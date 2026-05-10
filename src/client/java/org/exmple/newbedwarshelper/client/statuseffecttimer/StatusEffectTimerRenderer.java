package org.exmple.newbedwarshelper.client.statuseffecttimer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.effect.MobEffectInstance;

public class StatusEffectTimerRenderer {

    private StatusEffectTimerRenderer() {
    }

    public static void drawStatusEffectOverlay(Minecraft client,
                                               GuiGraphicsExtractor graphics,
                                               MobEffectInstance statusEffectInstance,
                                               int x,
                                               int y) {
        if (client == null || client.font == null || statusEffectInstance == null) {
            return;
        }

        String duration = getDurationAsString(statusEffectInstance);
        int durationLength = client.font.width(duration);
        graphics.text(client.font, duration,
                x + 13 - durationLength / 2,
                y + 14,
                0x99FFFFFF,
                true);

        int amplifier = statusEffectInstance.getAmplifier();
        if (amplifier > 0) {
            String amplifierString = amplifier < 10
                    ? I18n.get("enchantment.level." + (amplifier + 1))
                    : "**";
            int amplifierLength = client.font.width(amplifierString);
            int amplifierColor = isStrength(statusEffectInstance) ? 0xFFFFD700 : 0x99FFFFFF;
            graphics.text(client.font, amplifierString,
                    x + 22 - amplifierLength,
                    y + 3,
                    amplifierColor,
                    true);
        }
    }

    private static String getDurationAsString(MobEffectInstance statusEffectInstance) {
        if (statusEffectInstance.isInfiniteDuration()) {
            return I18n.get("effect.duration.infinite");
        }

        int ticks = statusEffectInstance.getDuration();
        int seconds = ticks / 20;

        if (seconds >= 3600) {
            return seconds / 3600 + "h";
        } else if (seconds >= 60) {
            return seconds / 60 + "m";
        } else {
            return String.valueOf(seconds);
        }
    }

    private static boolean isStrength(MobEffectInstance statusEffectInstance) {
        return statusEffectInstance.getEffect()
                .unwrapKey()
                .map(key -> "strength".equals(key.identifier().getPath()))
                .orElse(false);
    }
}
