package org.exmple.newbedwarshelper.client.esp.block.render;

import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import org.exmple.newbedwarshelper.client.esp.block.EspBlockColors;
import org.exmple.newbedwarshelper.client.esp.block.EspBlockTarget;

public final class EspBlockRenderColor {
    private static final int DEFAULT_OUTLINE_COLOR = 0xFFFF55DD;

    private EspBlockRenderColor() {
    }

    public static int colorFor(EspBlockTarget target, BlockState state) {
        int color = EspBlockColors.colorFor(target, state);
        return color == EspBlockColors.DEFAULT_COLOR ? DEFAULT_OUTLINE_COLOR : ARGB.opaque(color);
    }
}
