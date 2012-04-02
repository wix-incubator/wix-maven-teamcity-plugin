package com.wixpress.ci.teamcity.teamCityAnalyzer;

/**
* @author yoav
* @since 4/2/12
*/
public interface Vertex<NodeClass extends Vertex> {
    public int hashCode();
    public boolean equals(Object other);

    Iterable<NodeClass> getChildren();
}
