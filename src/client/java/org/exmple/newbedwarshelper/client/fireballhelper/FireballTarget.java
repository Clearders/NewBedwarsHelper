package org.exmple.newbedwarshelper.client.fireballhelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;

public record FireballTarget(BlockPos pos, VoxelShape shape) {
}
