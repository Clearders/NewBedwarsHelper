package org.exmple.newbedwarshelper.client.itemmodelenhance;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.exmple.newbedwarshelper.client.z_config.ModConfig;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ItemScaleRegistry {
    private static final float DEFAULT_SCALE = 1.0f;
    private static final float DEFAULT_SCALE_EPSILON = 0.0001f;
    private static final Map<Item, Float> SCALE_MAP = new HashMap<>();
    private static boolean initialized;

    private ItemScaleRegistry() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        importIdScaleMap(ModConfig.getInstance().itemModelEnhance.itemScales);
        initialized = true;
    }

    public static synchronized void setScale(Item item, float scale) {
        init();
        if (isDefaultScale(scale)) {
            SCALE_MAP.remove(item);
        } else {
            SCALE_MAP.put(item, scale);
        }
        saveScalesToDisk();
    }

    public static synchronized float getScale(Item item) {
        init();
        return SCALE_MAP.getOrDefault(item, DEFAULT_SCALE);
    }

    public static synchronized void clearScale(Item item) {
        init();
        SCALE_MAP.remove(item);
        saveScalesToDisk();
    }

    public static synchronized void clearAll() {
        init();
        SCALE_MAP.clear();
        saveScalesToDisk();
    }

    public static synchronized boolean hasCustomScale(Item item) {
        init();
        return SCALE_MAP.containsKey(item);
    }

    public static synchronized Map<String, Float> exportIdScaleMap() {
        init();
        Map<String, Float> out = new LinkedHashMap<>();
        for (Map.Entry<Item, Float> entry : SCALE_MAP.entrySet()) {
            Float scale = entry.getValue();
            if (scale != null && scale > 0.0f && !isDefaultScale(scale)) {
                Identifier id = BuiltInRegistries.ITEM.getKey(entry.getKey());
                out.put(id.toString(), scale);
            }
        }
        return out;
    }

    public static synchronized void importIdScaleMap(Map<String, Float> data) {
        SCALE_MAP.clear();
        if (data == null) {
            initialized = true;
            return;
        }

        for (Map.Entry<String, Float> entry : data.entrySet()) {
            Item item = findItemById(entry.getKey());
            Float scale = entry.getValue();
            if (item != null && item != Items.AIR && scale != null && scale > 0.0f && !isDefaultScale(scale)) {
                SCALE_MAP.put(item, scale);
            }
        }
        initialized = true;
    }

    public static Item findItemById(String itemIdStr) {
        String normalizedId = itemIdStr.contains(":") ? itemIdStr : "minecraft:" + itemIdStr;
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier key = BuiltInRegistries.ITEM.getKey(item);
            if (key.toString().equals(normalizedId)) {
                return item;
            }
        }
        return null;
    }

    private static void saveScalesToDisk() {
        ModConfig config = ModConfig.getInstance();
        config.itemModelEnhance.itemScales.clear();
        config.itemModelEnhance.itemScales.putAll(exportIdScaleMap());
        config.save();
    }

    private static boolean isDefaultScale(float scale) {
        return Math.abs(scale - DEFAULT_SCALE) < DEFAULT_SCALE_EPSILON;
    }
}
