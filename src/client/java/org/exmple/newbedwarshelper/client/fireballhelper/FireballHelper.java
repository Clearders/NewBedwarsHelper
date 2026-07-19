package org.exmple.newbedwarshelper.client.fireballhelper;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

public final class FireballHelper {
    private static final int TARGET_FILL_COLOR = ARGB.colorFromFloat(0.48F, 0.42F, 0.12F, 0.95F);
    private static final int TARGET_OUTLINE_COLOR = 0xFF57FFE1;
    private static final float TARGET_PADDING = 0.02F;

    private FireballHelper() {
    }

    public static void emitTargetHighlight(Minecraft client, float partialTicks) {
        if (client.level == null || client.player == null) {
            return;
        }

        if (!isHoldingFireCharge(client)) {
            return;
        }

        FireballRaycast.findTargetBlock(client.player, partialTicks)
                .ifPresent(FireballHelper::emitTargetShape);
    }

    private static void emitTargetShape(FireballTarget target) {
        BlockPos pos = target.pos();
        for (AABB box : target.shape().toAabbs()) {
            AABB worldBox = box.move(pos).inflate(TARGET_PADDING);
            Gizmos.cuboid(worldBox, GizmoStyle.fill(TARGET_FILL_COLOR));
            Gizmos.cuboid(worldBox, GizmoStyle.stroke(TARGET_OUTLINE_COLOR));
        }
    }

    private static boolean isHoldingFireCharge(Minecraft client) {
        return client.player.getMainHandItem().is(Items.FIRE_CHARGE)
                || client.player.getOffhandItem().is(Items.FIRE_CHARGE);
    }
}
