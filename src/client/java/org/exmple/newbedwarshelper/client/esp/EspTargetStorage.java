package org.exmple.newbedwarshelper.client.esp;

import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class EspTargetStorage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("newbedwarshelper-esp-whitelist.properties");

    private static final Map<EntityType<?>, Boolean> ENTITY_WHITELIST = createDefaultWhitelist();
    private static final Map<EntityType<?>, Boolean> TEMP_ENTITY_OVERRIDES = new HashMap<>();
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

        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }

        return isEntityTypeIspEnabled(livingEntity.getType());
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

    public static synchronized void setEntityTypeIspEnabled(EntityType<?> entityType, boolean enabled) {
        init();
        ENTITY_WHITELIST.put(entityType, enabled);
        saveWhitelistToDisk();
    }

    public static synchronized void setEntityTypesIspEnabled(List<EntityType<?>> entityTypes, boolean enabled) {
        init();
        for (EntityType<?> entityType : entityTypes) {
            ENTITY_WHITELIST.put(entityType, enabled);
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
    }

    public static synchronized void clearTemporaryOverrides() {
        TEMP_ENTITY_OVERRIDES.clear();
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

        return true;
    }


    private static Map<EntityType<?>, Boolean> createDefaultWhitelist() {
        Map<EntityType<?>, Boolean> whitelist = new LinkedHashMap<>();
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            if (entityType.getCategory() != MobCategory.MISC) {
                whitelist.put(entityType, Boolean.FALSE);
            }
        }

        whitelist.put(EntityType.PLAYER, Boolean.TRUE);
        return whitelist;
    }

    public static synchronized void loadWhitelistFromDisk() {
        if (!Files.isRegularFile(CONFIG_PATH)) {
            saveWhitelistToDisk();
            return;
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
            properties.load(inputStream);
        } catch (IOException exception) {
            LOGGER.warn("Failed to read ESP whitelist config: {}", CONFIG_PATH, exception);
            return;
        }

        for (EntityType<?> entityType : ENTITY_WHITELIST.keySet()) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            String value = properties.getProperty(id.toString());
            if (value == null) {
                continue;
            }

            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                ENTITY_WHITELIST.put(entityType, Boolean.parseBoolean(value));
            }
        }

        saveWhitelistToDisk();
    }

    public static synchronized void saveWhitelistToDisk() {
        Properties properties = new Properties();
        for (Map.Entry<EntityType<?>, Boolean> entry : ENTITY_WHITELIST.entrySet()) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey());
            properties.setProperty(id.toString(), Boolean.toString(Boolean.TRUE.equals(entry.getValue())));
        }

        try {
            Path parent = CONFIG_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (OutputStream outputStream = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(outputStream, "NewBedwarsHelper ESP whitelist");
            }
        } catch (IOException exception) {
            LOGGER.warn("Failed to write ESP whitelist config: {}", CONFIG_PATH, exception);
        }
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


