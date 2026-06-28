package org.exmple.newbedwarshelper.client.mixin.client;

import com.google.common.collect.Ordering;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.exmple.newbedwarshelper.client.statuseffecttimer.StatusEffectTimerRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Environment(EnvType.CLIENT)
@Mixin(Hud.class)
public abstract class GuiStatusEffectTimerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "extractEffects", at = @At("RETURN"))
    private void newbedwarshelper$appendStatusEffectTimers(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (minecraft.player == null || minecraft.gui.screen() != null && minecraft.gui.screen().showsActiveEffects()) {
            return;
        }

        Collection<MobEffectInstance> activeEffects = minecraft.player.getActiveEffects();
        if (activeEffects.isEmpty()) {
            return;
        }

        int beneficialCount = 0;
        int harmfulCount = 0;

        for (MobEffectInstance instance : Ordering.natural().reverse().sortedCopy(activeEffects)) {
            Holder<MobEffect> effect = instance.getEffect();
            if (!instance.showIcon()) {
                continue;
            }

            int x = graphics.guiWidth();
            int y = 1;
            if (minecraft.isDemo()) {
                y += 15;
            }

            if (effect.value().isBeneficial()) {
                beneficialCount++;
                x -= 25 * beneficialCount;
            } else {
                harmfulCount++;
                x -= 25 * harmfulCount;
                y += 26;
            }

            StatusEffectTimerRenderer.drawStatusEffectOverlay(minecraft, graphics, instance, x, y);
        }
    }
}
