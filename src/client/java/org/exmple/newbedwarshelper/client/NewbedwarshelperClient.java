package org.exmple.newbedwarshelper.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.exmple.newbedwarshelper.client.antiafk.AntiAFKManager;
import org.exmple.newbedwarshelper.client.esp.EspTargetStorage;
import org.exmple.newbedwarshelper.client.gui.KeyMappingManager;
import org.exmple.newbedwarshelper.client.hitboxenhance.HitboxEnhanceTargetStorage;
import org.exmple.newbedwarshelper.client.isp.IspTargetStorage;
import org.exmple.newbedwarshelper.client.itemmodelenhance.ItemScaleRegistry;
import org.exmple.newbedwarshelper.client.utils.AsyncExecutor;
import org.exmple.newbedwarshelper.client.z_commands.itemmodelenhance.ImeCommand;
import org.exmple.newbedwarshelper.client.z_commands.statsfetcher.WebCommand;
import org.exmple.newbedwarshelper.client.z_commands.statsfetcher.WeballCommand;

public class NewbedwarshelperClient implements ClientModInitializer {
    public static final String NAMESPACE = "newbedwarshelper";
    @Override
    public void onInitializeClient() {
        AntiAFKManager.init();
        KeyMappingManager.init();
        EspTargetStorage.init();
        HitboxEnhanceTargetStorage.init();
        IspTargetStorage.init();
        ItemScaleRegistry.init();
        WebCommand.register();
        WeballCommand.register();
        ImeCommand.register();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            EspTargetStorage.clearTemporaryOverrides();
            HitboxEnhanceTargetStorage.clearTemporaryOverrides();
            IspTargetStorage.clearTemporaryOverrides();
            AsyncExecutor.shutdown();
        });
    }
}
