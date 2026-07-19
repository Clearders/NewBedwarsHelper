package org.exmple.newbedwarshelper.client.z_debug.blockclassification;

public enum BlockClassificationCategory {
    ORES("ores", "Ores"),
    PLANTS_AND_CROPS("plants_and_crops", "Plants & Crops"),
    REDSTONE_COMPONENTS("redstone_components", "Redstone Components"),
    BUILDING_BLOCKS("building_blocks", "Building Blocks"),
    FUNCTIONAL_BLOCKS("functional_blocks", "Functional Blocks"),
    MISC("misc", "Misc");

    private final String id;
    private final String displayName;

    BlockClassificationCategory(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return this.id;
    }

    public String displayName() {
        return this.displayName;
    }

    public BlockClassificationCategory next() {
        BlockClassificationCategory[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public BlockClassificationCategory previous() {
        BlockClassificationCategory[] values = values();
        return values[(this.ordinal() - 1 + values.length) % values.length];
    }

    public static BlockClassificationCategory byId(String id) {
        for (BlockClassificationCategory category : values()) {
            if (category.id.equals(id)) {
                return category;
            }
        }
        return MISC;
    }
}
