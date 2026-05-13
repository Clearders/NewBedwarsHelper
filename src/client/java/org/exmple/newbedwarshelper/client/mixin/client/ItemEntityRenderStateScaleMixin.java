package org.exmple.newbedwarshelper.client.mixin.client;

import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import org.exmple.newbedwarshelper.client.itemmodelenhance.ItemEntityRenderScaleState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntityRenderState.class)
public class ItemEntityRenderStateScaleMixin implements ItemEntityRenderScaleState {
    @Unique
    private float itemmodelenhance$scale = 1.0f;

    @Override
    public float itemmodelenhance$getScale() {
        return this.itemmodelenhance$scale;
    }

    @Override
    public void itemmodelenhance$setScale(float scale) {
        this.itemmodelenhance$scale = scale;
    }
}
