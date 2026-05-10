package org.exmple.newbedwarshelper.client.esp;

public enum EspBlockEntityTarget {
    CHEST("chest", "block_entity.newbedwarshelper.chest"),
    TRAPPED_CHEST("trapped_chest", "block_entity.newbedwarshelper.trapped_chest"),
    ENDER_CHEST("ender_chest", "block_entity.newbedwarshelper.ender_chest"),
    SHULKER_BOX("shulker_box", "block_entity.newbedwarshelper.shulker_box");

    private final String id;
    private final String translationKey;

    EspBlockEntityTarget(String id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    public String id() {
        return this.id;
    }

    public String translationKey() {
        return this.translationKey;
    }
}
