package org.exmple.newbedwarshelper.client.mixin.esp.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.Block.UpdateFlags;
import net.minecraft.world.level.block.state.BlockState;
import org.exmple.newbedwarshelper.client.esp.block.render.EspBlockEspController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public class LevelChunkBlockEspUpdateMixin {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void newbedwarshelper$updateBlockEspPosition(BlockPos pos, BlockState state, @UpdateFlags int flags, CallbackInfoReturnable<BlockState> cir) {
        if (cir.getReturnValue() == null) {
            return;
        }

        LevelChunk chunk = (LevelChunk) (Object) this;
        if (!chunk.getLevel().isClientSide()) {
            return;
        }

        EspBlockEspController.updatePosition(Minecraft.getInstance(), pos, state);
    }
}
