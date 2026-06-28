package org.exmple.newbedwarshelper.client.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.blockentity.state.ShulkerBoxRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.exmple.newbedwarshelper.client.esp.EspBlockEntityTarget;
import org.exmple.newbedwarshelper.client.esp.EspTargetStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public class LevelRendererChestOutlineMixin {
    @Inject(
            method = "extractVisibleBlockEntities",
            at = @At("RETURN")
    )
    private void enableOutlinePassForChests(Camera camera, float deltaPartialTick, LevelRenderState levelRenderState, CallbackInfo ci) {
        if (levelRenderState.shouldShowEntityOutlines) {
            return;
        }

        for (var state : levelRenderState.blockEntityRenderStates) {
            if (state instanceof ChestRenderState chestState && shouldGlowChest(chestState)) {
                levelRenderState.shouldShowEntityOutlines = true;
                return;
            }

            if (state instanceof ShulkerBoxRenderState && EspTargetStorage.shouldGlowBlockEntity(EspBlockEntityTarget.SHULKER_BOX)) {
                levelRenderState.shouldShowEntityOutlines = true;
                return;
            }
        }
    }

    private static boolean shouldGlowChest(ChestRenderState state) {
        EspBlockEntityTarget target = switch (state.material) {
            case REGULAR, CHRISTMAS, COPPER_UNAFFECTED, COPPER_EXPOSED, COPPER_WEATHERED, COPPER_OXIDIZED -> EspBlockEntityTarget.CHEST;
            case TRAPPED -> EspBlockEntityTarget.TRAPPED_CHEST;
            case ENDER_CHEST -> EspBlockEntityTarget.ENDER_CHEST;
        };
        return EspTargetStorage.shouldGlowBlockEntity(target);
    }
}
