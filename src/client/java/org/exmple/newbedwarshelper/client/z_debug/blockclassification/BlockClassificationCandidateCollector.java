package org.exmple.newbedwarshelper.client.z_debug.blockclassification;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class BlockClassificationCandidateCollector {
    private BlockClassificationCandidateCollector() {
    }

    public static List<ClassifiedBlockEntry> collectOrdinaryBlockCandidates() {
        Set<Block> blockEntityBlocks = collectBlockEntityBlocks();
        List<ClassifiedBlockEntry> entries = new ArrayList<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            if (isIgnoredAir(block) || blockEntityBlocks.contains(block)) {
                continue;
            }

            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            String idString = id.toString();
            String localName = Component.translatable(block.getDescriptionId()).getString();
            entries.add(new ClassifiedBlockEntry(idString, toTitleCase(id.getPath()), localName));
        }

        entries.sort((left, right) -> left.id().compareTo(right.id()));
        return entries;
    }

    private static Set<Block> collectBlockEntityBlocks() {
        Set<Block> blocks = new HashSet<>();
        for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            for (Block block : BuiltInRegistries.BLOCK) {
                if (type.isValid(block.defaultBlockState())) {
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    private static boolean isIgnoredAir(Block block) {
        return block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR;
    }

    private static String toTitleCase(String path) {
        String[] parts = path.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(part.substring(0, 1).toUpperCase(Locale.ROOT));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }
}
