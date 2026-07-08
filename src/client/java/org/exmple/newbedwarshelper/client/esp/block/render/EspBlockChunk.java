package org.exmple.newbedwarshelper.client.esp.block.render;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class EspBlockChunk {
    private final ChunkPos pos;
    private final Map<Long, EspBlockCacheEntry> entries = new HashMap<>();

    public EspBlockChunk(ChunkPos pos) {
        this.pos = pos;
    }

    public ChunkPos pos() {
        return this.pos;
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public int size() {
        return this.entries.size();
    }

    public void put(EspBlockCacheEntry entry) {
        this.entries.put(entry.pos().asLong(), entry);
    }

    public void remove(BlockPos pos) {
        this.entries.remove(pos.asLong());
    }

    public EspBlockCacheEntry get(BlockPos pos) {
        return this.entries.get(pos.asLong());
    }

    public List<EspBlockCacheEntry> snapshot() {
        return new ArrayList<>(this.entries.values());
    }

    public void forEachEntry(Consumer<EspBlockCacheEntry> consumer) {
        this.entries.values().forEach(consumer);
    }
}
