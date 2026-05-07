package org.exmple.newbedwarshelper.client.gui;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.exmple.newbedwarshelper.ModConstants;
import org.exmple.newbedwarshelper.client.esp.EspTargetStorage;
import org.lwjgl.glfw.GLFW;

public class KeyMappingManager {
    private static final KeyMapping.Category CUSTOM_KEY_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "bed_wars_helper")
    );
    private static final KeyMapping OPEN_ESP_WHITELIST_CONFIG_KEY = new KeyMapping(
            "key.newbedwarshelper.open_esp_whitelist_config",
            GLFW.GLFW_KEY_F9,
            CUSTOM_KEY_CATEGORY
    );
    private static final KeyMapping TOGGLE_GLOBAL_ESP_KEY = new KeyMapping(
            "key.newbedwarshelper.toggle_global_esp",
            GLFW.GLFW_KEY_U,
            CUSTOM_KEY_CATEGORY
    );
    private static boolean initialized;
    private static boolean resetOnNextJoin = true;

    public static void init() {
        if (initialized) {
            return;
        }

        KeyMappingHelper.registerKeyMapping(OPEN_ESP_WHITELIST_CONFIG_KEY);
        KeyMappingHelper.registerKeyMapping(TOGGLE_GLOBAL_ESP_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(KeyMappingManager::onClientTick);
        initialized = true;
    }

    private static void onClientTick(Minecraft client) {
        if (client.player == null || client.level == null) {
            resetOnNextJoin = true;
        } else if (resetOnNextJoin) {
            EspTargetStorage.setGlobalEspEnabled(false);
            resetOnNextJoin = false;
        }

        while (OPEN_ESP_WHITELIST_CONFIG_KEY.consumeClick()) {
            client.setScreen(new EspWhitelistScreen(client, client.screen));
        }

        while (TOGGLE_GLOBAL_ESP_KEY.consumeClick()) {
            boolean enabled = !EspTargetStorage.isGlobalEspEnabled();
            EspTargetStorage.setGlobalEspEnabled(enabled);

            if (client.player != null) {
                Component text = Component.literal(enabled ? "ESP:ON" : "ESP:OFF")
                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
                client.player.sendOverlayMessage(text);
            }
        }
    }
}
