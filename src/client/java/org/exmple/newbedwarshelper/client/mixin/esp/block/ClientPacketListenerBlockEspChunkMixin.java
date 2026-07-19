package org.exmple.newbedwarshelper.client.mixin.esp.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.chunk.LevelChunk;
import org.exmple.newbedwarshelper.client.esp.block.render.EspBlockEspController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerBlockEspChunkMixin {
    @Inject(method = "handleLevelChunkWithLight", at = @At("RETURN"))
    private void newbedwarshelper$scanBlockEspChunk(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }

        LevelChunk chunk = client.level.getChunkSource().getChunk(packet.getX(), packet.getZ(), false);
        if (chunk != null) {
            EspBlockEspController.submitChunk(chunk);
        }
    }

    @Inject(method = "handleForgetLevelChunk", at = @At("RETURN"))
    private void newbedwarshelper$removeBlockEspChunk(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
        EspBlockEspController.removeChunk(packet.pos());
    }
}
