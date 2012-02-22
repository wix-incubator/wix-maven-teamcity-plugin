package com.wixpress.ci.teamcity.domain;

import com.google.common.collect.Iterables;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 2/19/12
 */
public class MModule extends MArtifact {
    
    private List<MModule> subModules = newArrayList();
    private MDependency dependencyTree;

    public MModule() {
    }

    public MModule(String groupId, String artifactId, String version) {
        super(groupId, artifactId, version);
    }

    public MModule(IArtifact artifact) {
        super(artifact);
    }

    @Override
    public boolean accept(MArtifactVisitor visitor) {
        if (visitor.visitEnter(this)){
            for (MModule subModule: subModules) {
                if (!subModule.accept(visitor))
                    break;
            }
            dependencyTree.accept(visitor);
        }

        return visitor.visitLeave( this );
    }

    public List<MModule> getSubModules() {
        return subModules;
    }

    public void setSubModules(List<MModule> subModules) {
        this.subModules = subModules;
    }

    public MDependency getDependencyTree() {
        return dependencyTree;
    }

    public void setDependencyTree(MDependency dependencyTree) {
        this.dependencyTree = dependencyTree;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("MModule: %s:%s:%s", getGroupId(), getArtifactId(), getVersion()));
        return sb.toString();
    }

    public Iterable<IArtifact> getChildren() {
        List<IArtifact> children = newArrayList();
        children.addAll(subModules);
        if (dependencyTree != null)
            children.addAll(dependencyTree.getDependencies());
        return children;
    }
}
