package org.exmple.newbedwarshelper.client.esp;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class EspTargetWhitelist<T> {
    private final Supplier<Map<T, Boolean>> defaultsFactory;
    private final Map<T, Boolean> persistent;
    private final Map<T, Boolean> temporary = new HashMap<>();

    public EspTargetWhitelist(Supplier<Map<T, Boolean>> defaultsFactory) {
        this.defaultsFactory = defaultsFactory;
        this.persistent = new LinkedHashMap<>(defaultsFactory.get());
    }

    public boolean isEnabled(T target) {
        Boolean temporaryOverride = this.temporary.get(target);
        if (temporaryOverride != null) {
            return temporaryOverride;
        }

        return this.isPersistentlyEnabled(target);
    }

    public boolean isPersistentlyEnabled(T target) {
        return Boolean.TRUE.equals(this.persistent.get(target));
    }

    public void setEnabled(T target, boolean enabled) {
        this.persistent.put(target, enabled);
    }

    public void setAllEnabled(List<T> targets, boolean enabled) {
        for (T target : targets) {
            this.persistent.put(target, enabled);
        }
    }

    public EspToggleAction getNextGroupToggleAction(List<T> targets) {
        return this.areAllPersistentlyEnabled(targets) ? EspToggleAction.DISABLE_ALL : EspToggleAction.ENABLE_ALL;
    }

    public void applyNextGroupToggleAction(List<T> targets) {
        EspToggleAction action = this.getNextGroupToggleAction(targets);
        this.setAllEnabled(targets, action == EspToggleAction.ENABLE_ALL);
    }

    public EspTempToggleMode getGroupTempToggleMode(List<T> targets) {
        Boolean expectedValue = null;
        for (T target : targets) {
            Boolean override = this.temporary.get(target);
            if (override == null) {
                return EspTempToggleMode.NONE;
            }

            if (expectedValue == null) {
                expectedValue = override;
            } else if (!expectedValue.equals(override)) {
                return EspTempToggleMode.NONE;
            }
        }

        return Boolean.TRUE.equals(expectedValue) ? EspTempToggleMode.ALL_ON : EspTempToggleMode.ALL_OFF;
    }

    public void cycleGroupTempToggleMode(List<T> targets) {
        this.setGroupTempToggleMode(targets, this.getGroupTempToggleMode(targets).next());
    }

    public void setGroupTempToggleMode(List<T> targets, EspTempToggleMode mode) {
        for (T target : targets) {
            if (mode == EspTempToggleMode.NONE) {
                this.temporary.remove(target);
            } else {
                this.temporary.put(target, mode == EspTempToggleMode.ALL_ON);
            }
        }
    }

    public void clearTemporaryOverrides() {
        this.temporary.clear();
    }

    public void resetToDefaults() {
        this.persistent.clear();
        this.persistent.putAll(this.defaultsFactory.get());
        this.temporary.clear();
    }

    public Map<T, Boolean> persistentTargets() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(this.persistent));
    }

    public Iterable<T> persistentTargetKeys() {
        return this.persistent.keySet();
    }

    private boolean areAllPersistentlyEnabled(List<T> targets) {
        for (T target : targets) {
            if (!Boolean.TRUE.equals(this.persistent.get(target))) {
                return false;
            }
        }

        return true;
    }
}
