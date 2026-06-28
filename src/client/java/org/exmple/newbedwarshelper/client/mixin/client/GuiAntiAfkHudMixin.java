package org.exmple.newbedwarshelper.client.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.exmple.newbedwarshelper.client.antiafk.AntiAfkHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Hud.class)
public abstract class GuiAntiAfkHudMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void newbedwarshelper$renderAntiAfkHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (minecraft.options == null || minecraft.gui.hud.isHidden()) {
            return;
        }

        AntiAfkHud.render(graphics);
    }
}
