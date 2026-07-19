package org.exmple.newbedwarshelper.client.esp.block;

import net.minecraft.world.level.block.Block;

import java.util.List;

public record EspBlockTarget(String id, String translationKey, List<Block> blocks, EspBlockTargetColorMode colorMode) {
    public EspBlockTarget {
        blocks = List.copyOf(blocks);
    }
}
