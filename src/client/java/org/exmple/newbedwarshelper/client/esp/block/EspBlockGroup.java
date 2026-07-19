package org.exmple.newbedwarshelper.client.esp.block;

import java.util.List;

public record EspBlockGroup(String id, String titleKey, List<EspBlockTarget> targets) {
    public EspBlockGroup {
        targets = List.copyOf(targets);
    }
}
