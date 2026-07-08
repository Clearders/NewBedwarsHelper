package org.exmple.newbedwarshelper.client.esp.block.render;

import net.minecraft.world.phys.AABB;

public final class EspBlockBoundsResolver {
    private static final AABB FULL_BLOCK = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final double PADDING = 0.002D;

    private EspBlockBoundsResolver() {
    }

    public static AABB resolve() {
        return FULL_BLOCK.inflate(PADDING);
    }
}
