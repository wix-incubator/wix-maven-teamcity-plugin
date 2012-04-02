package com.wixpress.ci.teamcity.teamCityAnalyzer.algorithms;

/**
 * @author yoav
 * @since 4/2/12
 */
public class CycleSolvingTopologicalSorterException extends RuntimeException {
    public CycleSolvingTopologicalSorterException(String message) {
        super(message);
    }
}
