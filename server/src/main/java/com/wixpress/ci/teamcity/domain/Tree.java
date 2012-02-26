package com.wixpress.ci.teamcity.domain;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.List;

/**
 * @author yoav
 * @since 2/26/12
 */
public interface Tree<T extends Tree<T>> {
    @JsonIgnore
    public List<T> getChildren();
}
