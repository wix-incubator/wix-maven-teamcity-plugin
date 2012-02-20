package com.wixpress.ci.teamcity.domain;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 2/19/12
 */
public class MDependency extends MArtifact {

    private List<MDependency> dependencies = newArrayList();
    private String scope;
    private boolean optional;

    public MDependency() {
    }

    public MDependency(String groupId, String artifactId, String version, String scope, boolean optional) {
        super(groupId, artifactId, version);
        this.scope = scope;
        this.optional = optional;
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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getGroupId()).append(":").append(getArtifactId()).append(":").append(getVersion());
        if (optional || !"".equals(scope))
            sb.append(" (")
                    .append(optional?"optional":"")
                    .append((optional && !"".equals(scope))?", ":"")
                    .append(scope)
                    .append(")");
        return sb.toString();
    }




}
