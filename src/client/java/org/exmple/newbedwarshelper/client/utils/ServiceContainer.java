package org.exmple.newbedwarshelper.client.utils;

import org.exmple.newbedwarshelper.client.statsfetcher.BWStatsExtractor;

public final class ServiceContainer {
    private static final BWStatsExtractor STATS_EXTRACTOR = new BWStatsExtractor();
    private static final PlayernameFormatter NAME_FORMATTER = new PlayernameFormatter();

    private ServiceContainer() {
    }

    public static BWStatsExtractor getStatsExtractor() {
        return STATS_EXTRACTOR;
    }

    public static PlayernameFormatter getNameFormatter() {
        return NAME_FORMATTER;
    }
}
