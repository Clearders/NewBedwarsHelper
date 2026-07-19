package org.exmple.newbedwarshelper.client.mixin.enemystatusviewer;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import org.exmple.newbedwarshelper.client.enemystatusviewer.BedwarsInvisibilityToastNotifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMobEffectMixin {
    @Inject(method = "handleUpdateMobEffect", at = @At("RETURN"))
    private void newbedwarshelper$notifyBedwarsInvisibility(ClientboundUpdateMobEffectPacket packet, CallbackInfo ci) {
        BedwarsInvisibilityToastNotifier.onMobEffectUpdated(packet);
    }

    @Inject(method = "handleRemoveMobEffect", at = @At("RETURN"))
    private void newbedwarshelper$clearBedwarsInvisibility(ClientboundRemoveMobEffectPacket packet, CallbackInfo ci) {
        BedwarsInvisibilityToastNotifier.onMobEffectRemoved(packet);
    }
}
