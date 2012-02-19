package com.wixpress.ci.teamcity.domain;

/**
 * @author yoav
 * @since 2/19/12
 */
public interface IArtifact {
    public String getGroupId();
    public String getArtifactId();
    public String getVersion();
}
