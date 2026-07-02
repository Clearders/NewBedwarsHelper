package org.exmple.newbedwarshelper.client.esp;

public final class EspGlobalState {
    private static boolean enabled;

    private EspGlobalState() {
    }

    public static synchronized boolean isEnabled() {
        return enabled;
    }

    public static synchronized void setEnabled(boolean enabled) {
        EspGlobalState.enabled = enabled;
    }
}
