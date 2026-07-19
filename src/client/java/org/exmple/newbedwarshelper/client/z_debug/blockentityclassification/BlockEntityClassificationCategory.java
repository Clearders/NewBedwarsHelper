package org.exmple.newbedwarshelper.client.z_debug.blockentityclassification;

public enum BlockEntityClassificationCategory {
    STORAGE_BLOCKS("storage_blocks", "Storage Blocks"),
    FUNCTIONAL_BLOCKS("functional_blocks", "Functional Blocks"),
    REDSTONE_COMPONENTS("redstone_components", "Redstone Components"),
    SPAWNER_BLOCKS("spawner_blocks", "Spawner Blocks"),
    MISC("misc", "Misc");

    private final String id;
    private final String displayName;

    BlockEntityClassificationCategory(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return this.id;
    }

    public String displayName() {
        return this.displayName;
    }

    public BlockEntityClassificationCategory next() {
        BlockEntityClassificationCategory[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public BlockEntityClassificationCategory previous() {
        BlockEntityClassificationCategory[] values = values();
        return values[(this.ordinal() - 1 + values.length) % values.length];
    }
}
