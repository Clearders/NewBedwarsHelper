package org.exmple.newbedwarshelper.client.esp.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import org.exmple.newbedwarshelper.client.esp.EspGlobalState;
import org.exmple.newbedwarshelper.client.esp.EspTargetWhitelist;
import org.exmple.newbedwarshelper.client.esp.EspTempToggleMode;
import org.exmple.newbedwarshelper.client.esp.EspToggleAction;
import org.exmple.newbedwarshelper.client.z_config.ModConfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EspEntityStorage {
    private static final EspTargetWhitelist<EntityType<?>> ENTITY_TARGETS = new EspTargetWhitelist<>(EspEntityStorage::createDefaultWhitelist);
    private static boolean dangerousWitherSkullWhitelist;
    private static Boolean temporaryDangerousWitherSkullOverride;
    private static boolean initialized;

    private EspEntityStorage() {
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

        if (!EspGlobalState.isEnabled()) {
            return false;
        }

        if (entity instanceof WitherSkull witherSkull && witherSkull.isDangerous()) {
            return isDangerousWitherSkullEspEnabled();
        }

        if (!(entity instanceof LivingEntity)) {
            return entity.getType() != EntityTypes.ITEM && isEntityTypeEspEnabled(entity.getType());
        }

        return isEntityTypeEspEnabled(entity.getType());
    }

    public static synchronized boolean shouldGlowDroppedItem(ItemEntity entity) {
        init();

        if (!EspGlobalState.isEnabled()) {
            return false;
        }

        return isEntityTypeEspEnabled(entity.getType());
    }

    public static synchronized boolean isEntityTypeEspEnabled(EntityType<?> entityType) {
        init();
        return ENTITY_TARGETS.isEnabled(entityType);
    }

    public static synchronized boolean isEntityTypePersistentlyEspEnabled(EntityType<?> entityType) {
        init();
        return ENTITY_TARGETS.isPersistentlyEnabled(entityType);
    }

    public static synchronized boolean isDangerousWitherSkullEspEnabled() {
        init();
        if (temporaryDangerousWitherSkullOverride != null) {
            return temporaryDangerousWitherSkullOverride;
        }

        return dangerousWitherSkullWhitelist;
    }

    public static synchronized boolean isDangerousWitherSkullPersistentlyEspEnabled() {
        init();
        return dangerousWitherSkullWhitelist;
    }

    public static synchronized void setEntityTypeEspEnabled(EntityType<?> entityType, boolean enabled) {
        init();
        ENTITY_TARGETS.setEnabled(entityType, enabled);
        saveWhitelistToDisk();
    }

    public static synchronized void setDangerousWitherSkullEspEnabled(boolean enabled) {
        init();
        dangerousWitherSkullWhitelist = enabled;
        saveWhitelistToDisk();
    }

    public static synchronized void setEntityTypesEspEnabled(List<EntityType<?>> entityTypes, boolean enabled) {
        init();
        ENTITY_TARGETS.setAllEnabled(entityTypes, enabled);
        if (includesWitherSkull(entityTypes)) {
            dangerousWitherSkullWhitelist = enabled;
        }

        saveWhitelistToDisk();
    }

    public static synchronized EspToggleAction getNextGroupToggleAction(List<EntityType<?>> entityTypes) {
        init();
        boolean allEntitiesEnabled = ENTITY_TARGETS.getNextGroupToggleAction(entityTypes) == EspToggleAction.DISABLE_ALL;
        boolean allEnabled = allEntitiesEnabled && (!includesWitherSkull(entityTypes) || dangerousWitherSkullWhitelist);
        return allEnabled ? EspToggleAction.DISABLE_ALL : EspToggleAction.ENABLE_ALL;
    }

    public static synchronized void applyNextGroupToggleAction(List<EntityType<?>> entityTypes) {
        EspToggleAction action = getNextGroupToggleAction(entityTypes);
        setEntityTypesEspEnabled(entityTypes, action == EspToggleAction.ENABLE_ALL);
    }

    public static synchronized EspTempToggleMode getGroupTempToggleMode(List<EntityType<?>> entityTypes) {
        init();
        EspTempToggleMode entityMode = ENTITY_TARGETS.getGroupTempToggleMode(entityTypes);
        if (entityMode == EspTempToggleMode.NONE) {
            return EspTempToggleMode.NONE;
        }

        if (includesWitherSkull(entityTypes)) {
            if (temporaryDangerousWitherSkullOverride == null) {
                return EspTempToggleMode.NONE;
            }
            boolean entityModeValue = entityMode == EspTempToggleMode.ALL_ON;
            if (!Boolean.valueOf(entityModeValue).equals(temporaryDangerousWitherSkullOverride)) {
                return EspTempToggleMode.NONE;
            }
        }

        return entityMode;
    }

    public static synchronized void cycleGroupTempToggleMode(List<EntityType<?>> entityTypes) {
        EspTempToggleMode nextMode = getGroupTempToggleMode(entityTypes).next();
        setGroupTempToggleMode(entityTypes, nextMode);
    }

    public static synchronized void setGroupTempToggleMode(List<EntityType<?>> entityTypes, EspTempToggleMode mode) {
        init();
        ENTITY_TARGETS.setGroupTempToggleMode(entityTypes, mode);
        if (includesWitherSkull(entityTypes)) {
            temporaryDangerousWitherSkullOverride = mode == EspTempToggleMode.NONE ? null : mode == EspTempToggleMode.ALL_ON;
        }
    }

    public static synchronized void clearTemporaryOverrides() {
        ENTITY_TARGETS.clearTemporaryOverrides();
        temporaryDangerousWitherSkullOverride = null;
    }

    public static synchronized void resetWhitelistToDefaults() {
        init();
        ENTITY_TARGETS.resetToDefaults();
        dangerousWitherSkullWhitelist = false;
        temporaryDangerousWitherSkullOverride = null;
        saveWhitelistToDisk();
    }

    public static synchronized Map<EntityType<?>, Boolean> getEntityWhitelistSnapshot() {
        init();
        return ENTITY_TARGETS.persistentTargets();
    }

    private static boolean includesWitherSkull(List<EntityType<?>> entityTypes) {
        return entityTypes.contains(EntityTypes.WITHER_SKULL);
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

        whitelist.put(EntityTypes.PLAYER, Boolean.TRUE);
        return whitelist;
    }

    public static synchronized void loadWhitelistFromDisk() {
        ModConfig.EspConfig config = ModConfig.getInstance().esp;

        for (EntityType<?> entityType : ENTITY_TARGETS.persistentTargetKeys()) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            Boolean value = config.entityWhitelist.get(id.toString());
            if (value != null) {
                ENTITY_TARGETS.setEnabled(entityType, value);
            }
        }

        dangerousWitherSkullWhitelist = config.dangerousWitherSkullWhitelist;

        saveWhitelistToDisk();
    }

    public static synchronized void saveWhitelistToDisk() {
        ModConfig config = ModConfig.getInstance();
        config.esp.entityWhitelist.clear();

        for (Map.Entry<EntityType<?>, Boolean> entry : ENTITY_TARGETS.persistentTargets().entrySet()) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey());
            config.esp.entityWhitelist.put(id.toString(), Boolean.TRUE.equals(entry.getValue()));
        }
        config.esp.dangerousWitherSkullWhitelist = dangerousWitherSkullWhitelist;
        config.save();
    }
}
