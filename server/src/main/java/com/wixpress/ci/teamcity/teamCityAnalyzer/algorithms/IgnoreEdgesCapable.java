package com.wixpress.ci.teamcity.teamCityAnalyzer.algorithms;

/**
 * @author yoav
 * @since 4/2/12
 */
public interface IgnoreEdgesCapable<NodeClass extends Vertex> {
    void ignoreEdgeTo(NodeClass nodeClass);
}
