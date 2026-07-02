package org.exmple.newbedwarshelper.client.esp.block;

import net.minecraft.world.level.block.Block;
import org.exmple.newbedwarshelper.client.esp.EspGlobalState;
import org.exmple.newbedwarshelper.client.esp.EspTargetWhitelist;
import org.exmple.newbedwarshelper.client.esp.EspTempToggleMode;
import org.exmple.newbedwarshelper.client.esp.EspToggleAction;
import org.exmple.newbedwarshelper.client.z_config.ModConfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EspBlockStorage {
    private static final EspTargetWhitelist<EspBlockTarget> BLOCK_TARGETS = new EspTargetWhitelist<>(EspBlockStorage::createDefaultBlockWhitelist);
    private static boolean initialized;

    private EspBlockStorage() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        loadWhitelistFromDisk();
        initialized = true;
    }

    public static synchronized boolean shouldGlowBlock(Block block) {
        init();

        if (!EspGlobalState.isEnabled()) {
            return false;
        }

        return targetForBlock(block) != null;
    }

    public static synchronized EspBlockTarget targetForBlock(Block block) {
        init();
        for (EspBlockTarget target : BLOCK_TARGETS.persistentTargetKeys()) {
            if (target.blocks().contains(block) && BLOCK_TARGETS.isEnabled(target)) {
                return target;
            }
        }
        return null;
    }

    public static synchronized boolean isBlockTargetEspEnabled(EspBlockTarget target) {
        init();
        return BLOCK_TARGETS.isEnabled(target);
    }

    public static synchronized boolean isBlockTargetPersistentlyEspEnabled(EspBlockTarget target) {
        init();
        return BLOCK_TARGETS.isPersistentlyEnabled(target);
    }

    public static synchronized void setBlockTargetEspEnabled(EspBlockTarget target, boolean enabled) {
        init();
        BLOCK_TARGETS.setEnabled(target, enabled);
        saveWhitelistToDisk();
    }

    public static synchronized void setBlockTargetsEspEnabled(List<EspBlockTarget> targets, boolean enabled) {
        init();
        BLOCK_TARGETS.setAllEnabled(targets, enabled);
        saveWhitelistToDisk();
    }

    public static synchronized EspToggleAction getNextBlockGroupToggleAction(List<EspBlockTarget> targets) {
        init();
        return BLOCK_TARGETS.getNextGroupToggleAction(targets);
    }

    public static synchronized void applyNextBlockGroupToggleAction(List<EspBlockTarget> targets) {
        init();
        BLOCK_TARGETS.applyNextGroupToggleAction(targets);
        saveWhitelistToDisk();
    }

    public static synchronized EspTempToggleMode getBlockGroupTempToggleMode(List<EspBlockTarget> targets) {
        init();
        return BLOCK_TARGETS.getGroupTempToggleMode(targets);
    }

    public static synchronized void cycleBlockGroupTempToggleMode(List<EspBlockTarget> targets) {
        init();
        BLOCK_TARGETS.cycleGroupTempToggleMode(targets);
    }

    public static synchronized void clearTemporaryOverrides() {
        BLOCK_TARGETS.clearTemporaryOverrides();
    }

    public static synchronized void resetWhitelistToDefaults() {
        init();
        BLOCK_TARGETS.resetToDefaults();
        saveWhitelistToDisk();
    }

    private static Map<EspBlockTarget, Boolean> createDefaultBlockWhitelist() {
        Map<EspBlockTarget, Boolean> whitelist = new LinkedHashMap<>();
        for (EspBlockGroup group : EspBlockGroups.ALL) {
            for (EspBlockTarget target : group.targets()) {
                whitelist.put(target, Boolean.FALSE);
            }
        }
        return whitelist;
    }

    public static synchronized void loadWhitelistFromDisk() {
        ModConfig.EspConfig config = ModConfig.getInstance().esp;

        for (EspBlockTarget target : BLOCK_TARGETS.persistentTargetKeys()) {
            Boolean value = config.blockWhitelist.get(target.id());
            if (value != null) {
                BLOCK_TARGETS.setEnabled(target, value);
            }
        }

        saveWhitelistToDisk();
    }

    public static synchronized void saveWhitelistToDisk() {
        ModConfig config = ModConfig.getInstance();
        config.esp.blockWhitelist.clear();

        for (Map.Entry<EspBlockTarget, Boolean> entry : BLOCK_TARGETS.persistentTargets().entrySet()) {
            config.esp.blockWhitelist.put(entry.getKey().id(), Boolean.TRUE.equals(entry.getValue()));
        }
        config.save();
    }
}
