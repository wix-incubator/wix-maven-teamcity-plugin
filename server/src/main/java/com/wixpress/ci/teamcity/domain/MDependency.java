package com.wixpress.ci.teamcity.domain;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 2/19/12
 */
public class MDependency extends MArtifact {

    private List<MDependency> dependencies = newArrayList();

    public MDependency() {
    }

    public MDependency(String groupId, String artifactId, String version) {
        super(groupId, artifactId, version);
    }

    public MDependency(IArtifact artifact) {
        super(artifact);
    }

    @Override
    public boolean accept(MArtifactVisitor visitor) {
        if (visitor.visitEnter(this)){
            for (MDependency dependency: dependencies) {
                if (!dependency.accept(visitor))
                    break;
            }
        }

        return visitor.visitLeave( this );
    }

    public List<MDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<MDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public String toString() {
        return String.format("%s:%s:%s", getGroupId(), getArtifactId(), getVersion());
    }




}
