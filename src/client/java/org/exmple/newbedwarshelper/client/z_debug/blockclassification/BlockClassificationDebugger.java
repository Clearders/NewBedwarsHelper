package org.exmple.newbedwarshelper.client.z_debug.blockclassification;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.exmple.newbedwarshelper.ModConstants;
import org.lwjgl.glfw.GLFW;

/**
 * Development helper for maintaining the ordinary block ESP category list.
 * This is only enabled when NewbedwarshelperClient explicitly calls init().
 */
public final class BlockClassificationDebugger {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "debug")
    );
    private static final KeyMapping OPEN_KEY = new KeyMapping(
            "key.newbedwarshelper.open_block_classification_debugger",
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );
    private static boolean initialized;

    private BlockClassificationDebugger() {
    }

    public static void init() {
        if (initialized) {
            return;
        }

        KeyMappingHelper.registerKeyMapping(OPEN_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(BlockClassificationDebugger::onClientTick);
        initialized = true;
    }

    private static void onClientTick(Minecraft client) {
        while (OPEN_KEY.consumeClick()) {
            client.gui.setScreen(new BlockClassificationScreen(client, client.gui.screen()));
        }
    }
}
