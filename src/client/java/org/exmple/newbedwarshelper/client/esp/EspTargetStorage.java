package org.exmple.newbedwarshelper.client.esp;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import org.exmple.newbedwarshelper.client.z_config.ModConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EspTargetStorage {
    private static final Map<EntityType<?>, Boolean> ENTITY_WHITELIST = createDefaultWhitelist();
    private static final Map<EntityType<?>, Boolean> TEMP_ENTITY_OVERRIDES = new HashMap<>();
    private static final Map<EspBlockEntityTarget, Boolean> BLOCK_ENTITY_WHITELIST = createDefaultBlockEntityWhitelist();
    private static final Map<EspBlockEntityTarget, Boolean> TEMP_BLOCK_ENTITY_OVERRIDES = new HashMap<>();
    private static boolean dangerousWitherSkullWhitelist;
    private static Boolean temporaryDangerousWitherSkullOverride;
    private static boolean initialized;
    private static boolean globalEspEnabled;

    private EspTargetStorage() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        loadWhitelistFromDisk();
        initialized = true;
    }

    public static synchronized boolean shouldGlow(Entity entity) {
        init();

        if (!globalEspEnabled) {
            return false;
        }

        if (entity instanceof WitherSkull witherSkull && witherSkull.isDangerous()) {
            return isDangerousWitherSkullIspEnabled();
        }

        if (!(entity instanceof LivingEntity)) {
            return entity.getType() != EntityType.ITEM && isEntityTypeIspEnabled(entity.getType());
        }

        return isEntityTypeIspEnabled(entity.getType());
    }

    public static synchronized boolean shouldGlowDroppedItem(ItemEntity entity) {
        init();

        if (!globalEspEnabled) {
            return false;
        }

        return isEntityTypeIspEnabled(entity.getType());
    }

    public static synchronized boolean shouldGlowBlockEntity(EspBlockEntityTarget target) {
        init();

        if (!globalEspEnabled) {
            return false;
        }

        return isBlockEntityTargetIspEnabled(target);
    }

    public static synchronized boolean isEntityTypeIspEnabled(EntityType<?> entityType) {
        init();
        Boolean temporaryOverride = TEMP_ENTITY_OVERRIDES.get(entityType);
        if (temporaryOverride != null) {
            return temporaryOverride;
        }

        return Boolean.TRUE.equals(ENTITY_WHITELIST.get(entityType));
    }

    public static synchronized boolean isEntityTypePersistentlyIspEnabled(EntityType<?> entityType) {
        init();
        return Boolean.TRUE.equals(ENTITY_WHITELIST.get(entityType));
    }

    public static synchronized boolean isDangerousWitherSkullIspEnabled() {
        init();
        if (temporaryDangerousWitherSkullOverride != null) {
            return temporaryDangerousWitherSkullOverride;
        }

        return dangerousWitherSkullWhitelist;
    }

    public static synchronized boolean isDangerousWitherSkullPersistentlyIspEnabled() {
        init();
        return dangerousWitherSkullWhitelist;
    }

    public static synchronized boolean isBlockEntityTargetIspEnabled(EspBlockEntityTarget target) {
        init();
        Boolean temporaryOverride = TEMP_BLOCK_ENTITY_OVERRIDES.get(target);
        if (temporaryOverride != null) {
            return temporaryOverride;
        }

        return Boolean.TRUE.equals(BLOCK_ENTITY_WHITELIST.get(target));
    }

    public static synchronized boolean isBlockEntityTargetPersistentlyIspEnabled(EspBlockEntityTarget target) {
        init();
        return Boolean.TRUE.equals(BLOCK_ENTITY_WHITELIST.get(target));
    }

    public static synchronized void setEntityTypeIspEnabled(EntityType<?> entityType, boolean enabled) {
        init();
        ENTITY_WHITELIST.put(entityType, enabled);
        saveWhitelistToDisk();
    }

    public static synchronized void setDangerousWitherSkullIspEnabled(boolean enabled) {
        init();
        dangerousWitherSkullWhitelist = enabled;
        saveWhitelistToDisk();
    }

    public static synchronized void setEntityTypesIspEnabled(List<EntityType<?>> entityTypes, boolean enabled) {
        init();
        for (EntityType<?> entityType : entityTypes) {
            ENTITY_WHITELIST.put(entityType, enabled);
        }
        if (includesWitherSkull(entityTypes)) {
            dangerousWitherSkullWhitelist = enabled;
        }

        saveWhitelistToDisk();
    }

    public static synchronized void setBlockEntityTargetIspEnabled(EspBlockEntityTarget target, boolean enabled) {
        init();
        BLOCK_ENTITY_WHITELIST.put(target, enabled);
        saveWhitelistToDisk();
    }

    public static synchronized void setBlockEntityTargetsIspEnabled(List<EspBlockEntityTarget> targets, boolean enabled) {
        init();
        for (EspBlockEntityTarget target : targets) {
            BLOCK_ENTITY_WHITELIST.put(target, enabled);
        }

        saveWhitelistToDisk();
    }

    public static synchronized GroupToggleAction getNextGroupToggleAction(List<EntityType<?>> entityTypes) {
        init();
        return areAllEntityTypesPersistentlyEnabled(entityTypes) ? GroupToggleAction.DISABLE_ALL : GroupToggleAction.ENABLE_ALL;
    }

    public static synchronized void applyNextGroupToggleAction(List<EntityType<?>> entityTypes) {
        GroupToggleAction action = getNextGroupToggleAction(entityTypes);
        setEntityTypesIspEnabled(entityTypes, action == GroupToggleAction.ENABLE_ALL);
    }

    public static synchronized GroupToggleAction getNextBlockEntityGroupToggleAction(List<EspBlockEntityTarget> targets) {
        init();
        return areAllBlockEntityTargetsPersistentlyEnabled(targets) ? GroupToggleAction.DISABLE_ALL : GroupToggleAction.ENABLE_ALL;
    }

    public static synchronized void applyNextBlockEntityGroupToggleAction(List<EspBlockEntityTarget> targets) {
        GroupToggleAction action = getNextBlockEntityGroupToggleAction(targets);
        setBlockEntityTargetsIspEnabled(targets, action == GroupToggleAction.ENABLE_ALL);
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

    public static synchronized TempToggleMode getBlockEntityGroupTempToggleMode(List<EspBlockEntityTarget> targets) {
        init();
        Boolean expectedValue = null;
        for (EspBlockEntityTarget target : targets) {
            Boolean override = TEMP_BLOCK_ENTITY_OVERRIDES.get(target);
            if (override == null) {
                return TempToggleMode.NONE;
            }

            if (expectedValue == null) {
                expectedValue = override;
            } else if (!expectedValue.equals(override)) {
                return TempToggleMode.NONE;
            }
        }

        return Boolean.TRUE.equals(expectedValue) ? TempToggleMode.ALL_ON : TempToggleMode.ALL_OFF;
    }

    public static synchronized void cycleBlockEntityGroupTempToggleMode(List<EspBlockEntityTarget> targets) {
        TempToggleMode nextMode = getBlockEntityGroupTempToggleMode(targets).next();
        setBlockEntityGroupTempToggleMode(targets, nextMode);
    }

    public static synchronized void setBlockEntityGroupTempToggleMode(List<EspBlockEntityTarget> targets, TempToggleMode mode) {
        init();
        for (EspBlockEntityTarget target : targets) {
            if (mode == TempToggleMode.NONE) {
                TEMP_BLOCK_ENTITY_OVERRIDES.remove(target);
            } else {
                TEMP_BLOCK_ENTITY_OVERRIDES.put(target, mode == TempToggleMode.ALL_ON);
            }
        }
    }

    public static synchronized void clearTemporaryOverrides() {
        TEMP_ENTITY_OVERRIDES.clear();
        TEMP_BLOCK_ENTITY_OVERRIDES.clear();
        temporaryDangerousWitherSkullOverride = null;
    }

    public static synchronized void resetWhitelistToDefaults() {
        init();
        ENTITY_WHITELIST.clear();
        ENTITY_WHITELIST.putAll(createDefaultWhitelist());
        BLOCK_ENTITY_WHITELIST.clear();
        BLOCK_ENTITY_WHITELIST.putAll(createDefaultBlockEntityWhitelist());
        dangerousWitherSkullWhitelist = false;
        TEMP_ENTITY_OVERRIDES.clear();
        TEMP_BLOCK_ENTITY_OVERRIDES.clear();
        temporaryDangerousWitherSkullOverride = null;
        saveWhitelistToDisk();
    }

    public static synchronized boolean isGlobalEspEnabled() {
        return globalEspEnabled;
    }

    public static synchronized void setGlobalEspEnabled(boolean enabled) {
        globalEspEnabled = enabled;
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
        return entityTypes.contains(EntityType.WITHER_SKULL);
    }

    private static boolean areAllBlockEntityTargetsPersistentlyEnabled(List<EspBlockEntityTarget> targets) {
        for (EspBlockEntityTarget target : targets) {
            if (!Boolean.TRUE.equals(BLOCK_ENTITY_WHITELIST.get(target))) {
                return false;
            }
        }

        return true;
    }


    private static Map<EntityType<?>, Boolean> createDefaultWhitelist() {
        Map<EntityType<?>, Boolean> whitelist = new LinkedHashMap<>();
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            if (entityType.getCategory() != MobCategory.MISC) {
                whitelist.put(entityType, Boolean.FALSE);
            }
        }

        for (EntityType<?> entityType : EspEntityGroups.MISC.entityTypes()) {
            whitelist.put(entityType, Boolean.FALSE);
        }

        whitelist.put(EntityType.PLAYER, Boolean.TRUE);
        return whitelist;
    }

    private static Map<EspBlockEntityTarget, Boolean> createDefaultBlockEntityWhitelist() {
        Map<EspBlockEntityTarget, Boolean> whitelist = new LinkedHashMap<>();
        for (EspBlockEntityTarget target : EspBlockEntityTarget.values()) {
            whitelist.put(target, Boolean.FALSE);
        }

        return whitelist;
    }

    public static synchronized void loadWhitelistFromDisk() {
        ModConfig.EspConfig config = ModConfig.getInstance().esp;

        for (EntityType<?> entityType : ENTITY_WHITELIST.keySet()) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            Boolean value = config.entityWhitelist.get(id.toString());
            if (value != null) {
                ENTITY_WHITELIST.put(entityType, value);
            }
        }

        for (EspBlockEntityTarget target : BLOCK_ENTITY_WHITELIST.keySet()) {
            Boolean value = config.blockEntityWhitelist.get(target.id());
            if (value != null) {
                BLOCK_ENTITY_WHITELIST.put(target, value);
            }
        }

        dangerousWitherSkullWhitelist = config.dangerousWitherSkullWhitelist;

        saveWhitelistToDisk();
    }

    public static synchronized void saveWhitelistToDisk() {
        ModConfig config = ModConfig.getInstance();
        config.esp.entityWhitelist.clear();
        config.esp.blockEntityWhitelist.clear();

        for (Map.Entry<EntityType<?>, Boolean> entry : ENTITY_WHITELIST.entrySet()) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey());
            config.esp.entityWhitelist.put(id.toString(), Boolean.TRUE.equals(entry.getValue()));
        }
        for (Map.Entry<EspBlockEntityTarget, Boolean> entry : BLOCK_ENTITY_WHITELIST.entrySet()) {
            config.esp.blockEntityWhitelist.put(entry.getKey().id(), Boolean.TRUE.equals(entry.getValue()));
        }
        config.esp.dangerousWitherSkullWhitelist = dangerousWitherSkullWhitelist;
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


