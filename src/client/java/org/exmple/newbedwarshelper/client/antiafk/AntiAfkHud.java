package org.exmple.newbedwarshelper.client.antiafk;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.exmple.newbedwarshelper.ModConstants;

public final class AntiAfkHud {
    private static final Identifier TEXTURE_SMALL = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/smallantiafkicon.png");
    private static final Identifier TEXTURE_LARGE = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/antiafkicon.png");
    private static final int TEX_SMALL = 16;
    private static final int TEX_LARGE = 64;
    private static final int DRAW_SIZE = 16;

    private AntiAfkHud() {
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (!AntiAFKManager.shouldRenderHud()) {
            return;
        }

        boolean useSmall = AntiAFKManager.isSmallIcon();
        Identifier texture = useSmall ? TEXTURE_SMALL : TEXTURE_LARGE;
        int textureSize = useSmall ? TEX_SMALL : TEX_LARGE;
        int x = 8;
        int y = (graphics.guiHeight() - DRAW_SIZE) / 2;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x,
                y,
                0,
                0,
                DRAW_SIZE,
                DRAW_SIZE,
                textureSize,
                textureSize,
                textureSize,
                textureSize
        );
    }
}
