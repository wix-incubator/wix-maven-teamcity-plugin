package com.wixpress.ci.teamcity.maven.workspace;

import com.wixpress.ci.teamcity.domain.IArtifact;

import java.io.File;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 2/14/12
 */
public class MavenModule implements IArtifact {
    private String groupId;
    private String artifactId;
    private String version;
    private File pomFile;
    private List<MavenModule> subModules = newArrayList();

    public MavenModule(String groupId, String artifactId, String version, File pomFile) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.pomFile = pomFile;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public File getPomFile() {
        return pomFile;
    }

    public void addSubModule(MavenModule mavenModule) {
        subModules.add(mavenModule);
    }

    public List<MavenModule> getSubModules() {
        return subModules;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("MavenModule[")
                .append(groupId).append(':').append(artifactId).append(":").append(version)
                .append(" defined at [").append(pomFile).append("]]");
        return sb.toString();
    }

    public List<IArtifact> getChildren() {
        List<IArtifact> children = newArrayList();
        children.addAll(subModules);
        return children;
    }

}
