package org.exmple.newbedwarshelper.client.mixin.enemystatusviewer;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.exmple.newbedwarshelper.client.enemystatusviewer.BedwarsSidebarProtectionRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTeam.class)
public class PlayerTeamSidebarProtectionMixin {
    @Inject(method = "formatNameForTeam", at = @At("RETURN"), cancellable = true)
    private static void appendBedwarsProtectionLevel(Team team, Component name, CallbackInfoReturnable<MutableComponent> cir) {
        cir.setReturnValue(BedwarsSidebarProtectionRenderer.appendProtectionLevel(team, cir.getReturnValue()));
    }
}
