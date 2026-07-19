package org.exmple.newbedwarshelper.client.hitboxenhance;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import org.exmple.newbedwarshelper.client.z_config.ModConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HitboxEnhanceTargetStorage {
    private static final Map<EntityType<?>, Boolean> ENTITY_WHITELIST = createDefaultWhitelist();
    private static final Map<EntityType<?>, Boolean> TEMP_ENTITY_OVERRIDES = new HashMap<>();
    private static boolean dangerousWitherSkullWhitelist = true;
    private static Boolean temporaryDangerousWitherSkullOverride;
    private static boolean initialized;

    private HitboxEnhanceTargetStorage() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        loadWhitelistFromDisk();
        initialized = true;
    }

    public static synchronized boolean shouldEnhance(Entity entity) {
        init();

        if (entity instanceof WitherSkull witherSkull && witherSkull.isDangerous()) {
            return isDangerousWitherSkullEnhanceEnabled();
        }

        return isEntityTypeEnhanceEnabled(entity.getType());
    }

    public static synchronized boolean isEntityTypeEnhanceEnabled(EntityType<?> entityType) {
        init();
        Boolean temporaryOverride = TEMP_ENTITY_OVERRIDES.get(entityType);
        if (temporaryOverride != null) {
            return temporaryOverride;
        }

        return Boolean.TRUE.equals(ENTITY_WHITELIST.get(entityType));
    }

    public static synchronized boolean isEntityTypePersistentlyEnhanceEnabled(EntityType<?> entityType) {
        init();
        return Boolean.TRUE.equals(ENTITY_WHITELIST.get(entityType));
    }

    public static synchronized boolean isDangerousWitherSkullEnhanceEnabled() {
        init();
        if (temporaryDangerousWitherSkullOverride != null) {
            return temporaryDangerousWitherSkullOverride;
        }

        return dangerousWitherSkullWhitelist;
    }

    public static synchronized boolean isDangerousWitherSkullPersistentlyEnhanceEnabled() {
        init();
        return dangerousWitherSkullWhitelist;
    }

    public static synchronized void setEntityTypeEnhanceEnabled(EntityType<?> entityType, boolean enabled) {
        init();
        ENTITY_WHITELIST.put(entityType, enabled);
        saveWhitelistToDisk();
    }

    public static synchronized void setDangerousWitherSkullEnhanceEnabled(boolean enabled) {
        init();
        dangerousWitherSkullWhitelist = enabled;
        saveWhitelistToDisk();
    }

    public static synchronized void setEntityTypesEnhanceEnabled(List<EntityType<?>> entityTypes, boolean enabled) {
        init();
        for (EntityType<?> entityType : entityTypes) {
            ENTITY_WHITELIST.put(entityType, enabled);
        }
        if (includesWitherSkull(entityTypes)) {
            dangerousWitherSkullWhitelist = enabled;
        }

        saveWhitelistToDisk();
    }

    public static synchronized GroupToggleAction getNextGroupToggleAction(List<EntityType<?>> entityTypes) {
        init();
        return areAllEntityTypesPersistentlyEnabled(entityTypes) ? GroupToggleAction.DISABLE_ALL : GroupToggleAction.ENABLE_ALL;
    }

    public static synchronized void applyNextGroupToggleAction(List<EntityType<?>> entityTypes) {
        GroupToggleAction action = getNextGroupToggleAction(entityTypes);
        setEntityTypesEnhanceEnabled(entityTypes, action == GroupToggleAction.ENABLE_ALL);
    }

    public static synchronized TempToggleMode getGroupTempToggleMode(List<EntityType<?>> entityTypes) {
        init();
        Boolean expectedValue = null;
        for (EntityType<?> entityType : entityTypes) {
            Boolean override = TEMP_ENTITY_OVERRIDES.get(entityType);
            if (override == null) {
                return TempToggleMode.NONE;
            }

            if (expectedValue == null) {
                expectedValue = override;
            } else if (!expectedValue.equals(override)) {
                return TempToggleMode.NONE;
            }
        }

        if (includesWitherSkull(entityTypes)) {
            if (temporaryDangerousWitherSkullOverride == null) {
                return TempToggleMode.NONE;
            }
            if (expectedValue == null) {
                expectedValue = temporaryDangerousWitherSkullOverride;
            } else if (!expectedValue.equals(temporaryDangerousWitherSkullOverride)) {
                return TempToggleMode.NONE;
            }
        }

        return Boolean.TRUE.equals(expectedValue) ? TempToggleMode.ALL_ON : TempToggleMode.ALL_OFF;
    }

    public static synchronized void cycleGroupTempToggleMode(List<EntityType<?>> entityTypes) {
        TempToggleMode nextMode = getGroupTempToggleMode(entityTypes).next();
        setGroupTempToggleMode(entityTypes, nextMode);
    }

    public static synchronized void setGroupTempToggleMode(List<EntityType<?>> entityTypes, TempToggleMode mode) {
        init();
        for (EntityType<?> entityType : entityTypes) {
            if (mode == TempToggleMode.NONE) {
                TEMP_ENTITY_OVERRIDES.remove(entityType);
            } else {
                TEMP_ENTITY_OVERRIDES.put(entityType, mode == TempToggleMode.ALL_ON);
            }
        }
        if (includesWitherSkull(entityTypes)) {
            temporaryDangerousWitherSkullOverride = mode == TempToggleMode.NONE ? null : mode == TempToggleMode.ALL_ON;
        }
    }

    public static synchronized void clearTemporaryOverrides() {
        TEMP_ENTITY_OVERRIDES.clear();
        temporaryDangerousWitherSkullOverride = null;
    }

    public static synchronized void resetWhitelistToDefaults() {
        init();
        ENTITY_WHITELIST.clear();
        ENTITY_WHITELIST.putAll(createDefaultWhitelist());
        dangerousWitherSkullWhitelist = true;
        TEMP_ENTITY_OVERRIDES.clear();
        temporaryDangerousWitherSkullOverride = null;
        saveWhitelistToDisk();
    }

    public static synchronized Map<EntityType<?>, Boolean> getEntityWhitelistSnapshot() {
        init();
        return Collections.unmodifiableMap(new LinkedHashMap<>(ENTITY_WHITELIST));
    }

    private static boolean areAllEntityTypesPersistentlyEnabled(List<EntityType<?>> entityTypes) {
        for (EntityType<?> entityType : entityTypes) {
            if (!Boolean.TRUE.equals(ENTITY_WHITELIST.get(entityType))) {
                return false;
            }
        }

        return !includesWitherSkull(entityTypes) || dangerousWitherSkullWhitelist;
    }

    private static boolean includesWitherSkull(List<EntityType<?>> entityTypes) {
        return entityTypes.contains(EntityTypes.WITHER_SKULL);
    }

    private static Map<EntityType<?>, Boolean> createDefaultWhitelist() {
        Map<EntityType<?>, Boolean> whitelist = new LinkedHashMap<>();
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            whitelist.put(entityType, Boolean.TRUE);
        }
        return whitelist;
    }

    public static synchronized void loadWhitelistFromDisk() {
        ModConfig.HitboxEnhanceConfig config = ModConfig.getInstance().hitboxEnhance;

        for (EntityType<?> entityType : ENTITY_WHITELIST.keySet()) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            Boolean value = config.entityWhitelist.get(id.toString());
            if (value != null) {
                ENTITY_WHITELIST.put(entityType, value);
            }
        }

        dangerousWitherSkullWhitelist = config.dangerousWitherSkullWhitelist;
        saveWhitelistToDisk();
    }

    public static synchronized void saveWhitelistToDisk() {
        ModConfig config = ModConfig.getInstance();
        config.hitboxEnhance.entityWhitelist.clear();

        for (Map.Entry<EntityType<?>, Boolean> entry : ENTITY_WHITELIST.entrySet()) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey());
            config.hitboxEnhance.entityWhitelist.put(id.toString(), Boolean.TRUE.equals(entry.getValue()));
        }
        config.hitboxEnhance.dangerousWitherSkullWhitelist = dangerousWitherSkullWhitelist;
        config.save();
    }

    public enum GroupToggleAction {
        ENABLE_ALL,
        DISABLE_ALL
    }

    public enum TempToggleMode {
        NONE,
        ALL_ON,
        ALL_OFF;

        private TempToggleMode next() {
            return switch (this) {
                case NONE -> ALL_ON;
                case ALL_ON -> ALL_OFF;
                case ALL_OFF -> NONE;
            };
        }
    }
}
