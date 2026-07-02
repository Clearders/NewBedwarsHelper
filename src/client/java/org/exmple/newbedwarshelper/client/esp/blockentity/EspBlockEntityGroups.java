package org.exmple.newbedwarshelper.client.esp.blockentity;

import java.util.List;

public final class EspBlockEntityGroups {
    public static final EspBlockEntityGroup BLOCK_ENTITIES = new EspBlockEntityGroup(
            "screen.newbedwarshelper.esp_whitelist.group.block_entities",
            List.of(
                    EspBlockEntityTarget.CHEST,
                    EspBlockEntityTarget.TRAPPED_CHEST,
                    EspBlockEntityTarget.ENDER_CHEST,
                    EspBlockEntityTarget.SHULKER_BOX
            )
    );

    public static final List<EspBlockEntityGroup> ALL = List.of(BLOCK_ENTITIES);

    private EspBlockEntityGroups() {
    }
}
