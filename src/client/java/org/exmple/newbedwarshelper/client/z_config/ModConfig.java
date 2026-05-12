package org.exmple.newbedwarshelper.client.z_config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "newbedwarshelper.json";

    private static ModConfig instance;

    public AntiAfkConfig antiAfk = new AntiAfkConfig();
    public EspConfig esp = new EspConfig();
    public HitboxEnhanceConfig hitboxEnhance = new HitboxEnhanceConfig();
    public IspConfig isp = new IspConfig();
    public StatsFetcherConfig statsFetcher = new StatsFetcherConfig();

    public static ModConfig getInstance() {
        if (instance == null) {
            instance = load();
        }

        return instance;
    }

    public static ModConfig load() {
        Path configPath = getConfigPath();
        if (Files.isRegularFile(configPath)) {
            try {
                String content = Files.readString(configPath, StandardCharsets.UTF_8);
                ModConfig config = GSON.fromJson(content, ModConfig.class);
                if (config != null) {
                    config.ensureDefaults();
                    return config;
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        ModConfig config = new ModConfig();
        config.save();
        return config;
    }

    public void save() {
        ensureDefaults();
        Path configPath = getConfigPath();
        try {
            Path parent = configPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(configPath, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void ensureDefaults() {
        if (antiAfk == null) {
            antiAfk = new AntiAfkConfig();
        }
        if (esp == null) {
            esp = new EspConfig();
        }
        if (hitboxEnhance == null) {
            hitboxEnhance = new HitboxEnhanceConfig();
        }
        if (isp == null) {
            isp = new IspConfig();
        }
        if (statsFetcher == null) {
            statsFetcher = new StatsFetcherConfig();
        }
        esp.ensureDefaults();
        hitboxEnhance.ensureDefaults();
        isp.ensureDefaults();
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
    }

    public static class AntiAfkConfig {
        public boolean featureEnabled = true;
        public boolean smallIcon = true;
    }

    public static class EspConfig {
        public Map<String, Boolean> entityWhitelist = new LinkedHashMap<>();
        public Map<String, Boolean> blockEntityWhitelist = new LinkedHashMap<>();
        public boolean dangerousWitherSkullWhitelist = false;

        private void ensureDefaults() {
            if (entityWhitelist == null) {
                entityWhitelist = new LinkedHashMap<>();
            }
            if (blockEntityWhitelist == null) {
                blockEntityWhitelist = new LinkedHashMap<>();
            }
        }
    }

    public static class HitboxEnhanceConfig {
        public Map<String, Boolean> entityWhitelist = new LinkedHashMap<>();
        public boolean dangerousWitherSkullWhitelist = true;

        private void ensureDefaults() {
            if (entityWhitelist == null) {
                entityWhitelist = new LinkedHashMap<>();
            }
        }
    }

    public static class IspConfig {
        public Map<String, Boolean> entityWhitelist = new LinkedHashMap<>();

        private void ensureDefaults() {
            if (entityWhitelist == null) {
                entityWhitelist = new LinkedHashMap<>();
            }
        }
    }

    public static class StatsFetcherConfig {
        public boolean showDangerousPlayers = true;
        public boolean showFinalKD = true;
        public boolean showDoublesFinalKD = true;
        public boolean showQuadsFinalKD = true;
        public boolean showTotalWins = true;
        public double dangerousPlayersKDThreshold = 1.0;
    }
}
