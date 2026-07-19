package org.exmple.newbedwarshelper.client.esp;

public enum EspTempToggleMode {
    NONE,
    ALL_ON,
    ALL_OFF;

    public EspTempToggleMode next() {
        return switch (this) {
            case NONE -> ALL_ON;
            case ALL_ON -> ALL_OFF;
            case ALL_OFF -> NONE;
        };
    }
}
