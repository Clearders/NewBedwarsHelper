package org.exmple.newbedwarshelper.client.z_debug.blockentityclassification;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores debug-only block entity classification output under run/config.
 * Formal ESP logic should not depend on this JSON file at runtime.
 */
public final class BlockEntityClassificationStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int SCHEMA_VERSION = 1;
    private static final Path FILE = FabricLoader.getInstance().getGameDir()
            .resolve("config")
            .resolve("newbedwarshelper")
            .resolve("block-entity-classification-debug.json");

    private BlockEntityClassificationStore() {
    }

    public static BlockEntityClassificationData loadOrCreate(List<ClassifiedBlockEntityEntry> candidates) {
        BlockEntityClassificationData data = new BlockEntityClassificationData();
        List<String> candidateIds = candidates.stream().map(ClassifiedBlockEntityEntry::id).sorted().toList();
        Set<String> candidateIdSet = new HashSet<>(candidateIds);

        data.setMinecraftVersion(currentMinecraftVersion());
        data.setCandidateHash(hashCandidateIds(candidates));
        data.candidateBlockIds().addAll(candidateIds);

        if (Files.isRegularFile(FILE)) {
            loadExistingAssignments(data, candidateIdSet);
        }

        return data;
    }

    public static void save(BlockEntityClassificationData data) throws IOException {
        Files.createDirectories(FILE.getParent());

        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", SCHEMA_VERSION);
        root.addProperty("minecraftVersion", data.minecraftVersion());
        root.addProperty("candidateHash", data.candidateHash());
        root.add("candidateBlockIds", toArray(data.candidateBlockIds()));

        JsonObject categories = new JsonObject();
        for (BlockEntityClassificationCategory category : BlockEntityClassificationCategory.values()) {
            categories.add(category.id(), toArray(new ArrayList<>(data.category(category))));
        }
        root.add("categories", categories);
        root.add("unclassified", toArray(data.unclassifiedBlockIds()));
        root.add("stale", toArray(data.staleBlockIds()));

        try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }
    }

    public static Path file() {
        return FILE;
    }

    private static void loadExistingAssignments(BlockEntityClassificationData data, Set<String> candidateIds) {
        Set<String> seenOldIds = new HashSet<>();

        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject categories = root.has("categories") && root.get("categories").isJsonObject()
                    ? root.getAsJsonObject("categories")
                    : new JsonObject();

            for (BlockEntityClassificationCategory category : BlockEntityClassificationCategory.values()) {
                JsonArray ids = categories.has(category.id()) && categories.get(category.id()).isJsonArray()
                        ? categories.getAsJsonArray(category.id())
                        : new JsonArray();
                for (JsonElement element : ids) {
                    if (!element.isJsonPrimitive()) {
                        continue;
                    }
                    String id = element.getAsString();
                    seenOldIds.add(id);
                    if (candidateIds.contains(id)) {
                        data.assign(id, category);
                    }
                }
            }

            for (String id : seenOldIds) {
                if (!candidateIds.contains(id)) {
                    data.staleBlockIds().add(id);
                }
            }
            data.staleBlockIds().sort(String::compareTo);
        } catch (RuntimeException | IOException ignored) {
            data.staleBlockIds().clear();
        }
    }

    private static JsonArray toArray(List<String> ids) {
        JsonArray array = new JsonArray();
        for (String id : ids.stream().sorted().toList()) {
            array.add(id);
        }
        return array;
    }

    private static String currentMinecraftVersion() {
        String versionName = SharedConstants.getCurrentVersion().name();
        return versionName == null || versionName.isBlank() ? "unknown" : versionName;
    }

    private static String hashCandidateIds(List<ClassifiedBlockEntityEntry> candidates) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String id : candidates.stream().map(ClassifiedBlockEntityEntry::id).sorted().toList()) {
                digest.update(id.getBytes(StandardCharsets.UTF_8));
                digest.update((byte) '\n');
            }
            byte[] bytes = digest.digest();
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
