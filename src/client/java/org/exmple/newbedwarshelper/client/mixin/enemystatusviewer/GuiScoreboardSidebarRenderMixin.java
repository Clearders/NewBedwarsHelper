package org.exmple.newbedwarshelper.client.mixin.enemystatusviewer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.scores.Objective;
import org.exmple.newbedwarshelper.client.enemystatusviewer.BedwarsSidebarProtectionRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class GuiScoreboardSidebarRenderMixin {
    @Inject(method = "displayScoreboardSidebar", at = @At("HEAD"))
    private void beginBedwarsSidebarProtectionRender(GuiGraphicsExtractor graphics, Objective objective, CallbackInfo ci) {
        BedwarsSidebarProtectionRenderer.beginSidebarRender();
    }

    @Inject(method = "displayScoreboardSidebar", at = @At("RETURN"))
    private void endBedwarsSidebarProtectionRender(GuiGraphicsExtractor graphics, Objective objective, CallbackInfo ci) {
        BedwarsSidebarProtectionRenderer.endSidebarRender();
    }
}
