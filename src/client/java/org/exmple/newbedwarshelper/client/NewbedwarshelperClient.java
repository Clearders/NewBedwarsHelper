package org.exmple.newbedwarshelper.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.exmple.newbedwarshelper.client.esp.EspTargetStorage;
import org.exmple.newbedwarshelper.client.gui.KeyMappingManager;

public class NewbedwarshelperClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyMappingManager.init();
        EspTargetStorage.init();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> EspTargetStorage.clearTemporaryOverrides());
    }
}
