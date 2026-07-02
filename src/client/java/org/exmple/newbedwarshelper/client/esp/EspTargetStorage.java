package org.exmple.newbedwarshelper.client.esp;

import org.exmple.newbedwarshelper.client.esp.blockentity.EspBlockEntityStorage;
import org.exmple.newbedwarshelper.client.esp.entity.EspEntityStorage;

public final class EspTargetStorage {
    private EspTargetStorage() {
    }

    public static synchronized void init() {
        EspEntityStorage.init();
        EspBlockEntityStorage.init();
    }

    public static synchronized void clearTemporaryOverrides() {
        EspEntityStorage.clearTemporaryOverrides();
        EspBlockEntityStorage.clearTemporaryOverrides();
    }

    public static synchronized void resetWhitelistToDefaults() {
        EspEntityStorage.resetWhitelistToDefaults();
        EspBlockEntityStorage.resetWhitelistToDefaults();
    }

    public static synchronized boolean isGlobalEspEnabled() {
        return EspGlobalState.isEnabled();
    }

    public static synchronized void setGlobalEspEnabled(boolean enabled) {
        EspGlobalState.setEnabled(enabled);
    }
}
