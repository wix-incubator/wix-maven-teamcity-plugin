package com.wixpress.ci.teamcity.domain;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.List;

/**
 * @author yoav
 * @since 2/19/12
 */
public interface IArtifact extends Tree<IArtifact>{
    public String getGroupId();
    public String getArtifactId();
    public String getVersion();
}
