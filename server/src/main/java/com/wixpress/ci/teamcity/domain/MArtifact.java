package com.wixpress.ci.teamcity.domain;

/**
 * @author yoav
 * @since 2/19/12
 */
public abstract class MArtifact implements IArtifact {
    private String groupId;
    private String artifactId;
    private String version;

    public MArtifact() {
    }

    public MArtifact(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public MArtifact(IArtifact artifact) {
        this.groupId = artifact.getGroupId();
        this.artifactId = artifact.getArtifactId();
        this.version = artifact.getVersion();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public abstract boolean accept(MArtifactVisitor visitor);
}
