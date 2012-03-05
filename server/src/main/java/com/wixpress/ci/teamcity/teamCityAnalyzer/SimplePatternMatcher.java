package com.wixpress.ci.teamcity.teamCityAnalyzer;

/**
 * @author yoav
 * @since 3/5/12
 */
public class SimplePatternMatcher {
    public boolean wildcardMatch(String text, String pattern) {
        String[] cards = pattern.split("\\*");

        int idx = 0;
        for (String card : cards) {
            idx = text.indexOf(card, idx);
            if(idx == -1)
                return false;
        }
        return true;
    }
}
