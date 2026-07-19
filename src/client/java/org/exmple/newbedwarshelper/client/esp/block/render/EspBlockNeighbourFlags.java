package org.exmple.newbedwarshelper.client.esp.block.render;

public final class EspBlockNeighbourFlags {
    public static final int FO = 1 << 1;
    public static final int FO_RI = 1 << 2;
    public static final int RI = 1 << 3;
    public static final int BA_RI = 1 << 4;
    public static final int BA = 1 << 5;
    public static final int BA_LE = 1 << 6;
    public static final int LE = 1 << 7;
    public static final int FO_LE = 1 << 8;

    public static final int TO = 1 << 9;
    public static final int TO_FO = 1 << 10;
    public static final int TO_BA = 1 << 11;
    public static final int TO_RI = 1 << 12;
    public static final int TO_LE = 1 << 13;
    public static final int BO = 1 << 14;
    public static final int BO_FO = 1 << 15;
    public static final int BO_BA = 1 << 16;
    public static final int BO_RI = 1 << 17;
    public static final int BO_LE = 1 << 18;

    private EspBlockNeighbourFlags() {
    }
}
