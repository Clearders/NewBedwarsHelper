package org.exmple.newbedwarshelper.client.z_debug.blockentityclassification;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BlockEntityClassificationData {
    private final Map<BlockEntityClassificationCategory, LinkedHashSet<String>> categories = new EnumMap<>(BlockEntityClassificationCategory.class);
    private final List<String> candidateBlockIds = new ArrayList<>();
    private final List<String> staleBlockIds = new ArrayList<>();
    private String minecraftVersion = "";
    private String candidateHash = "";

    public BlockEntityClassificationData() {
        for (BlockEntityClassificationCategory category : BlockEntityClassificationCategory.values()) {
            this.categories.put(category, new LinkedHashSet<>());
        }
    }

    public String minecraftVersion() {
        return this.minecraftVersion;
    }

    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public String candidateHash() {
        return this.candidateHash;
    }

    public void setCandidateHash(String candidateHash) {
        this.candidateHash = candidateHash;
    }

    public List<String> candidateBlockIds() {
        return this.candidateBlockIds;
    }

    public List<String> staleBlockIds() {
        return this.staleBlockIds;
    }

    public Set<String> category(BlockEntityClassificationCategory category) {
        return this.categories.get(category);
    }

    public void assign(String blockId, BlockEntityClassificationCategory category) {
        this.remove(blockId);
        this.categories.get(category).add(blockId);
    }

    public void remove(String blockId) {
        for (Set<String> ids : this.categories.values()) {
            ids.remove(blockId);
        }
    }

    public List<String> unclassifiedBlockIds() {
        Set<String> assigned = new HashSet<>();
        for (Set<String> ids : this.categories.values()) {
            assigned.addAll(ids);
        }

        List<String> unclassified = new ArrayList<>();
        for (String id : this.candidateBlockIds) {
            if (!assigned.contains(id)) {
                unclassified.add(id);
            }
        }
        return unclassified;
    }
}
