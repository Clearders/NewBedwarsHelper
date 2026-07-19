package org.exmple.newbedwarshelper.client.esp.block.render.navigation;

import net.minecraft.world.phys.Vec3;
import org.exmple.newbedwarshelper.client.z_config.ModConfig;

public final class EspBlockTracerRenderer {
    private EspBlockTracerRenderer() {
    }

    public static void render(LineEmitter emitter, EspBlockNavigationGroup group, Vec3 startPos) {
        if (group == null || !isEnabled()) {
            return;
        }

        emitter.line(
                startPos.x,
                startPos.y,
                startPos.z,
                group.centerX(),
                group.centerY(),
                group.centerZ(),
                EspBlockNavigationConstants.TRACER_COLOR
        );
    }

    public interface LineEmitter {
        void line(double x1, double y1, double z1, double x2, double y2, double z2, int color);
    }

    private static boolean isEnabled() {
        return Boolean.TRUE.equals(ModConfig.getInstance().esp.showBlockEspTracer);
    }
}
