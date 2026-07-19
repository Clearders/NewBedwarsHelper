package org.exmple.newbedwarshelper.client.fireballhelper;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

public final class FireballRaycast {
    public static final double RANGE = 300.0D;

    private FireballRaycast() {
    }

    public static Optional<FireballTarget> findTargetBlock(Player player, float partialTicks) {
        HitResult hitResult = player.pick(RANGE, partialTicks, false);
        if (hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
            Level level = player.level();
            BlockState state = level.getBlockState(blockHitResult.getBlockPos());
            VoxelShape shape = state.getShape(level, blockHitResult.getBlockPos(), CollisionContext.of(player));
            if (!shape.isEmpty()) {
                return Optional.of(new FireballTarget(blockHitResult.getBlockPos(), shape));
            }
        }

        return Optional.empty();
    }
}
