package org.exmple.newbedwarshelper.client.z_debug.blockentityclassification;

import net.minecraft.network.chat.Component;

import java.util.Locale;

public record ClassifiedBlockEntityEntry(String id, String englishName, String localName) {
    public String searchText() {
        return (this.id + " " + this.englishName + " " + this.localName).toLowerCase(Locale.ROOT);
    }

    public Component title() {
        return Component.literal(this.englishName);
    }

    public Component subtitle() {
        if (this.localName == null || this.localName.isBlank() || this.localName.equals(this.englishName)) {
            return Component.literal(this.id);
        }
        return Component.literal(this.id + " / " + this.localName);
    }
}
