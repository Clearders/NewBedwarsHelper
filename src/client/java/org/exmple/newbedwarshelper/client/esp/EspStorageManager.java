package org.exmple.newbedwarshelper.client.esp;

import org.exmple.newbedwarshelper.client.esp.blockentity.EspBlockEntityStorage;
import org.exmple.newbedwarshelper.client.esp.block.EspBlockStorage;
import org.exmple.newbedwarshelper.client.esp.entity.EspEntityStorage;

public final class EspStorageManager {
    private EspStorageManager() {
    }

    public static synchronized void init() {
        EspEntityStorage.init();
        EspBlockEntityStorage.init();
        EspBlockStorage.init();
    }

    public static synchronized void clearTemporaryOverrides() {
        EspEntityStorage.clearTemporaryOverrides();
        EspBlockEntityStorage.clearTemporaryOverrides();
        EspBlockStorage.clearTemporaryOverrides();
    }

    public static synchronized void resetWhitelistToDefaults() {
        EspEntityStorage.resetWhitelistToDefaults();
        EspBlockEntityStorage.resetWhitelistToDefaults();
        EspBlockStorage.resetWhitelistToDefaults();
    }

    public static synchronized boolean isGlobalEspEnabled() {
        return EspGlobalState.isEnabled();
    }

    public static synchronized void setGlobalEspEnabled(boolean enabled) {
        EspGlobalState.setEnabled(enabled);
    }
}
