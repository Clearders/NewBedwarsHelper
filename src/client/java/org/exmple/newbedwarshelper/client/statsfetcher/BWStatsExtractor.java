package org.exmple.newbedwarshelper.client.statsfetcher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class BWStatsExtractor {
    public static final String NOT_FOUND_TRANSLATION_KEY = "statsfetcher.newbedwarshelper.not_found";

    public BWStats extractBWStats(String playername) throws Exception {
        String url = "https://hypixel.net/player/" + playername;
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get();

        String finalKD4v4 = NOT_FOUND_TRANSLATION_KEY;
        String finalKD2v2 = NOT_FOUND_TRANSLATION_KEY;
        String totalWins = NOT_FOUND_TRANSLATION_KEY;
        String finalKD = NOT_FOUND_TRANSLATION_KEY;

        for (Element tdName : doc.select("#stats-content-bedwars td.statName")) {
            String nameText = tdName.text().trim();
            Element tr = tdName.parent();
            if (tr == null) {
                continue;
            }

            Element tdValue = tr.selectFirst("td.statValue");
            if (tdValue == null) {
                continue;
            }

            String value = tdValue.text();

            if (BWStatsConstants.BWCONST_4V4V4V4_FINAL_KD.equalsIgnoreCase(nameText) && isNotFound(finalKD4v4)) {
                finalKD4v4 = value;
            } else if (BWStatsConstants.BWCONST_DOUBLES_FINAL_KD.equalsIgnoreCase(nameText) && isNotFound(finalKD2v2)) {
                finalKD2v2 = value;
            } else if (BWStatsConstants.BWCONST_TOTALWINS.equalsIgnoreCase(nameText) && isNotFound(totalWins)) {
                totalWins = value;
            } else if (BWStatsConstants.BWCONST_FINAL_KD.equalsIgnoreCase(nameText) && isNotFound(finalKD)) {
                finalKD = value;
            }

            if (!isNotFound(finalKD4v4) && !isNotFound(finalKD2v2) && !isNotFound(totalWins) && !isNotFound(finalKD)) {
                break;
            }
        }

        return new BWStats(finalKD, finalKD2v2, finalKD4v4, totalWins);
    }

    public static boolean isNotFound(String value) {
        return NOT_FOUND_TRANSLATION_KEY.equals(value);
    }

    public static class BWStats {
        private final String finalKD;
        private final String finalKD2v2;
        private final String finalKD4v4;
        private final String totalWins;

        public BWStats(String finalKD, String finalKD2v2, String finalKD4v4, String totalWins) {
            this.finalKD = finalKD;
            this.finalKD2v2 = finalKD2v2;
            this.finalKD4v4 = finalKD4v4;
            this.totalWins = totalWins;
        }

        public String getFinalKD() {
            return finalKD;
        }

        public String getFinalKD2v2() {
            return finalKD2v2;
        }

        public String getFinalKD4v4() {
            return finalKD4v4;
        }

        public String getTotalWins() {
            return totalWins;
        }
    }
}
