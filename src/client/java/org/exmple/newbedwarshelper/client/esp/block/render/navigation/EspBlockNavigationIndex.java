package org.exmple.newbedwarshelper.client.esp.block.render.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.exmple.newbedwarshelper.client.esp.block.render.EspBlockCacheEntry;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class EspBlockNavigationIndex {
    private static final int[][] NEIGHBOURS = {
            {1, 0, 0},
            {-1, 0, 0},
            {0, 1, 0},
            {0, -1, 0},
            {0, 0, 1},
            {0, 0, -1}
    };

    private final List<EspBlockNavigationGroup> groups = new ArrayList<>();
    private final Map<Long, EspBlockNavigationGroup> groupByPos = new HashMap<>();

    public void rebuild(List<EspBlockCacheEntry> entries) {
        this.groups.clear();
        this.groupByPos.clear();

        Map<Long, EspBlockCacheEntry> entriesByPos = new HashMap<>(entries.size() * 2);
        for (EspBlockCacheEntry entry : entries) {
            entriesByPos.put(entry.pos().asLong(), entry);
        }

        Set<Long> visited = new HashSet<>(entries.size() * 2);
        Queue<EspBlockCacheEntry> queue = new ArrayDeque<>();
        int nextGroupId = 1;

        for (EspBlockCacheEntry seed : entries) {
            long seedKey = seed.pos().asLong();
            if (!visited.add(seedKey)) {
                continue;
            }

            EspBlockNavigationGroup group = new EspBlockNavigationGroup(nextGroupId++, seed.target());
            queue.add(seed);

            while (!queue.isEmpty()) {
                EspBlockCacheEntry current = queue.remove();
                group.add(current.pos());

                for (int[] offset : NEIGHBOURS) {
                    long neighbourKey = BlockPos.asLong(
                            current.pos().getX() + offset[0],
                            current.pos().getY() + offset[1],
                            current.pos().getZ() + offset[2]
                    );
                    EspBlockCacheEntry neighbour = entriesByPos.get(neighbourKey);
                    if (neighbour != null && seed.target().equals(neighbour.target()) && visited.add(neighbourKey)) {
                        queue.add(neighbour);
                    }
                }
            }

            this.groups.add(group);
            if (group.isNavigable()) {
                for (Long pos : group.positions()) {
                    this.groupByPos.put(pos, group);
                }
            }
        }
    }

    public EspBlockNavigationGroup nearestNavigableGroup(Vec3 origin) {
        EspBlockNavigationGroup nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (EspBlockNavigationGroup group : this.groups) {
            if (!group.isNavigable()) {
                continue;
            }

            double distance = nearestDistanceToGroup(origin, group);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = group;
            }
        }

        return nearest;
    }

    public EspBlockNavigationGroup groupAt(BlockPos pos) {
        return this.groupByPos.get(pos.asLong());
    }

    private static double nearestDistanceToGroup(Vec3 origin, EspBlockNavigationGroup group) {
        double nearest = Double.MAX_VALUE;
        for (Long packedPos : group.positions()) {
            BlockPos pos = BlockPos.of(packedPos);
            double distance = origin.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            if (distance < nearest) {
                nearest = distance;
            }
        }
        return nearest;
    }
}
