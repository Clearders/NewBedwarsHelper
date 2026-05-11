package org.exmple.newbedwarshelper.client.statsfetcher;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.exmple.newbedwarshelper.client.z_config.ModConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class BWStatsFormatter {
    private static final LinkedHashMap<String, Function<BWStatsExtractor.BWStats, String>> STAT_FIELDS =
            new LinkedHashMap<>();

    static {
        STAT_FIELDS.put(BWStatsConstants.BWCONST_FINAL_KD, BWStatsExtractor.BWStats::getFinalKD);
        STAT_FIELDS.put(BWStatsConstants.BWCONST_DOUBLES_FINAL_KD, BWStatsExtractor.BWStats::getFinalKD2v2);
        STAT_FIELDS.put(BWStatsConstants.BWCONST_4V4V4V4_FINAL_KD, BWStatsExtractor.BWStats::getFinalKD4v4);
        STAT_FIELDS.put(BWStatsConstants.BWCONST_TOTALWINS, BWStatsExtractor.BWStats::getTotalWins);
    }

    private BWStatsFormatter() {
    }

    public static List<Component> formatStats(BWStatsExtractor.BWStats stats) {
        List<Component> components = new ArrayList<>();
        ModConfig.StatsFetcherConfig config = ModConfig.getInstance().statsFetcher;

        for (Map.Entry<String, Function<BWStatsExtractor.BWStats, String>> entry : STAT_FIELDS.entrySet()) {
            String label = entry.getKey();
            String value = entry.getValue().apply(stats);

            if (shouldDisplayField(label, config)) {
                components.add(formatLine(label, value));
            }
        }

        return components;
    }

    public static double parseStatAsDouble(String statValue) {
        try {
            return Double.parseDouble(statValue);
        } catch (NumberFormatException exception) {
            return Double.NaN;
        }
    }

    private static boolean shouldDisplayField(String fieldLabel, ModConfig.StatsFetcherConfig config) {
        if (BWStatsConstants.BWCONST_FINAL_KD.equalsIgnoreCase(fieldLabel)) {
            return config.showFinalKD;
        } else if (BWStatsConstants.BWCONST_DOUBLES_FINAL_KD.equalsIgnoreCase(fieldLabel)) {
            return config.showDoublesFinalKD;
        } else if (BWStatsConstants.BWCONST_4V4V4V4_FINAL_KD.equalsIgnoreCase(fieldLabel)) {
            return config.showQuadsFinalKD;
        } else if (BWStatsConstants.BWCONST_TOTALWINS.equalsIgnoreCase(fieldLabel)) {
            return config.showTotalWins;
        }

        return true;
    }

    private static Component formatLine(String label, String value) {
        return Component.literal(getDisplayLabel(label) + ": ")
                .withStyle(ChatFormatting.AQUA)
                .append(formatValue(value));
    }

    private static String getDisplayLabel(String label) {
        if (BWStatsConstants.BWCONST_TOTALWINS.equalsIgnoreCase(label)) {
            return "Total Wins";
        }

        return label;
    }

    private static Component formatValue(String value) {
        if (BWStatsExtractor.isNotFound(value)) {
            return Component.translatable(value).withStyle(ChatFormatting.WHITE);
        }

        return Component.literal(value).withStyle(ChatFormatting.WHITE);
    }
}
